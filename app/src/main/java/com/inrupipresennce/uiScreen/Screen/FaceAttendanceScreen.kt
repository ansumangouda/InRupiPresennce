package com.inrupipresennce.uiScreen.Screen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.*
import android.location.Location
import android.location.LocationManager
import android.provider.Settings
import android.util.Log
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.location.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.inrupipresennce.R
import com.inrupipresennce.data.valu.Constants
import com.inrupipresennce.ui.theme.graentDark2
import com.inrupipresennce.ui.theme.graentlight1
import com.inrupipresennce.uiScreen.viewmodel.AttendanceViewModel
import com.inrupipresennce.utils.AttendanceStatusDialog
import com.inrupipresennce.utils.FaceEmbeddingExtractor
import com.inrupipresennce.utils.FullScreenImageDialog
import com.inrupipresennce.utils.GpsDisabledDialog
import com.inrupipresennce.utils.GpsStatusReceiver
import com.inrupipresennce.utils.PreferenceHelper
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Composable
fun FaceAttendanceScreen(viewModel: AttendanceViewModel, navController: NavHostController) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val fused = remember { LocationServices.getFusedLocationProviderClient(context) }

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var status by remember { mutableStateOf("Ready") }
    val coroutineScope = rememberCoroutineScope()
    val currentTime = remember { mutableStateOf("") }
    val currentDate = remember { mutableStateOf("") }

    var showErrorDialog by remember { mutableStateOf(false) }
    var errorTitle by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var errorIcon by remember { mutableStateOf<Int>(R.drawable.face_scan) }

    val lunchResponse by viewModel.lunchState.collectAsState()

    var isLoading by remember { mutableStateOf(false) }

    var isProcessing by remember { mutableStateOf(false) }


    var showPunchOutConfirm by remember { mutableStateOf(false) }




    //error message dialog

    fun showDialog(title: String, message: String, icon: Int = R.drawable.face_scan) {
        errorTitle = title
        errorMessage = message
        errorIcon = icon
        showErrorDialog = true
    }


    // Request permissions
    val permissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    // GPS status
    var isGpsEnabled by remember { mutableStateOf(true) }
    val gpsReceiver = remember { GpsStatusReceiver { enabled -> isGpsEnabled = enabled } }

    var currentDistance by remember { mutableStateOf<Float?>(null) }
    var isInsideOffice by remember { mutableStateOf(false) }


    val today by viewModel.todayAttendance.collectAsState()
    val punchInValue = today?.punch_in ?: "--"
    val punchOutValue = today?.punch_out ?: "--"


    val lunchInValue = today?.lunch_start_at ?: "--"
    val lunchOutValue = today?.lunch_end_at ?: "--"

    val imagePath = PreferenceHelper.getImagePath(context)


    DisposableEffect(Unit) {
        val intentFilter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        context.registerReceiver(gpsReceiver, intentFilter)
        onDispose { context.unregisterReceiver(gpsReceiver) }
    }

    LaunchedEffect(Unit) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        isGpsEnabled =
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    if (!isGpsEnabled) {
        GpsDisabledDialog(
            onOpenSettings = {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                context.startActivity(intent)
            },
            onDismiss = {}
        )
    }

    // Clock updater
    LaunchedEffect(Unit) {
        permissionsLauncher.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
        coroutineScope.launch {
            while (true) {
                val date = Date()
                currentTime.value =
                    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date)
                currentDate.value =
                    SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault()).format(date)
                delay(1000)
            }
        }
    }
    // 🔹 Observe ViewModel State
    val attendanceResponse by viewModel.attendanceState.collectAsState()
    var showFullImage by remember { mutableStateOf(false) }



    LaunchedEffect(attendanceResponse) {
        attendanceResponse?.let { res ->


            val (iconRes, title) = when (res.status_code) {
                1101 -> Pair(R.drawable.punch_in, "Invalid Descriptor")
                2101 -> Pair(R.drawable.punch_in, "Punch In Successful")
                2102 -> Pair(R.drawable.punch_out, "Punch Out Successful")
                3101 -> Pair(R.drawable.already_complet, "Already Completed")
                4101 -> Pair(R.drawable.validation, "Validation Error")
                5101 -> Pair(R.drawable.server_error, "Server Error")
                else -> Pair(R.drawable.unknow_status, "Unknown Status")
            }

            // If backend has multiple messages, join them nicely
            val messageText = when {
                res.messages != null && res.messages.isNotEmpty() ->
                    res.messages.joinToString("\n\n") // multiline list

                else -> res.message
            }

            showDialog(
                title = title,
                message = messageText,
                icon = iconRes
            )
            isLoading = false
            if (res.status_code == 2101 || res.status_code == 2102) {
                viewModel.loadTodayAttendance()   // <<< IMPORTANT
            }
            viewModel.clearAttendanceEvent()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadTodayAttendance()
    }
    // 🔹 React to updates

    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) return@LaunchedEffect

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 2000L   // every 2 seconds
        ).setMinUpdateDistanceMeters(1f).build()     // update every 1 meter

        fused.requestLocationUpdates(request, object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return

                val distResult = FloatArray(1)
            /*    Location.distanceBetween(
                    loc.latitude, loc.longitude,
                    Constants.OFFICE_LAT, Constants.OFFICE_LON,
                    distResult
                )

                currentDistance = distResult[0]
                isInsideOffice = distResult[0] <= Constants.OFFICE_RADIUS_METERS*/
                val officeLat = PreferenceHelper.getOfficeLat(context)
                val officeLon = PreferenceHelper.getOfficeLon(context)
                val officeRadius = PreferenceHelper.getOfficeRadius(context)

                if (officeLat != null && officeLon != null && officeRadius != null) {

                    Location.distanceBetween(
                        loc.latitude, loc.longitude,
                        officeLat, officeLon,
                        distResult
                    )

                    currentDistance = distResult[0]
                    isInsideOffice = distResult[0] <= officeRadius
                    Log.d("OFFICE_DEBUG", "lat=$officeLat lon=$officeLon radius=$officeRadius distance=${distResult[0]}")


                }
            }
        }, context.mainLooper)
    }

    LaunchedEffect(lunchResponse) {
        lunchResponse?.let { res ->
            showDialog(
                title = if (res.status) "Success" else "Lunch Error",
                message = res.message,
                icon = if (res.status) R.drawable.punch_in else R.drawable.validation
            )
            if (res.status) {
            viewModel.loadTodayAttendance()
        }

            viewModel.clearLunchEvent()
        }
    }


    fun alignFace(bitmap: Bitmap, face: com.google.mlkit.vision.face.Face): Bitmap {
        val leftEye = face.getLandmark(com.google.mlkit.vision.face.FaceLandmark.LEFT_EYE)?.position
        val rightEye =
            face.getLandmark(com.google.mlkit.vision.face.FaceLandmark.RIGHT_EYE)?.position
        if (leftEye != null && rightEye != null) {
            val dx = rightEye.x - leftEye.x
            val dy = rightEye.y - leftEye.y
            val angle = Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble())).toFloat()
            val matrix = Matrix()
            matrix.postRotate(-angle, bitmap.width / 2f, bitmap.height / 2f)
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }
        return bitmap
    }

    fun cropFaceBitmap(original: Bitmap, box: Rect): Bitmap {
        val padding = (box.width() * 0.2f).toInt()
        val left = (box.left - padding).coerceAtLeast(0)
        val top = (box.top - padding).coerceAtLeast(0)
        val right = (box.right + padding).coerceAtMost(original.width)
        val bottom = (box.bottom + padding).coerceAtMost(original.height)
        return Bitmap.createBitmap(original, left, top, right - left, bottom - top)
    }
    if (showErrorDialog) {
        AttendanceStatusDialog(
            icon = errorIcon,
            title = errorTitle,
            message = errorMessage,
            onDismiss = { showErrorDialog = false }
        )
    }


    // 🔹 Capture and process live face
    fun captureAndVerifyFace(loc: Location) {


      /*  val distance = FloatArray(1)
        Location.distanceBetween(
            loc.latitude, loc.longitude,
            Constants.OFFICE_LAT, Constants.OFFICE_LON, distance
        )*/
        val distance = FloatArray(1)
        val officeLat = PreferenceHelper.getOfficeLat(context)
        val officeLon = PreferenceHelper.getOfficeLon(context)
        val officeRadius = PreferenceHelper.getOfficeRadius(context)

        if (officeLat != null && officeLon != null && officeRadius != null) {

            Location.distanceBetween(
                loc.latitude, loc.longitude,
                officeLat, officeLon,
                distance
            )

            currentDistance = distance[0]
            isInsideOffice = distance[0] <= officeRadius.toFloat()
        }
        val radius = PreferenceHelper.getOfficeRadius(context).toFloat()
        if (distance[0] > radius) {//Constants.OFFICE_RADIUS_METERS
            showDialog(
                title = " Hey you are outside the office",
                message = "Out of office (${distance[0].roundToInt()}m away)",
                icon = R.drawable.away_from_office
            )
            status = "Out of office (${distance[0].roundToInt()}m away)"
            return
        }

        val file = File(context.cacheDir, "${System.currentTimeMillis()}.jpg")
        val output = ImageCapture.OutputFileOptions.Builder(file).build()

        imageCapture?.takePicture(
            output, ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                @SuppressLint("DefaultLocale")
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    status = "Detecting face..."
                    val image = InputImage.fromFilePath(context, file.toUri())
                    val detector = FaceDetection.getClient(
                        FaceDetectorOptions.Builder()
                            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                            .build()
                    )

                    detector.process(image)
                        .addOnSuccessListener { faces ->
                            if (faces.isEmpty()) {
                                showDialog(
                                    title = "❌ No face detected. Try again.",
                                    message = "Please position your face inside the frame and try again.",
                                    icon = R.drawable.face_scan
                                )
                                isProcessing = false
                                isLoading = false


                                return@addOnSuccessListener
                            }

                            coroutineScope.launch(Dispatchers.Main) {
                                status = "✅ Face detected! Generating descriptor..."
                            }

                            coroutineScope.launch(Dispatchers.IO) {
                                try {
                                    val face = faces.first()

                                    // 1) Load and correct rotation from camera file
                                    val original = correctImageRotation(file)

                                    // 2) Align using eye landmarks (if available)
                                    val aligned = alignFace(original, face)

                                    // 3) Crop around face bounding box with padding
                                    val cropped = cropFaceBitmap(aligned, face.boundingBox)

                                    // 4) Mirror (front camera selfie) so orientation matches stored images
                                    val flipMatrix = Matrix().apply { preScale(-1f, 1f) }
                                    val mirrored = Bitmap.createBitmap(
                                        cropped,
                                        0,
                                        0,
                                        cropped.width,
                                        cropped.height,
                                        flipMatrix,
                                        true
                                    )

                                    // 5) Normalize: convert to grayscale to reduce color/lighting variance
                                    val normalized = createBitmap(mirrored.width, mirrored.height)
                                    val canvas = Canvas(normalized)
                                    val paint = Paint().apply {
                                        colorFilter = ColorMatrixColorFilter(ColorMatrix().apply {
                                            setSaturation(0f) // desaturate
                                            // optional: slight scale to boost contrast/brightness
                                            // setScale(1.05f, 1.05f, 1.05f, 1f)
                                        })
                                    }
                                    canvas.drawBitmap(mirrored, 0f, 0f, paint)

                                    // 6) Save normalized live face to temp file
                                    val tempFile = File(
                                        context.cacheDir,
                                        "live_face_${System.currentTimeMillis()}.jpg"
                                    )
                                    FileOutputStream(tempFile).use {
                                        normalized.compress(Bitmap.CompressFormat.JPEG, 92, it)
                                    }
                                    Log.d(
                                        "FaceDebug",
                                        "✅ Saved normalized live face to ${tempFile.absolutePath}"
                                    )

                                    // 7) Generate embedding for live capture
                                    val liveDescriptor =
                                        FaceEmbeddingExtractor.generateEmbedding(context, tempFile)

                                    // 8) Get stored image path from prefs and download
                                    val prefs = context.getSharedPreferences(
                                        "login_prefs",
                                        Context.MODE_PRIVATE
                                    )
                                    val imagePath = prefs.getString("image", null)
                                    if (imagePath.isNullOrEmpty()) {
                                        withContext(Dispatchers.Main) {
                                            showDialog(
                                                title = " No stored face image found. Please re-login.",
                                                message = "❌No image is stored. Please re-login.",
                                                icon = R.drawable.face_scan
                                            )
                                            isProcessing = false
                                            isLoading = false


                                        }
                                        return@launch
                                    }

                                    val storedFile =
                                        viewModel.repository.downloadAdminFace(context, imagePath)
                                    if (storedFile == null) {
                                        withContext(Dispatchers.Main) {
                                            showDialog(
                                                title = " Failed to download stored image",
                                                message = "❌No image is stored. Please re-login.",
                                                icon = R.drawable.face_scan
                                            )
                                            isProcessing = false
                                            isLoading = false


                                        }
                                        return@launch
                                    }

                                    // 9) Crop/normalize stored face (extractFaceRegion does that)
                                    val croppedStoredFile = extractFaceRegion(context, storedFile)
                                    if (croppedStoredFile == null) {
                                        withContext(Dispatchers.Main) {
                                            showDialog(
                                                title = " No face detected in stored image",
                                                message = "❌Please position your face inside the frame and try again.",
                                                icon = R.drawable.face_scan
                                            )
                                            isProcessing = false
                                            isLoading = false

                                        }
                                        return@launch
                                    }

                                    // 10) Generate embedding for stored face
                                    val storedDescriptor = FaceEmbeddingExtractor.generateEmbedding(
                                        context,
                                        croppedStoredFile
                                    )

                                    // 11) Compare embeddings
                                    val similarity = compareFaces(storedDescriptor, liveDescriptor)
                                    Log.d("FaceCompare", "🔍 Similarity score = $similarity")

                                    // 12) Decide based on threshold (you can tweak thresholds)
                                    withContext(Dispatchers.Main) {
                                        when {
                                            similarity >= 0.45f -> {
                                                isProcessing = false
                                                isLoading = false

                                                status = "✅ Face verified (similarity = ${
                                                    "%.3f".format(similarity)
                                                })"
                                                coroutineScope.launch(Dispatchers.IO) {
                                                    viewModel.uploadAttendance(
                                                        adminId = viewModel.adminId,
                                                        faceDescriptor = liveDescriptor,
                                                        imageFile = file,
                                                        lat = loc.latitude,
                                                        lon = loc.longitude
                                                    )
                                                }
                                            }

                                            similarity >= 0.3f -> {
                                                isProcessing = false
                                                isLoading = false

                                                // Accept but low confidence — you can choose to require another capture or show warning
                                                status = "⚠️ Face verified (low confidence = ${
                                                    "%.3f".format(similarity)
                                                })"
                                                coroutineScope.launch(Dispatchers.IO) {
                                                    viewModel.uploadAttendance(
                                                        adminId = viewModel.adminId,
                                                        faceDescriptor = liveDescriptor,
                                                        imageFile = file,
                                                        lat = loc.latitude,
                                                        lon = loc.longitude
                                                    )
                                                }
                                            }

                                            else -> {
                                                showDialog(
                                                    title = "Face Mismatch",
                                                    message = "❌ Your face does not match our records.",
                                                    icon = R.drawable.face_scan
                                                )
                                                isProcessing = false
                                                isLoading = false


                                            }
                                        }
                                    }

                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    coroutineScope.launch(Dispatchers.Main) {
                                        showDialog(
                                            title = "Face Mismatch",
                                            message = "❌ Face descriptor failed: ${e.message}",
                                            icon = R.drawable.face_scan
                                        )

                                    }
                                }
                            }
                        }
                        .addOnFailureListener { e ->

                            status = "Face detection failed: ${e.message}"
                            isProcessing = false
                            isLoading = false

                        }
                }

                override fun onError(exception: ImageCaptureException) {
                    status = "Capture error: ${exception.message}"
                    isProcessing = false
                    isLoading = false

                }
            }
        )
    }


    // ✅ --- UI (unchanged) ---
    Scaffold(containerColor = colorScheme.background) { innerPadding ->

        if (showErrorDialog) {
            AttendanceStatusDialog(
                icon = errorIcon,
                title = errorTitle,
                message = errorMessage,
                onDismiss = { showErrorDialog = false }
            )
        }
        Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    graentlight1, // light green near camera
                                    graentDark2   // darker green toward bottom
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(0f, Float.POSITIVE_INFINITY)
                            )
                        )

                ) {

            // ⭐ TOP-RIGHT PROFILE IMAGE (FULLY FIXED)

            Image(
                painter = rememberAsyncImagePainter(
                    model = Constants.BASE_URL + (imagePath ?: "")
                ),
                contentDescription = "Profile Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(
                        top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 12.dp,
                        end = 16.dp
                    )
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.White, CircleShape)
                    .align(Alignment.TopEnd)
                        .clickable { showFullImage = true }

            )

            Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp)
                            //  .verticalScroll(scrollState)  // ✅ enables scroll
                            .padding(bottom = 80.dp),     // extra space for bottom button,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            currentTime.value,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(currentDate.value, color = Color.White, fontSize = 14.sp)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .padding(top = 200.dp)
                            .offset(y = 140.dp - 90.dp) // Starts just below camera
                            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                            .background(colorScheme.background)
                    )

                    // Camera preview
                    Card(
                        modifier = Modifier
                            .size(250.dp)
                            .align(Alignment.Center)
                            .offset(y = (-120).dp)
                            .border(3.dp, colorScheme.primary, CircleShape),
                        shape = CircleShape,
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                PreviewView(ctx).also { previewView ->
                                    val cameraProviderFuture =
                                        ProcessCameraProvider.getInstance(ctx)
                                    cameraProviderFuture.addListener({
                                        val cameraProvider = cameraProviderFuture.get()
                                        val preview = Preview.Builder().build().also {
                                            it.setSurfaceProvider(previewView.surfaceProvider)
                                        }
                                        val capture = ImageCapture.Builder()
                                            .setTargetResolution(Size(640, 480))
                                            .build()
                                        val selector = CameraSelector.DEFAULT_FRONT_CAMERA
                                        cameraProvider.unbindAll()
                                        cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            selector,
                                            preview,
                                            capture
                                        )
                                        imageCapture = capture
                                    }, ContextCompat.getMainExecutor(ctx))
                                }
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .offset(y = -(30).dp),
                        contentAlignment = Alignment.BottomCenter // 👈 anchors to bottom
                    ) {

                        // Bottom info & button
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 0.dp)
                                .align(Alignment.BottomCenter),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.padding(6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                GradientBorderInfoBox(
                                    label = "Punch In",
                                    value = punchInValue,
                                    gradientColors = listOf(graentDark2, graentlight1),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 4.dp)
                                )
                                GradientBorderInfoBox(
                                    label = "Punch Out",
                                    value = punchOutValue,
                                    gradientColors = listOf(graentDark2, graentlight1),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 4.dp)
                                )
                            }

                            Row(
                                modifier = Modifier.padding(6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                GradientBorderInfoBox(
                                    label = "Lunch In",
                                    value = lunchInValue,
                                    gradientColors = listOf(graentDark2, graentlight1),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 4.dp)
                                )
                                GradientBorderInfoBox(
                                    label = "Lunch Out",
                                    value = lunchOutValue,
                                    gradientColors = listOf(graentDark2, graentlight1),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 4.dp)
                                )
                            }

                            if (currentDistance != null) {
                                val distanceText = if (isInsideOffice) {
                                    "🏢 You’re inside the office zone (${currentDistance!!.roundToInt()} m)"
                                } else {
                                    "📍 You are ${currentDistance!!.roundToInt()} m away from office"
                                }

                                Text(
                                    text = distanceText,
                                    color = if (isInsideOffice) Color(0xFF2E7D32) else Color(
                                        0xFFD32F2F
                                    ),
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 15.sp,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                                    .offset(y = 30.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {

                                // 👉 BUTTON 1 — Capture Face
                                Button(
                                    onClick = {
                                        if (isProcessing) return@Button // 🔥 prevents double click

                                        isProcessing = true
                                        isLoading = true
                                        // If Punch-IN exists but Punch-OUT is missing → Ask confirmation
                                        if (punchInValue != "--" && punchOutValue == "--") {
                                            showPunchOutConfirm = true
                                            isProcessing = false   // ⭐ Allow dialog to show
                                            isLoading = false
                                            return@Button
                                        }
                                        status = "Checking location..."

                                        if (ActivityCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.ACCESS_FINE_LOCATION
                                            ) != PackageManager.PERMISSION_GRANTED
                                        ) {
                                            status = "Location permission not granted"
                                            return@Button
                                        }

                                        fused.lastLocation.addOnSuccessListener { loc ->
                                            if (loc != null) captureAndVerifyFace(loc)
                                            else showDialog(
                                                title = "Location Error",
                                                message = "Unable to get your current location. Please try again.",
                                                icon = R.drawable.loaction
                                            )
                                            isProcessing = false
                                            isLoading = false

                                        }
                                    },
                                    enabled = !isLoading,   // disable while loading
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                brush = Brush.linearGradient(
                                                    colors = listOf(graentDark2, graentlight1),
                                                    start = Offset(0f, Float.POSITIVE_INFINITY),
                                                    end = Offset(Float.POSITIVE_INFINITY, 0f)
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isLoading) {
                                            CircularProgressIndicator(
                                                color = Color.White,
                                                strokeWidth = 2.dp,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        } else {
                                            Text(
                                                "Capture Face",
                                                color = Color.White,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 15.sp
                                            )

                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                // 👉 BUTTON 2 — Lunch Break
                                Button(
                                    onClick = {
                                        if (!isInsideOffice) {
                                            showDialog(
                                                title = "Not Inside Office",
                                                message = "Lunch break can only be taken at office location.",
                                                icon = R.drawable.away_from_office
                                            )
                                            isLoading = false

                                            return@Button
                                        }

                                        viewModel.takeLunchBreak()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                brush = Brush.linearGradient(
                                                    colors = listOf(
                                                        Color(0xFFff8a00),
                                                        Color(0xFFffdd00)
                                                    ),
                                                    start = Offset(0f, Float.POSITIVE_INFINITY),
                                                    end = Offset(Float.POSITIVE_INFINITY, 0f)
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "Lunch Break",
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = status,
                                color = colorScheme.onBackground,
                                modifier = Modifier.offset(y = 30.dp)
                            )
                        }
                    }
                }
        if (showFullImage) {
            FullScreenImageDialog(
                imageUrl = Constants.BASE_URL + (imagePath ?: ""),
                onDismiss = { showFullImage = false }
            )
        }
        if (showPunchOutConfirm) {
            AlertDialog(
                onDismissRequest = { showPunchOutConfirm = false },
                title = { Text("Confirm Punch Out") },
                text = { Text("Do you want to log out (Punch Out)?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (isProcessing) return@TextButton  // 🔥 Prevent double tap

                            isProcessing = true
                            showPunchOutConfirm = false
                            isLoading = true

                            if (ActivityCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                status = "Location permission not granted"
                                isProcessing = false
                                isLoading = false
                                return@TextButton
                            }

                            fused.lastLocation.addOnSuccessListener { loc ->
                                if (loc != null) captureAndVerifyFace(loc)
                                else showDialog(
                                    title = "Location Error",
                                    message = "Unable to get your current location.",
                                    icon = R.drawable.loaction
                                )

                                // do NOT reset here — captureAndVerifyFace will reset
                            }
                        }
                    ){ Text("Yes") }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showPunchOutConfirm = false
                            isLoading = false
                        }
                    ) { Text("No") }
                }
            )
        }


    }
}
@Composable
fun GradientBorderInfoBox(
    label: String,
    value: String,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(gradientColors),
                shape = RoundedCornerShape(12.dp)
            )
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = label,
                color = Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


fun correctImageRotation(file: File): Bitmap {
    val exif = androidx.exifinterface.media.ExifInterface(file.absolutePath)
    val orientation = exif.getAttributeInt(
        androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION,
        androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL
    )

    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
    val matrix = Matrix()
    when (orientation) {
        androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
    }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}


// 🔹 Compare embeddings
fun compareFaces(storedJson: String, liveJson: String): Float {
    val stored = JSONArray(storedJson)
    val live = JSONArray(liveJson)
    var dot = 0f;
    var normA = 0f;
    var normB = 0f
    for (i in 0 until stored.length()) {
        val a = stored.getDouble(i).toFloat()
        val b = live.getDouble(i).toFloat()
        dot += a * b; normA += a * a; normB += b * b
    }
    return dot / (sqrt(normA) * sqrt(normB))
}

// 🔹 Crop + brighten + mirror stored face
// 🔹 Crop + brighten/match + mirror stored face (replace existing extractFaceRegion)
suspend fun extractFaceRegion(context: Context, imageFile: File): File? {
    val image = InputImage.fromFilePath(context, imageFile.toUri())
    val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE).build()
    )
    val faces = detector.process(image).await()
    if (faces.isEmpty()) return null

    val face = faces.first()
    val original = BitmapFactory.decodeFile(imageFile.absolutePath)

    // we assume stored image might already be upright; if not, rotate appropriately
    // (if your stored images come rotated, you can apply correctImageRotation here too)
    val rotated = Bitmap.createBitmap(original, 0, 0, original.width, original.height)

    val padding = (face.boundingBox.width() * 0.2f).toInt()
    val left = (face.boundingBox.left - padding).coerceAtLeast(0)
    val top = (face.boundingBox.top - padding).coerceAtLeast(0)
    val right = (face.boundingBox.right + padding).coerceAtMost(rotated.width)
    val bottom = (face.boundingBox.bottom + padding).coerceAtMost(rotated.height)
    var cropped = Bitmap.createBitmap(rotated, left, top, right - left, bottom - top)

    // Mirror stored image to match selfie orientation (if required)
    val flipMatrix = Matrix().apply { preScale(-1f, 1f) }
    cropped = Bitmap.createBitmap(cropped, 0, 0, cropped.width, cropped.height, flipMatrix, true)

    // Normalize: convert to grayscale to match live normalization
    val normalized = createBitmap(cropped.width, cropped.height)
    val canvas = Canvas(normalized)
    val paint = Paint().apply {
        colorFilter = ColorMatrixColorFilter(ColorMatrix().apply {
            setSaturation(0f)
            // optional small boost: setScale(1.05f, 1.05f, 1.05f, 1f)
        })
    }
    canvas.drawBitmap(cropped, 0f, 0f, paint)

    val croppedFile = File(context.cacheDir, "face_${System.currentTimeMillis()}.jpg")
    FileOutputStream(croppedFile).use {
        normalized.compress(Bitmap.CompressFormat.JPEG, 95, it)
    }
    Log.d("FaceDebug", "✅ Saved normalized stored face to ${croppedFile.absolutePath}")
    return croppedFile
}

