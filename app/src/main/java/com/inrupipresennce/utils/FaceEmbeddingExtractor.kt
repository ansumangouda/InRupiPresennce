package com.inrupipresennce.utils


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import org.json.JSONArray
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sqrt
import androidx.core.graphics.scale

object FaceEmbeddingExtractor {
    private const val TAG = "FaceEmbedding"
    private const val MODEL_FILE = "mobilefacenet.tflite" // must exist in assets/
    private var interpreter: Interpreter? = null
    private const val NUM_THREADS = 4

    private fun getInterpreter(context: Context): Interpreter {
        if (interpreter == null) {
            val model = FileUtil.loadMappedFile(context, MODEL_FILE)
            val opts = Interpreter.Options().apply { setNumThreads(NUM_THREADS) }
            interpreter = Interpreter(model, opts)
            Log.d(TAG, "✅ Loaded model: $MODEL_FILE")

            // Log shapes
            val inT = interpreter!!.getInputTensor(0)
            val outT = interpreter!!.getOutputTensor(0)
            Log.d(TAG, "Input shape: ${inT.shape().contentToString()}, dtype=${inT.dataType()}")
            Log.d(TAG, "Output shape: ${outT.shape().contentToString()}, dtype=${outT.dataType()}")
        }
        return interpreter!!
    }

    /** Generate normalized embedding JSON from image */
    @Throws(Exception::class)
    fun generateEmbedding(context: Context, imageFile: File): String {
        val bmp = BitmapFactory.decodeFile(imageFile.absolutePath)
            ?: throw IllegalArgumentException("Cannot decode image: ${imageFile.path}")

        val interp = getInterpreter(context)
        val inputTensor = interp.getInputTensor(0)
        val inputShape = inputTensor.shape() // e.g. [1,112,112,3] or [2,112,112,3]
        if (inputShape.size < 4) throw IllegalArgumentException("Unexpected input tensor shape: ${inputShape.contentToString()}")

        val batchSize = inputShape[0]
        val height = inputShape[1]
        val width = inputShape[2]
        val channels = inputShape[3]
        val dtype = inputTensor.dataType()

        val scaled = bmp.scale(width, height)
        val singleBuffer = bitmapToInputBuffer(scaled, dtype, channels)

        // 👇 Fix for batch dimension (duplicate image if needed)
        val fullBuffer = if (batchSize > 1) {
            val full = ByteBuffer.allocateDirect(singleBuffer.capacity() * batchSize)
                .order(ByteOrder.nativeOrder())
            repeat(batchSize) {
                full.put(singleBuffer.duplicate())
            }
            full.rewind()
            full
        } else {
            singleBuffer
        }

        val outTensor = interp.getOutputTensor(0)
        val embShape = outTensor.shape()
        val embSize = embShape.last()

        // Allocate output array for one batch
        val output = Array(batchSize) { FloatArray(embSize) }

        try {
            synchronized(this) {
                interp.run(fullBuffer, output)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Inference error: ${e.message}", e)
            throw e
        }

        // Use first embedding (since all batches are identical)
        val normalized = l2Normalize(output[0])
        Log.d(TAG, "✅ Embedding generated (size=${normalized.size})")
        return JSONArray(normalized).toString()
    }

    /** Converts Bitmap to Tensor-friendly ByteBuffer */
    private fun bitmapToInputBuffer(bitmap: Bitmap, dtype: DataType, channels: Int): ByteBuffer {
        require(channels == 1 || channels == 3) { "Only 1 or 3 channel inputs supported (got $channels)" }

        val w = bitmap.width
        val h = bitmap.height
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)

        val mean = 127.5f
        val std = 128.0f

        return when (dtype) {
            DataType.FLOAT32 -> {
                val bb = ByteBuffer.allocateDirect(4 * w * h * channels).order(ByteOrder.nativeOrder())
                var idx = 0
                for (y in 0 until h) {
                    for (x in 0 until w) {
                        val p = pixels[idx++]
                        val r = ((p shr 16) and 0xFF).toFloat()
                        val g = ((p shr 8) and 0xFF).toFloat()
                        val b = (p and 0xFF).toFloat()
                        if (channels == 3) {
                            bb.putFloat((r - mean) / std)
                            bb.putFloat((g - mean) / std)
                            bb.putFloat((b - mean) / std)
                        } else {
                            val gray = (0.299f * r + 0.587f * g + 0.114f * b)
                            bb.putFloat((gray - mean) / std)
                        }
                    }
                }
                bb.rewind()
                bb
            }

            DataType.UINT8 -> {
                val bb = ByteBuffer.allocateDirect(w * h * channels).order(ByteOrder.nativeOrder())
                var idx = 0
                for (y in 0 until h) {
                    for (x in 0 until w) {
                        val p = pixels[idx++]
                        val r = ((p shr 16) and 0xFF).toByte()
                        val g = ((p shr 8) and 0xFF).toByte()
                        val b = (p and 0xFF).toByte()
                        if (channels == 3) {
                            bb.put(r); bb.put(g); bb.put(b)
                        } else {
                            val gray = (((p shr 16) and 0xFF) * 0.299 +
                                    ((p shr 8) and 0xFF) * 0.587 +
                                    (p and 0xFF) * 0.114).toInt().toByte()
                            bb.put(gray)
                        }
                    }
                }
                bb.rewind()
                bb
            }

            else -> {
                Log.w(TAG, "⚠️ Unsupported dtype: $dtype, defaulting to FLOAT32 encoding.")
                val bb = ByteBuffer.allocateDirect(4 * w * h * channels).order(ByteOrder.nativeOrder())
                var idx = 0
                for (y in 0 until h) {
                    for (x in 0 until w) {
                        val p = pixels[idx++]
                        val r = ((p shr 16) and 0xFF).toFloat()
                        val g = ((p shr 8) and 0xFF).toFloat()
                        val b = (p and 0xFF).toFloat()
                        bb.putFloat((r - mean) / std)
                        bb.putFloat((g - mean) / std)
                        bb.putFloat((b - mean) / std)
                    }
                }
                bb.rewind()
                bb
            }
        }
    }

    /** Normalize embedding to unit length */
    private fun l2Normalize(vec: FloatArray): FloatArray {
        var sum = 0f
        for (v in vec) sum += v * v
        val norm = sqrt(sum)
        return if (norm > 0f) FloatArray(vec.size) { i -> vec[i] / norm } else vec
    }

    /** Close model cleanly */
    fun close() {
        interpreter?.close()
        interpreter = null
        Log.d(TAG, "Interpreter closed")
    }
}
