package com.inrupipresennce.utils

// UniversalFaceProcessor.kt
import android.content.Context
import android.graphics.*
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream

object UniversalFaceProcessor {

    /**
     * Process a File (live or stored) and return a processed File (112x112, grayscale, mirrored).
     * Returns null if no face was detected.
     */
    suspend fun process(context: Context, file: File): File? {

        // 1) Load bitmap from file (file must already be oriented correctly; FaceEnrollDialog ensures this)
        val bmp = BitmapFactory.decodeFile(file.absolutePath) ?: return null

        // 2) Detect face on the bitmap (we assume bitmap is upright)
        val detector = FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .build()
        )

        val input = InputImage.fromBitmap(bmp, 0)
        val faces = detector.process(input).await()
        if (faces.isEmpty()) return null
        val face = faces.first()

        // 3) Align by eyes if available
        val aligned = alignFace(bmp, face)

        // 4) Crop around face bounding box with padding
        val cropped = cropFace(aligned, face.boundingBox)

        // 5) Mirror (front camera selfie) to match stored orientation convention
        val flipMatrix = Matrix().apply { preScale(-1f, 1f) }
        val mirrored = Bitmap.createBitmap(cropped, 0, 0, cropped.width, cropped.height, flipMatrix, true)

        // 6) Convert to grayscale
        val gray = toGray(mirrored)

        // 7) Resize to 112x112
        val resized = gray.scale(112, 112)

        // 8) Save
        val outFile = File(context.cacheDir, "face_clean_${System.currentTimeMillis()}.jpg")
        FileOutputStream(outFile).use {
            resized.compress(Bitmap.CompressFormat.JPEG, 95, it)
        }

        return outFile
    }

    private fun alignFace(bmp: Bitmap, face: com.google.mlkit.vision.face.Face): Bitmap {
        val leftEye = face.getLandmark(com.google.mlkit.vision.face.FaceLandmark.LEFT_EYE)?.position
        val rightEye = face.getLandmark(com.google.mlkit.vision.face.FaceLandmark.RIGHT_EYE)?.position

        if (leftEye != null && rightEye != null) {
            val dx = rightEye.x - leftEye.x
            val dy = rightEye.y - leftEye.y
            val angle = Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble())).toFloat()
            val matrix = Matrix().apply { postRotate(-angle, bmp.width / 2f, bmp.height / 2f) }
            return Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
        }
        return bmp
    }

    private fun cropFace(original: Bitmap, box: Rect): Bitmap {
        val pad = (box.width() * 0.20f).toInt()
        val left = (box.left - pad).coerceAtLeast(0)
        val top = (box.top - pad).coerceAtLeast(0)
        val right = (box.right + pad).coerceAtMost(original.width)
        val bottom = (box.bottom + pad).coerceAtMost(original.height)
        return Bitmap.createBitmap(original, left, top, right - left, bottom - top)
    }

    private fun toGray(src: Bitmap): Bitmap {
        val result = createBitmap(src.width, src.height)
        val canvas = Canvas(result)
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
        }
        canvas.drawBitmap(src, 0f, 0f, paint)
        return result
    }
}
