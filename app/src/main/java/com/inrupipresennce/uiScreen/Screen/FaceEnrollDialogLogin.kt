// FaceEnrollDialog.kt
package com.runamargapresence.uiScreen.Screen

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.ImageCaptureException
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.camera.view.PreviewView
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.core.CameraSelector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.ByteArrayOutputStream
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.compose.foundation.layout.*
import androidx.compose.ui.text.font.FontWeight
import java.lang.Exception

/**
 * Small composable that shows camera preview and a Capture button.
 * - onCaptured(file) returns the saved image file (correctly rotated according to CameraX rotationDegrees).
 * - onDismiss() invoked when user cancels enrollment.
 *
 * IMPLEMENTATION NOTES:
 * - We use ImageCapture.OnImageCapturedCallback to get ImageProxy and rotationDegrees (device-specific).
 * - Convert ImageProxy (YUV_420_888) -> NV21 -> JPEG -> Bitmap, rotate by imageInfo.rotationDegrees,
 *   then save to file and pass file to onCaptured.
 */
@Composable
fun FaceEnrollDialog(
    imageCapture: ImageCapture? = null, // optional pre-configured ImageCapture; if null we create one locally
    onCaptured: (File) -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean,
    setLoading: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    var localImageCapture by remember { mutableStateOf(imageCapture) }
    var faceLoading = isLoading

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Scan Your Face", fontWeight = FontWeight.Bold) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .clip(CircleShape)
                ) {
                    AndroidView(factory = { ctx ->
                        val previewView = PreviewView(ctx)

                        // Setup camera provider and bind preview + capture
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()

                            // create local imageCapture if caller didn't provide one
                            if (localImageCapture == null) {
                                localImageCapture = ImageCapture.Builder()
                                    // you can set TargetResolution if desired
                                    .build()
                            }

                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val selector = CameraSelector.DEFAULT_FRONT_CAMERA

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    selector,
                                    preview,
                                    localImageCapture
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }, ContextCompat.getMainExecutor(ctx))

                        previewView
                    }, modifier = Modifier.fillMaxSize())
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                setLoading(true)   // start loading

                val capture = localImageCapture
                if (capture == null) {
                    Toast.makeText(context, "Camera not ready", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                capture.takePicture(ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(imageProxy: ImageProxy) {
                            coroutineScope.launch(Dispatchers.IO) {
                                try {
                                    // Convert ImageProxy -> Bitmap
                                    val bitmap = imageProxyToBitmap(imageProxy)
                                    val rotation = imageProxy.imageInfo.rotationDegrees
                                    imageProxy.close()

                                    // Rotate bitmap according to rotationDegrees (if needed)
                                    val rotated = if (rotation != 0) {
                                        val m = Matrix().apply { postRotate(rotation.toFloat()) }
                                        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
                                    } else bitmap

                                    // Save rotated bitmap to temp file
                                    val file = File(context.cacheDir, "face_enroll_${System.currentTimeMillis()}.jpg")
                                    FileOutputStream(file).use { out ->
                                        rotated.compress(Bitmap.CompressFormat.JPEG, 92, out)
                                    }

                                    withContext(Dispatchers.Main) {
                                        faceLoading = false

                                        onCaptured(file)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    withContext(Dispatchers.Main) {
                                        faceLoading = false

                                        Toast.makeText(context, "Capture failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }

                        override fun onError(exception: ImageCaptureException) {
                            super.onError(exception)
                            exception.printStackTrace()
                            // forward to UI thread
                            coroutineScope.launch(Dispatchers.Main) {
                                Toast.makeText(context, "Capture error: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    })
            },
                enabled = !isLoading
                // disable when loadin
            ) {
                if (faceLoading) {
                    CircularProgressIndicator(
                        color = ProgressIndicatorDefaults.circularColor,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text("Capture Face")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * Convert YUV_420_888 ImageProxy into Bitmap via NV21 -> JPEG route.
 * This is a commonly used conversion that works reliably across devices.
 */
private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
    return when (image.format) {

        ImageFormat.JPEG -> {
            // Most Vivo / Oppo / Realme front cameras return JPEG directly
            val buffer = image.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }

        ImageFormat.YUV_420_888 -> {
            // Pixel / Samsung usually return this format
            val yBuffer = image.planes[0].buffer
            val uBuffer = image.planes[1].buffer
            val vBuffer = image.planes[2].buffer

            val ySize = yBuffer.remaining()
            val uSize = uBuffer.remaining()
            val vSize = vBuffer.remaining()

            val nv21 = ByteArray(ySize + uSize + vSize)
            yBuffer.get(nv21, 0, ySize)
            vBuffer.get(nv21, ySize, vSize)
            uBuffer.get(nv21, ySize + vSize, uSize)

            val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 90, out)
            val imageBytes = out.toByteArray()
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        }

        else -> {
            throw IllegalArgumentException("Unsupported image format: ${image.format}")
        }
    }
}

