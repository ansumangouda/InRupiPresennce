// LoginScreen.kt
package com.inrupipresennce.uiScreen.Screen

import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inrupipresennce.data.repositry.LoginRepository
import com.inrupipresennce.uiScreen.ViewModelFactory.LoginViewModelFactory
import com.inrupipresennce.uiScreen.viewmodel.LoginViewModel
import com.inrupipresennce.utils.PreferenceHelper
import com.inrupipresennce.utils.UniversalFaceProcessor
import com.runamargapresence.uiScreen.Screen.FaceEnrollDialog
import com.inrupipresennce.R
import com.inrupipresennce.utils.FaceEmbeddingExtractor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@SuppressLint("DefaultLocale")
@Composable
fun LoginScreen(onLoginSuccess: (adminId: Int) -> Unit) {
    val context = LocalContext.current
    val factory = LoginViewModelFactory(context)
    val viewModel: LoginViewModel = viewModel(factory = factory)
    val loginRepo = LoginRepository(context)

    val loginResult by viewModel.loginState.collectAsState()

    var phone by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    var isFaceLoading by remember { mutableStateOf(false) }

    var showFaceScanDialog by remember { mutableStateOf(false) }
    var cameraPermissionRequested by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    var showErrorDialog by remember { mutableStateOf(false) }
    var errorTitle by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var errorIcon by remember { mutableStateOf(R.drawable.face_scan) }

    fun showDialog(title: String, message: String, icon: Int = R.mipmap.ic_launcher_foreground) {
        errorTitle = title
        errorMessage = message
        errorIcon = icon
        showErrorDialog = true
    }


    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            cameraPermissionRequested = true
            if (granted) showFaceScanDialog = true
            else {
                isLoading = false
                Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    LaunchedEffect(loginResult) {
        loginResult?.let { result ->
            // isLoading = false

            if (result.success) {

                // 🔥 BYPASS FACE VERIFICATION FOR SPECIFIC ADMIN ID
                if (result.id == 111) {
                    PreferenceHelper.saveLoginData(
                        context,
                        result.id,
                        result.name,
                        result.image,
                        result.officeLat,
                        result.officeLon,
                        result.officeRadius
                    )
                    PreferenceHelper.setFaceVerified(context, true)

                    Toast.makeText(context, "Login Successful (Face Bypassed)", Toast.LENGTH_SHORT).show()

                    onLoginSuccess(result.id)
                    return@LaunchedEffect
                }

                // 👉 normal flow for other users
                if (!cameraPermissionRequested) {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                } else {
                    showFaceScanDialog = true
                }
            } else {
                Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                isLoading = false
            }

            try { viewModel.checkLogin() } catch (_: Exception) {}
        }
    }


    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
             /*   .background(
                    brush = Brush.linearGradient(
                        colors = listOf(graentDark2, graentlight1),
                        start = Offset(0f, Float.POSITIVE_INFINITY),
                        end = Offset(Float.POSITIVE_INFINITY, 0f)
                    )
                ),*/
            contentAlignment = Alignment.TopCenter
        ) {

            InRupiBackground()

            Image(
                painter = painterResource(id = R.drawable.inrupi_round_logo),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.height(100.dp).width(100.dp).offset(y = (160).dp)
            )


            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .offset(y = (-40).dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Login", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Enter your registered phone number",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it.filter { c -> c.isDigit() }.take(10) },
                    placeholder = { Text("10 digits...") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))


                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ContinueButton(
                        onClick = {
                            if (isLoading) return@ContinueButton

                            if (phone.length != 10) {
                                showDialog(
                                    title = "Invalid Number",
                                    message = "Please enter a valid 10-digit phone number."
                                )
                                return@ContinueButton
                            }

                            isLoading = true
                            cameraPermissionRequested = false
                            viewModel.login(phone)
                        },
                        isLoading = isLoading,      // ⭐ pass loading state
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }
            }
        }
    }

    val funnyMessages = listOf(
        "User validated. Image not present.",
        "User confirmed. Profile photo decided not to show up today.",
        "User located. Image still negotiating its appearance.",
        "User details retrieved. Image declined to participate in the login process.",
        "User located, but the display picture seems to be on vacation.",
        "All good! User verified. Image, however, chose not to participate."
    )

    // Face enroll dialog
    if (showFaceScanDialog) {
        FaceEnrollDialog(
            isLoading = isFaceLoading,
            setLoading = { isFaceLoading = it },
            onCaptured = { file ->
                isFaceLoading = true
                coroutineScope.launch(Dispatchers.IO) {
                    try {
                        // Use UniversalFaceProcessor for both live and stored images
                        val processedLive = UniversalFaceProcessor.process(context, file)
                        if (processedLive == null) {
                            withContext(Dispatchers.Main) {
                                showDialog(
                                    title = "Face is not match",
                                    message = "your face is not match with our record",
                                    icon = R.drawable.face_scan
                                )
                            }
                            isFaceLoading =  false

                            return@launch
                        }

                        val liveDescriptor = FaceEmbeddingExtractor.generateEmbedding(context, processedLive)

                        val storedPath = loginResult?.image
                        val storedFile = loginRepo.downloadAdminFace(context, storedPath!!)
                        if (storedFile == null) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Your image is missing", Toast.LENGTH_SHORT).show()
                            }

                            isFaceLoading =  false
                            return@launch
                        }

                        val processedStored = UniversalFaceProcessor.process(context, storedFile)
                        if (processedStored == null) {
                            withContext(Dispatchers.Main) {
                                val randomMessage = funnyMessages.random()
                                Toast.makeText(context, randomMessage, Toast.LENGTH_SHORT).show()
                            }
                            isFaceLoading =  false
                            return@launch
                        }

                        val storedDescriptor = FaceEmbeddingExtractor.generateEmbedding(context, processedStored)
                        val similarity = compareFaces(storedDescriptor, liveDescriptor)

                        withContext(Dispatchers.Main) {
                            Log.d("LOGIN_FACE", "Similarity = $similarity")

                            if (similarity >= 0.35f) {
                                PreferenceHelper.saveLoginData(
                                    context,
                                    adminId = loginResult!!.id,
                                    name = loginResult!!.name,
                                    image = loginResult!!.image,
                                    officeLat = loginResult!!.officeLat,
                                    officeLon = loginResult!!.officeLon,        // ✅ Correct value
                                    officeRadius = loginResult!!.officeRadius
                                )
                                PreferenceHelper.setFaceVerified(context, true)
                                Toast.makeText(context, "Face Verified!", Toast.LENGTH_SHORT).show()
                                showFaceScanDialog = false
                                onLoginSuccess(loginResult!!.id)
                            } else {

                                Toast.makeText(context, "Face mismatch (${String.format("%.3f", similarity)})", Toast.LENGTH_LONG).show()
                                isFaceLoading = false

                            }
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            isFaceLoading = false

                        }
                    }
                }
            },
            onDismiss = {
                showFaceScanDialog = false
                isLoading = false
            }
        )
    }
    if (showErrorDialog) {
        StatusDialog(

            title = errorTitle,
            message = errorMessage,
            icon = errorIcon,
            onDismiss = { showErrorDialog = false }
        )
    }

}
@Composable
fun ContinueButton(
    onClick: () -> Unit,
    isLoading: Boolean = false,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(Color.White)
            .clickable(enabled = !isLoading) { onClick() }   // ⭐ disable while loading
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isLoading) "Please wait..." else "Continue",   // ⭐ loading text
            color = Color(0xFF0F1C5B),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.width(16.dp))

        if (!isLoading) {
            AnimatedArrowBox()    // ⭐ hide animation during loading
        }
    }
}



@Composable
fun AnimatedArrowBox() {

    val infiniteTransition = rememberInfiniteTransition()
    // Animate the whole circle box
    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,   // distance to move right
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .size(25.dp)
            .offset(x = offsetX.dp)  // ⭐ move whole circular button
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF4FCBFF),
                        Color(0xFF264DFF)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_arrow_right),
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
    }
}
fun DrawScope.gradientArc(
    topLeft: Offset,
    size: Size,
    startAngle: Float,
    sweepAngle: Float,
    useCenter: Boolean = false,
    brushProvider: (Rect) -> Brush,   // YOU provide gradient per arc
    blendMode: BlendMode = BlendMode.SrcOver // default blend mode
) {
    // Create rect for arc bounds
    val rect = Rect(
        topLeft = topLeft,
        bottomRight = Offset(
            topLeft.x + size.width,
            topLeft.y + size.height
        )
    )

    drawIntoCanvas { canvas ->
        val paint = Paint()

        // ⭐ Get YOUR custom gradient for THIS arc
        val brush = brushProvider(rect)

        // ⭐ Apply gradient to paint
        brush.applyTo(
            size = Size(rect.width, rect.height),
            p = paint,
            alpha = 1f
        )

        // ⭐ Apply overlay or any blend mode you want
        paint.blendMode = blendMode

        canvas.save()

        // Align gradient inside arc
        canvas.translate(rect.left, rect.top)

        // Draw arc using gradient paint
        canvas.drawArc(
            rect = Rect(0f, 0f, rect.width, rect.height),
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = useCenter,
            paint = paint
        )

        canvas.restore()
    }
}


@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun InRupiBackground() {


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF052EB7).copy(alpha = 0.8f),

                        Color(0xFF0B0F59),
                        // Color(0xFF167AD4),
                    ),
                    center = Offset(
                        0f,    // 🔥 LEFT
                        1100f   // 🔥 MIDDLE VERTICAL
                    ),
                    radius = 1400f,      // scale (as fraction of screen)
                    tileMode = TileMode.Clamp
                )
            )

    ) {

        Canvas(modifier = Modifier.fillMaxSize()) {

            val width = size.width
            val height = size.height

            // ********** LARGE TOP RIGHT PURPLE BUBBLE **********
            gradientArc(
                topLeft = Offset(width * 0.4f, height * 0.02f),
                size = Size(width * 0.48f, width * 0.48f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = true,
                blendMode = BlendMode.Plus,   // <-- overlay here
                brushProvider = { rect ->
                    Brush.linearGradient(
                        colors = listOf(

                            Color(0xFFFFFFFF).copy(alpha = 0.1f),
                            Color(0xFF97B7EA).copy(alpha = 0.11f),
                            Color(0xFF2936EA).copy(alpha = 0.12f),
                            Color(0xFF3433B3).copy(alpha = 0.14f),
                            Color(0xFF2934EA).copy(alpha = 0.20f),
                            Color.White.copy(alpha = 0.9f)          // bottom
                        ),
                        end = Offset(rect.right, rect.top),      // ⭐ your control
                        start = Offset(rect.left, rect.bottom)      // ⭐ your control
                    )
                }
            )


            // ********** TWO SMALL TOP RIGHT BUBBLES **********
            gradientArc(

                topLeft = Offset(width * 0.67f, height * 0.26f),
                size = Size(width * 0.20f, width * 0.20f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = true,
                blendMode = BlendMode.Overlay,
                brushProvider = { rect ->
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF),   // top
                            Color(0xFF25737A)//.copy(alpha = 0.5f)    // bottom
                        ),
                        end = Offset(rect.right, rect.top),      // ⭐ your control
                        start = Offset(rect.left, rect.bottom)      // ⭐ your control
                    )
                }

            )

            // ********** HUGE BOTTOM-LEFT ARC (MAIN SHAPE) **********
            gradientArc(

                size = Size(width * 1.9f, height * 1f),
                topLeft = Offset(-width * .69f, height * .15f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                blendMode = BlendMode.Plus,
                brushProvider = { rect ->
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1E00FF).copy(alpha = .33f),   // top
                            Color(0xFF64A4FF)    // bottom
                        ),
                        start = Offset(rect.left, rect.top),      // ⭐ your control
                        end = Offset(rect.left, rect.bottom)

                    )
                }

            )
            gradientArc(

                size = Size(width * 2.2f, height * 2.2f),
                // topLeft = Offset(width * 0f, height * 0f),
                topLeft = Offset(-width * 1.39f, height * .5f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                blendMode = BlendMode.Overlay,
                brushProvider = { rect ->
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFBFCFC).copy(alpha = 0.2f),
                            Color(0xFF3196FB),   // top. 0xFF31F2FB
                            Color(0xFF3140FF),
                            // bottom
                        ),
                        // ⭐ your control
                        start = Offset(rect.left, rect.top),
                        end = Offset(rect.right, rect.bottom),
                    )
                }
            )
            gradientArc(
                topLeft = Offset(200f,1900f),
                size = Size(width *3f, height * 0.52f),
                // color = Color(0xff0DD9FD),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                blendMode = BlendMode.Hardlight,
                brushProvider = { rect ->
                    Brush.linearGradient(
                        // ✔ Provide stop–color pairs instead of list + stops[]
                        0.10f to Color(0xFF0050E7).copy(alpha = 0.3f),  // bright top-left
                        // 0.9f to Color(0xFF31C2FB),                     // middle band
                        1.0f to Color(0xFF3140FF).copy(alpha = 1f),   // bottom-right fade

                        // ✔ Your control of gradient direction
                        start = Offset(rect.left, rect.top),         // gradient begins top-left
                        end = Offset(rect.right, rect.bottom)        // diagonal to bottom-right
                    )
                }
            )
        }
    }
}
@Composable
fun StatusDialog(
    title: String,
    message: String,
    icon: Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Image(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
        },
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}
