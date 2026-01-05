package com.inrupipresennce.uiScreen.Screen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.location.Location
import android.os.Looper
import android.util.Log
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.inrupipresennce.R
import com.inrupipresennce.data.model.EarlyTeammate
import com.inrupipresennce.data.model.TeamMember
import com.inrupipresennce.data.model.Teammate
import com.inrupipresennce.data.valu.Constants
import com.inrupipresennce.ui.theme.graentDark2
import com.inrupipresennce.ui.theme.graentlight1
import com.inrupipresennce.uiScreen.viewmodel.AttendanceViewModel
import com.inrupipresennce.utils.FaceEmbeddingExtractor
import com.inrupipresennce.utils.FullScreenImageDialog
import com.inrupipresennce.utils.PreferenceHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.atan2


@Composable
fun Facescreen(viewModel: AttendanceViewModel) {
    val context = LocalContext.current
    val attendanceResponse by viewModel.attendanceState.collectAsState()
    val lunchState by viewModel.lunchState.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    val today by viewModel.todayAttendance.collectAsState()
    val earlyToday by viewModel.earlyBirdToday.collectAsState()
    val earlyMonthly by viewModel.earlyBirdMonthly.collectAsState()
    val teamMates by viewModel.teamMates.collectAsState()
    var selectedTeammate by remember { mutableStateOf<TeamMember?>(null) }


    val currentTime = remember { mutableStateOf("") }
    val currentDate = remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    val punchInValue = today?.punch_in ?: "--"
    val punchOutValue = today?.punch_out ?: "--"
    val lunchInValue = today?.lunch_start_at ?: "--"
    val lunchOutValue = today?.lunch_end_at ?: "--"


    var isExpanded by remember { mutableStateOf(false) }

    val allTeammates by viewModel.birthdayTeammates.collectAsState()

    val collapsedCount = 5
    val visibleTeammates = if (isExpanded) {
        allTeammates
    } else {
        allTeammates.take(collapsedCount)
    }

    val remainingCount = allTeammates.size - collapsedCount


    var isProcessing by remember { mutableStateOf(false) }
    var currentDistance by remember { mutableStateOf<Float?>(null) }
    var isInsideOffice by remember { mutableStateOf(false) }
    var isOfficeWifi by remember { mutableStateOf(false) }
    var showAllBirthdays by remember { mutableStateOf(false) }
    val officeBssids = setOf(
        "5C:E9:31:E8:8A:5F", // 2.4 GHz
        "5C:E9:31:E8:8A:61"  // 5 GHz
    )


//loaction
    val fused = remember { LocationServices.getFusedLocationProviderClient(context) }

    var showPunchOutConfirm by remember { mutableStateOf(false) }

    val imagePath = PreferenceHelper.getImagePath(context)
    var showFullImage by remember { mutableStateOf(false) }

    if (showPunchOutConfirm) {
        AlertDialog(
            onDismissRequest = { showPunchOutConfirm = false },
            title = { Text("Confirm Punch Out") },
            text = { Text("You have already punched in. Do you want to punch out?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPunchOutConfirm = false
                        isProcessing = true
                        // Existing punch-in logic is reused for punch-out
                        if (ActivityCompat.checkSelfPermission(
                                context, Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            viewModel.setErrorMessage("Location permission is required.")
                            isProcessing = false
                            return@TextButton
                        }
                        fused.lastLocation.addOnSuccessListener { loc ->
                            if (loc != null) {
                                captureAndVerifyFace(
                                    loc,
                                    context,
                                    imageCapture,
                                    coroutineScope,
                                    viewModel,
                                    onDistanceUpdate = { newDistance -> currentDistance = newDistance },
                                    onIsInsideOfficeUpdate = { newIsInside -> isInsideOffice = newIsInside },
                                    onError = { msg ->
                                        viewModel.setErrorMessage(msg)
                                        isProcessing = false // Also turn off processing on error
                                    },
                                    onDone = { isProcessing = false }
                                )
                            } else {
                                viewModel.setErrorMessage("Could not get location.")
                                isProcessing = false
                            }
                        }
                    }) {
                    Text("Yes, Punch Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPunchOutConfirm = false }) {
                    Text("Cancel")
                }
            })
    }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 1.dp)
                .background(Color(0xFFF6F6F6))
        )
        {
            item {
                HomePageHeader(
                    currentTime,
                    currentDate,
                    imagePath,
                    onImageClick = { showFullImage = true },
                    lifecycleOwner,
                    onImageCapture = { capture -> imageCapture = capture })
            }


            item { Spacer(modifier = Modifier.height(80.dp)) }
            item {
                PunchActions(
                    punchInValue,
                    punchOutValue,
                    lunchInValue,
                    lunchOutValue,
                    currentDistance,
                    isInsideOffice
                )
            }
            item {
                ActionButtons(
                    punchInValue,
                    punchOutValue,
                    lunchInValue,
                    lunchOutValue,
                    isProcessing,
                    context,
                    fused,
                    imageCapture,
                    coroutineScope,
                    viewModel,
                    onDistanceUpdate = { newDistance -> currentDistance = newDistance },
                    onIsInsideOfficeUpdate = { newIsInside -> isInsideOffice = newIsInside },
                    onShowPunchOutConfirmChange = { showPunchOutConfirm = it },
                    onProcessingChange = { isProcessing = it },
                    isInsideOffice = isInsideOffice,
                    isOfficeWifi = isOfficeWifi


                )
            }

            if (allTeammates.isNotEmpty()) {
                item {
                    ExpandableTeammateRow(
                        teammates = visibleTeammates,
                        remainingCount = remainingCount,
                        isExpanded = isExpanded,
                        onExpandClick = { isExpanded = true })
                }

            }
            item {
                OffTodaySection(viewModel)
            }
             item {
                TeamMatesSection(
                    viewModel = viewModel,
                    onTeammateClick = { teammate ->
                        selectedTeammate = teammate
                    }
                )
            }

            item {
                ExpandableRowSection(
                    title = "Early Birds Today 🏆",
                    list = earlyToday
                )
            }

            item {
                ExpandableRowSection(
                    title = "Monthly Early Bird Ranking 🥇",
                    list = earlyMonthly
                )
            }


            /* if (birthdayTeammates.isNotEmpty()) {
                 item {
                     TeammateSection(
                         title = "Wish Them",
                         teammates = birthdayTeammates,
                         showSeeMoreCount = seeMoreCount
                     )
                 }
             }*/

            /* item { TeammateSection(title = "Off Today", teammates = getOffTodayMates()) }
             item { TeammateSection(title = "My Teammates", teammates = getMyTeammates(), showSeeMoreCount = "+16") }
             item { TeammateSection(title = "Early Bird for the day", teammates = getEarlyBirds()) }
             item { TeammateSection(title = "Early Bird Ranking", teammates = getEarlyBirdRanking()) }*/
        }
    if (selectedTeammate != null) {
        TeamMateDetailsDialog(
            teammate = selectedTeammate!!,
            onDismiss = { selectedTeammate = null }
        )
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }


    // Location updates
    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.lastOrNull()?.let { loc ->
                    val officeLat = PreferenceHelper.getOfficeLat(context)
                    val officeLon = PreferenceHelper.getOfficeLon(context)
                    val officeRadius = PreferenceHelper.getOfficeRadius(context)

                    val distance = FloatArray(1)
                    Location.distanceBetween(
                        loc.latitude, loc.longitude, officeLat, officeLon, distance
                    )
                    currentDistance = distance[0]
                    isInsideOffice = distance[0] <= officeRadius.toFloat()
                }
            }
        }
    }

    DisposableEffect(Unit) {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000).build()

        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fused.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }

        onDispose {
            fused.removeLocationUpdates(locationCallback)
        }
    }
    LaunchedEffect(Unit) {
        while (true) {
            isOfficeWifi = isConnectedToOfficeWifi(
                context,
                officeBssids
            )
            delay(2000) // every 2 seconds
        }
    }

    LaunchedEffect(Unit) {
        permissionsLauncher.launch(
            arrayOf(
                Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
        viewModel.loadTodayAttendance()
        viewModel.loadBirthdays()
        viewModel.loadEarlyBirds()
        viewModel.loadTeamMates()
        coroutineScope.launch {
            while (true) {
                val date = Date()
                currentTime.value = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date)
                currentDate.value =
                    SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault()).format(date)
                delay(1000)
            }
        }
    }
    LaunchedEffect(attendanceResponse) {
        attendanceResponse?.let {

            // ✅ Refresh today's data immediately
            viewModel.loadTodayAttendance()


            // ✅ Clear event so it won't retrigger
            viewModel.clearAttendanceEvent()
        }
    }

    LaunchedEffect(lunchState) {
        lunchState?.let {
            viewModel.loadTodayAttendance()
            viewModel.clearLunchEvent()
        }
    }

    LaunchedEffect(successMessage) {
        successMessage?.let {
            // Show success dialog
            // This will be handled by a stateful dialog below
        }
    }
    if (successMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearSuccessMessage() },
            title = { Text("Success") },
            text = { Text(successMessage ?: "") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearSuccessMessage() }) {
                    Text("OK")
                }
            })
    }

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearErrorMessage() },
            title = { Text("Error") },
            text = { Text(errorMessage ?: "") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearErrorMessage() }) {
                    Text("OK")
                }
            })
    }


    if (showFullImage) {
        FullScreenImageDialog(
            imageUrl = Constants.BASE_URL + (imagePath ?: ""), onDismiss = { showFullImage = false}
        )
    }
}
fun isConnectedToOfficeWifi(context: Context, officeBssids: Set<String>): Boolean {

    // 🔐 Runtime permission check
    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        Log.w("OfficeWiFi", "Location permission not granted")
        return false
    }

    val wifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as android.net.wifi.WifiManager

    val info = wifiManager.connectionInfo ?: return false
    val currentBssid = info.bssid ?: return false

    Log.d("OfficeWiFi", "Current BSSID = $currentBssid")

    return officeBssids.any {
        it.equals(currentBssid, ignoreCase = true)
    }
}



fun alignFace(bitmap: Bitmap, face: com.google.mlkit.vision.face.Face): Bitmap {
    val leftEye = face.getLandmark(com.google.mlkit.vision.face.FaceLandmark.LEFT_EYE)?.position
    val rightEye = face.getLandmark(com.google.mlkit.vision.face.FaceLandmark.RIGHT_EYE)?.position
    if (leftEye != null && rightEye != null) {
        val dx = rightEye.x - leftEye.x
        val dy = rightEye.y - leftEye.y
        val angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
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

fun captureAndVerifyFace(
    loc: Location,
    context: Context,
    imageCapture: ImageCapture?,
    coroutineScope: CoroutineScope,
    viewModel: AttendanceViewModel,
    onDistanceUpdate: (Float) -> Unit,
    onIsInsideOfficeUpdate: (Boolean) -> Unit,
    onError: (String) -> Unit,
    onDone: () -> Unit
) {
    val distance = FloatArray(1)
    val officeLat = PreferenceHelper.getOfficeLat(context)
    val officeLon = PreferenceHelper.getOfficeLon(context)
    val officeRadius = PreferenceHelper.getOfficeRadius(context)

    Location.distanceBetween(
        loc.latitude, loc.longitude, officeLat, officeLon, distance
    )
    val isGpsInside = distance[0] <= officeRadius.toFloat()
    val isOfficeWifi = isConnectedToOfficeWifi(context, setOf(
        "5C:E9:31:E8:8A:5F",
        "5C:E9:31:E8:8A:61"
    ))

    onDistanceUpdate(distance[0])
    onIsInsideOfficeUpdate(isGpsInside || isOfficeWifi)

// FINAL CHECK
    if (!isGpsInside && !isOfficeWifi) {
        onError("You must be inside office or connected to office Wi-Fi")
        onDone()
        return
    }

 /*   onDistanceUpdate(distance[0])
    onIsInsideOfficeUpdate(distance[0] <= officeRadius.toFloat())
    val radius = officeRadius.toFloat()
    if (distance[0] > radius) {
        onError("You are outside office area")
        onDone()
        return
    }*/

    val file = File(context.cacheDir, "${System.currentTimeMillis()}.jpg")
    val output = ImageCapture.OutputFileOptions.Builder(file).build()

    imageCapture?.takePicture(
        output, ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageSavedCallback {
            @SuppressLint("DefaultLocale")
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val image = InputImage.fromFilePath(context, file.toUri())
                val detector = FaceDetection.getClient(
                    FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE).build()
                )

                detector.process(image).addOnSuccessListener { faces ->
                    if (faces.isEmpty()) {
                        onError("No face detected")
                        onDone()
                        return@addOnSuccessListener
                    }

                    coroutineScope.launch(Dispatchers.Main) {}

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
                                cropped, 0, 0, cropped.width, cropped.height, flipMatrix, true
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
                                context.cacheDir, "live_face_${System.currentTimeMillis()}.jpg"
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
                                "login_prefs", Context.MODE_PRIVATE
                            )
                            val imagePath = prefs.getString("image", null)
                            if (imagePath.isNullOrEmpty()) {
                                withContext(Dispatchers.Main) {
                                    onError("Stored face image not found.")
                                    onDone()
                                }
                                return@launch
                            }

                            val storedFile =
                                viewModel.repository.downloadAdminFace(context, imagePath)
                            if (storedFile == null) {
                                withContext(Dispatchers.Main) {
                                    onError("Failed to download stored face.")
                                    onDone()
                                }
                                return@launch
                            }

                            // 9) Crop/normalize stored face (extractFaceRegion does that)
                            val croppedStoredFile = extractFaceRegion(context, storedFile)
                            if (croppedStoredFile == null) {
                                withContext(Dispatchers.Main) {
                                    onError("Failed to process stored face.")
                                    onDone()
                                }
                                return@launch
                            }

                            // 10) Generate embedding for stored face
                            val storedDescriptor = FaceEmbeddingExtractor.generateEmbedding(
                                context, croppedStoredFile
                            )

                            // 11) Compare embeddings
                            val similarity = compareFaces(storedDescriptor, liveDescriptor)
                            Log.d("FaceCompare", "🔍 Similarity score = $similarity")

                            // 12) Decide based on threshold (you can tweak thresholds)
                            withContext(Dispatchers.Main) {
                                if (similarity >= 0.45f) { // Stricter threshold
                                    coroutineScope.launch(Dispatchers.IO) {
                                        viewModel.uploadAttendance(
                                            adminId = viewModel.adminId,
                                            faceDescriptor = liveDescriptor,
                                            imageFile = file,
                                            lat = loc.latitude,
                                            lon = loc.longitude
                                        )
                                    }
                                } else {
                                    onError(
                                        "Face mismatch. Score: ${
                                            String.format(
                                                "%.2f",
                                                similarity
                                            )
                                        }. Please try again."
                                    )
                                }
                                onDone()
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                            coroutineScope.launch(Dispatchers.Main) {
                                onError("An error occurred during face verification.")
                                onDone()
                            }
                        }
                    }
                }.addOnFailureListener {
                    onError("Failed to detect face.")
                    onDone()
                }
            }

            override fun onError(exception: ImageCaptureException) {
                onError("Failed to capture image.")
                onDone()
            }
        })
}


@Composable
fun HomePageHeader(
    currentTime: MutableState<String>,
    currentDate: MutableState<String>,
    imagePath: String?,
    onImageClick: () -> Unit,
    lifecycleOwner: LifecycleOwner,
    onImageCapture: (ImageCapture) -> Unit
) {
    val headerGradient = Brush.verticalGradient(
        colors = listOf(graentlight1, graentDark2)
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp) // Total height for the header and the curve
    ) {
        // Top Green Background with Curve
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp) // Height of the green section
                .background(headerGradient)
                .drawBehind {
                    val path = Path().apply {
                        moveTo(0f, size.height) // Start at bottom-left
                        // This creates a large arc dipping down in the center
                        quadraticTo(
                            x1 = size.width / 2f, // Control point in the middle
                            y1 = size.height + 250f, // Control point pulled way down
                            x2 = size.width, // End at bottom-right
                            y2 = size.height
                        )
                        lineTo(size.width, 0f)
                        lineTo(0f, 0f)
                        close()
                    }
                    drawPath(path, brush = headerGradient)
                }) {
            // Inset Card for the top bar
            // VVV WHERE THE CHANGE IS VVV
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 16.dp, end = 16.dp, top = 16.dp
                    ), // Padding applied to the Card itself
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp) // Set a proper height for the Row
                        .padding(horizontal = 16.dp), // Padding inside the row
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // ... (Content of the Row is correct)
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = Constants.BASE_URL + (imagePath ?: "")
                        ),
                        contentDescription = "User Avatar",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White, CircleShape)
                            .clickable { onImageClick() })
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            currentTime.value,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            currentDate.value,
                            fontSize = 13.sp,
                            color = Color.Black.copy(alpha = 0.8f)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_bell),
                            contentDescription = "Notifications",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
        // Central placeholder
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(250.dp) // Increased size to 200.dp
                .offset(y = (80).dp)
                .background(Color.White, CircleShape)
                .border(6.dp, graentDark2, CircleShape), contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).also { previewView ->
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            val capture =
                                ImageCapture.Builder().setTargetResolution(Size(640, 480)).build()
                            val selector = CameraSelector.DEFAULT_FRONT_CAMERA
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner, selector, preview, capture
                            )
                            onImageCapture(capture)
                        }, ContextCompat.getMainExecutor(ctx))
                    }
                }, modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        }
    }
}

@Composable
fun PunchActions(
    punchInValue: String,
    punchOutValue: String,
    lunchInValue: String,
    lunchOutValue: String,
    currentDistance: Float?,
    isInsideOffice: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            PunchInfo(
                punchInValue, "Punch In", painterResource(id = R.drawable.ic_clock)
            )
            PunchInfo(
                lunchInValue, "Lunch Start", painterResource(id = R.drawable.ic_clock)
            )
            PunchInfo(lunchOutValue, "Lunch End", painterResource(id = R.drawable.ic_clock))
            PunchInfo(punchOutValue, "Punch Out", painterResource(id = R.drawable.ic_clock))
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (currentDistance != null) {
            val distanceText = "You are %.1f meters away from the office".format(currentDistance)
            val textColor = if (isInsideOffice) Color.Green else Color.Red
            Text(text = distanceText, color = textColor)
        } else {
            Text(text = "Calculating distance...", color = Color.Gray)
        }
    }
}

@Composable
fun PunchInfo(time: String, label: String, painterResource: Painter) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // VVV FIX THE ICON HERE VVV
        Icon(
            painter = painterResource, contentDescription = label, tint = Color.Gray
        )
        // ^^^ FIX THE ICON HERE ^^^
        Spacer(modifier = Modifier.height(4.dp))
        Text(time, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
        Text(label, fontSize = 11.sp, color = Color.Black)
    }
}

@Composable
fun ActionButtons(
    punchInValue: String,
    punchOutValue: String,
    lunchInValue: String,
    lunchOutValue: String,
    isProcessing: Boolean,
    context: Context,
    fused: FusedLocationProviderClient,
    imageCapture: ImageCapture?,
    coroutineScope: CoroutineScope,
    viewModel: AttendanceViewModel,
    onDistanceUpdate: (Float) -> Unit,
    onIsInsideOfficeUpdate: (Boolean) -> Unit,
    onShowPunchOutConfirmChange: (Boolean) -> Unit,
    onProcessingChange: (Boolean) -> Unit,
    isInsideOffice: Boolean,
    isOfficeWifi: Boolean
) {
    val lunchButtonText =
        if (lunchInValue != "--" && lunchOutValue == "--") "Lunch End" else "Lunch Start"
    val punchButtonText = if (punchInValue != "--" && punchOutValue == "--") "Punch Out" else "Punch In"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val canPunch = isInsideOffice || isOfficeWifi

        Button(
            enabled = imageCapture != null && !isProcessing && canPunch,
            onClick = {

                Log.d("PunchIn", "Punch In clicked")

                // Prevent double click
                if (isProcessing) return@Button

                // Punch-out confirm
                if (punchInValue != "--" && punchOutValue == "--") {
                    onShowPunchOutConfirmChange(true)
                    return@Button
                }

                // Location permission check
                if (ActivityCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    viewModel.setErrorMessage("Location permission required")
                    return@Button
                }

                onProcessingChange(true)
                // Location fetch
                fused.lastLocation.addOnSuccessListener { loc ->

                        if (loc == null) {
                            viewModel.setErrorMessage("Unable to fetch location")
                            onProcessingChange(false)
                            return@addOnSuccessListener
                        }

                        Log.d("PunchIn", "Location received")

                        captureAndVerifyFace(
                            loc = loc,
                            context = context,
                            imageCapture = imageCapture,
                            coroutineScope = coroutineScope,
                            viewModel = viewModel,
                            onDistanceUpdate = onDistanceUpdate,
                            onIsInsideOfficeUpdate = onIsInsideOfficeUpdate,
                            onDone = {
                                onProcessingChange(false)
                            },
                            onError = { msg ->
                                viewModel.setErrorMessage(msg)
                            })
                    }.addOnFailureListener {
                        viewModel.setErrorMessage("Location error")
                        onProcessingChange(false)
                    }
            },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB7DB4F)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(punchButtonText, color = Color.Black)
        }

        Button(
            enabled = punchInValue != "--" && punchOutValue == "--" && !isProcessing && canPunch,
            onClick = {
                if (isProcessing) return@Button

                if (ActivityCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    viewModel.setErrorMessage("Location permission required")
                    return@Button
                }

                onProcessingChange(true)
                fused.lastLocation.addOnSuccessListener { loc ->
                    if (loc == null) {
                        viewModel.setErrorMessage("Unable to fetch location")
                        onProcessingChange(false)
                        return@addOnSuccessListener
                    }

                    val distance = FloatArray(1)
                    val officeLat = PreferenceHelper.getOfficeLat(context)
                    val officeLon = PreferenceHelper.getOfficeLon(context)
                    val officeRadius = PreferenceHelper.getOfficeRadius(context)

                    Location.distanceBetween(
                        loc.latitude, loc.longitude, officeLat, officeLon, distance
                    )
                    val isGpsInside = distance[0] <= officeRadius.toFloat()
                    val isWifiConnected = isConnectedToOfficeWifi(context, setOf("5C:E9:31:E8:8A:5F", "5C:E9:31:E8:8A:61"))

                    onDistanceUpdate(distance[0])
                    onIsInsideOfficeUpdate(isGpsInside || isWifiConnected)

                    if (isGpsInside || isWifiConnected) {
                        if (punchInValue != "--" && punchOutValue == "--") {
                            viewModel.takeLunchBreak()
                        } else {
                            viewModel.setErrorMessage("You need to punch in first.")
                        }
                    } else {
                        viewModel.setErrorMessage("You must be inside office or connected to office Wi-Fi")
                    }
                    onProcessingChange(false)
                }.addOnFailureListener {
                    viewModel.setErrorMessage("Location error")
                    onProcessingChange(false)
                }
            },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF9C80E)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(lunchButtonText, color = Color.Black)
        }
    }
}


@Composable
fun ExpandableTeammateRow(
    teammates: List<Teammate>,
    remainingCount: Int,
    isExpanded: Boolean,
    onExpandClick: () -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = "Wish Them",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = Color(0xFF0B1500)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {

            items(teammates) { teammate ->
                TeammateItem(teammate)
            }

            if (!isExpanded && remainingCount > 0) {
                item {
                    ExpandMoreBubble(
                        count = remainingCount, onClick = onExpandClick
                    )
                }
            }
        }
    }
}

@Composable
fun OffTodaySection(viewModel: AttendanceViewModel) {

    val offTodayList by viewModel.offTodayTeammates.collectAsState()
    var isExpanded by remember { mutableStateOf(false) }

    val maxVisible = 5
    val visibleList = if (isExpanded) offTodayList else offTodayList.take(maxVisible)
    val remainingCount = offTodayList.size - maxVisible

    LaunchedEffect(Unit) {
        viewModel.loadOffToday()
    }

    if (offTodayList.isNotEmpty()) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {

            Text(
                text = "Off Today",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = Color(0xFF0B1500)
            )

            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                items(visibleList) { teammate ->
                    TeammateItem(teammate)
                }

                if (!isExpanded && remainingCount > 0) {
                    item {
                        ExpandMoreBubble(
                            count = remainingCount, onClick = { isExpanded = true })
                    }
                }
            }
        }
    }
}
@Composable
fun TeamMatesSection(
    viewModel: AttendanceViewModel,
    onTeammateClick: (TeamMember) -> Unit
) {
    val teamMates by viewModel.teamMates.collectAsState()
    var isExpanded by remember { mutableStateOf(false) }

    val maxVisible = 5
    val visibleList = if (isExpanded) teamMates else teamMates.take(maxVisible)
    val remainingCount = teamMates.size - maxVisible

    LaunchedEffect(Unit) {
        viewModel.loadTeamMates()
    }

    if (teamMates.isNotEmpty()) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = "Team Mates",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = Color(0xFF0B1500)
            )
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(visibleList) { teammate ->
                    TeamMateItem(teammate = teammate, onClick = { onTeammateClick(teammate) })
                }
                if (!isExpanded && remainingCount > 0) {
                    item {
                        ExpandMoreBubble(
                            count = remainingCount,
                            onClick = { isExpanded = true }
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun TeamMateItem(teammate: TeamMember, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(64.dp)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = "${Constants.BASE_URL}/${teammate.details.faceDescriptor}",
            contentDescription = teammate.name,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.face_scan),
            error = painterResource(R.drawable.face_scan)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            teammate.name,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            color = Color.Black
        )
    }
}

@Composable
fun TeamMateDetailsDialog(teammate: TeamMember, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = "${Constants.BASE_URL}/${teammate.details.faceDescriptor}",
                    contentDescription = teammate.name,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.face_scan),
                    error = painterResource(R.drawable.face_scan)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(teammate.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(teammate.details.designation ?: "No designation", fontSize = 16.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Employee ID: ${teammate.details.empId}", fontSize = 14.sp)
                Text("Phone: ${teammate.phone}", fontSize = 14.sp)
                Text("Joining Date: ${teammate.details.joiningDate}", fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    }
}



@Composable
fun ExpandMoreBubble(
    count: Int, onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(Color(0xFFE9F7C8)) // light green
            .clickable { onClick() }, contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+$count",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4A7C00)
        )
    }
}



@Composable
fun ExpandableRowSection(
    title: String,
    list: List<EarlyTeammate>,
    maxVisible: Int = 5
) {
    var expanded by remember { mutableStateOf(false) }

    val visibleList = if (expanded) list else list.take(maxVisible)
    val remaining = list.size - maxVisible

    if (list.isNotEmpty()) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {

            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = Color.Black
            )

            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                items(visibleList) { teammate ->
                    EarlyBirdItem(teammate)
                }

                if (!expanded && remaining > 0) {
                    item {
                        ExpandMoreBubble(count = remaining) {
                            expanded = true
                        }
                    }
                }
            }
        }
    }


}
@Composable
fun EarlyBirdItem(teammate: EarlyTeammate) {
    Column(
        modifier = Modifier.width(90.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        AsyncImage(
            model = Constants.BASE_URL + (teammate.imageUrl ?: ""),
            contentDescription = teammate.name,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.face_scan),
            error = painterResource(R.drawable.face_scan)
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = teammate.name,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            color = Color.Black
        )

        Text(
            text = teammate.subText,
            fontSize = 11.sp,
            color = Color.Black
        )
    }
}






@Composable
fun TeammateItem(teammate: Teammate) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(64.dp)
    ) {

        AsyncImage(
            model = "${Constants.BASE_URL}/${teammate.imageUrl}",
            contentDescription = teammate.name,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )



        Spacer(modifier = Modifier.height(4.dp))

        Text(
            teammate.name,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            color = Color.Black
        )

        Text(
            teammate.birthdayText, fontSize = 11.sp, color = Color.Gray, maxLines = 1
        )
    }
}
