package com.inrupipresennce.uiScreen.Screen


import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.inrupipresennce.data.valu.Constants
import com.inrupipresennce.ui.presence.PresenceCalendarScreen
import com.inrupipresennce.ui.presence.PresenceCalendarViewModel
import com.inrupipresennce.uiScreen.ViewModelFactory.PresenceCalendarViewModelFactory
import com.inrupipresennce.utils.FullScreenImageDialog
import com.inrupipresennce.utils.PreferenceHelper

import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter


@Composable
fun CalendarScreen(navController: NavHostController) {
    AttendanceOverviewScreen(navController)
}

@Composable
fun AttendanceOverviewScreen(navController: NavHostController) {
    val context = LocalContext.current
    // ✅ Use single shared ViewModel
    val viewModel: PresenceCalendarViewModel = viewModel(
        factory = PresenceCalendarViewModelFactory(context),
        key = "SharedPresenceVM"
    )

    val records by viewModel.presenceRecords.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()


    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val summary = viewModel.calculateAttendanceSummary(records)
    var showFullImage by remember { mutableStateOf(false) }


    val donutData = listOf(
        "Working Days" to summary.remainingDays.toFloat(),
        "On Time" to summary.onTimeDays.toFloat(),
        "Late" to summary.lateDays.toFloat(),
        "Late" to summary.presentDays.toFloat(),
        "Absent" to summary.absentDays.toFloat()
    )

    val donutColors = listOf(
        Color(0xFF4285F4),
        Color(0xFF26C6DA),
        Color(0xFFE57373),
        Color(0xFFFFC107),
        Color(0xFFFF0707)
    )
    val imagePath = PreferenceHelper.getImagePath(context)



    Box(modifier = Modifier.fillMaxSize()) {

        // TOP BAR AREA
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = WindowInsets.statusBars
                        .asPaddingValues()
                        .calculateTopPadding() + 12.dp,
                    start = 16.dp,
                    end = 16.dp
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Image(
                    painter = rememberAsyncImagePainter(Constants.BASE_URL + (imagePath ?: "")),
                    contentDescription = "Profile",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .clickable { showFullImage = true }
                )
            }
        }

        // MAIN CONTENT BELOW IMAGE
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 120.dp, start = 16.dp, end = 16.dp), // << clean padding
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

             item {
                DonutChart(
                    data = donutData.map { it.second },
                    colors = donutColors,
                    centerText = "Overview",
                    context = context
                )
            }

            item { Spacer(Modifier.height(24.dp)) }

            item { OverviewInfoGrid(summary) }

            item { Spacer(Modifier.height(24.dp)) }

            item { PresenceCalendarScreen(viewModel, context) }

            item { Spacer(Modifier.height(24.dp)) }

            item { DailyReportBarChart(viewModel) }
        }

        if (showFullImage) {
            FullScreenImageDialog(
                imageUrl = Constants.BASE_URL + (imagePath ?: ""),
                onDismiss = { showFullImage = false }
            )
        }
    }

}


@Composable
fun DonutChart(
    data: List<Float>,
    colors: List<Color>,
    centerText: String,
    context: Context
) {
    val total = data.sum()
    if (total <= 0f) {
        // nothing to draw
        Box(modifier = Modifier.size(260.dp), contentAlignment = Alignment.Center) {
            Text(centerText, fontWeight = FontWeight.Bold)
        }
        return
    }

    // compute sweep angles
    val sweepAngles = data.map { 360f * (it / total) }

    // configuration: tune these to change look
    val canvasSizeDp = 260.dp
    val baseInnerRadiusRatio = 0.32f // proportion of box used as inner hole (0..0.5). smaller => bigger hole
    val maxStrokePx = 132f // maximum stroke in px for the thickest ring
    val minStrokePx = 78f // minimum stroke in px for thinnest ring

    // auto-calc stroke widths across slices (outermost slice first)
    val count = data.size.coerceAtLeast(1)
    val strokeWidths = (0 until count).map { idx ->
        // distribute stroke widths from max->min
        val t = 1f - idx.toFloat() / (count - 1).coerceAtLeast(1) // 1..0
        (minStrokePx + t * (maxStrokePx - minStrokePx))
    }



        Box(
            modifier = Modifier.size(canvasSizeDp),
            contentAlignment = Alignment.Center
        ) {


            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasMin = size.minDimension
                val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)

                // innerRadius in pixels (actual inside hole radius)
                val innerRadius = canvasMin * baseInnerRadiusRatio / 2f

                var startAngle = 135f

                for (i in data.indices) {
                    val sweep = sweepAngles[i]
                    val stroke = strokeWidths.getOrElse(i) { minStrokePx }
                    val strokeWidth = stroke.coerceAtLeast(1f)

                    // centerline radius for this stroke: innerRadius + strokeWidth/2
                    val centerlineRadius = innerRadius + strokeWidth / 2f

                    // size for drawArc uses diameter = centerlineRadius * 2
                    val diameter = centerlineRadius * 2f

                    val topLeft = androidx.compose.ui.geometry.Offset(
                        center.x - centerlineRadius,
                        center.y - centerlineRadius
                    )

                    // draw arc with stroke centered on circle with radius `centerlineRadius`
                    drawArc(
                        color = colors[i % colors.size],
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = androidx.compose.ui.geometry.Size(diameter, diameter),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                    )

                    // label position: halfway through stroke thickness from inner edge
                    val midAngle = startAngle + sweep / 2f
                    val angleRad = Math.toRadians(midAngle.toDouble())
                    val labelRadius =
                        innerRadius + strokeWidth * 0.5f // in px; roughly center of stroke
                    val labelX = (center.x + kotlin.math.cos(angleRad) * labelRadius).toFloat()
                    val labelY = (center.y + kotlin.math.sin(angleRad) * labelRadius).toFloat()

                    drawContext.canvas.nativeCanvas.apply {
                        drawText(
                            data[i].toInt().toString(),
                            labelX,
                            labelY + 10f,
                            android.graphics.Paint().apply {
                                textAlign = android.graphics.Paint.Align.CENTER
                                textSize = 28f
                                color = android.graphics.Color.BLACK
                                isFakeBoldText = true
                            }
                        )
                    }

                    startAngle += sweep
                }
            }

            // center label
            Text(
                text = centerText,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

}




@Composable
fun OverviewInfoGrid(summary: PresenceCalendarViewModel.AttendanceSummary) {
    // Same order as donut chart
    val infoColors = listOf(
        Color(0xFF4285F4), // Working Days
        Color(0xFF26C6DA), // On Time
        Color(0xFFE57373), // Late
        Color(0xFFFFC107), // Present
        Color(0xFFFF0707), // Optional extra (e.g. Left Timely / On Leave) Absent
        Color(0xFF4CAF50)
    )

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            InfoCard(
                value = summary.remainingDays.toString(),
                label = "Working Days",
                color = infoColors[0],
                modifier = Modifier.weight(1f)
            )
            InfoCard(
                value = summary.onTimeDays.toString(),
                label = "On Time",
                color = infoColors[1],
                modifier = Modifier.weight(1f)
            )
            InfoCard(
                value = summary.lateDays.toString(),
                label = "Late",
                color = infoColors[2],
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            InfoCard(
                value = summary.presentDays.toString(),
                label = "Present",
                color = infoColors[3],
                modifier = Modifier.weight(1f)
            )
            InfoCard(
                value = summary.absentDays.toString(),
                label = "Absent",
                color = infoColors[4], // can reuse blue or choose unique color
                modifier = Modifier.weight(1f)
            )
        }
    }
}


@Composable
fun InfoCard(
    value: String,
    label: String,
    color: Color, // 👈 new parameter
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(4.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Value + Label
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color.Black
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )

        // 🎨 Colored bottom line
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .height(3.dp)
                .fillMaxWidth(0.8f)
                .background(color, RoundedCornerShape(12.dp))
        )
    }
}



@Composable
fun DailyReportBarChart(viewModel: PresenceCalendarViewModel) {
    val records by viewModel.presenceRecords.collectAsState()
    val dailyReports = viewModel.getDailyWorkReport(records)

    val today = LocalDate.now()



    if (dailyReports.isEmpty()) {
        Text("No attendance data available", color = Color.Gray)
        return
    }

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

// ✅ Group by (year, month, weekOfMonth)
    val weeklyGrouped = dailyReports.groupBy { report ->
        val date = LocalDate.parse(report.date, formatter)
        val firstDayOfMonth = date.withDayOfMonth(1)
        val dayOfWeekOffset = (firstDayOfMonth.dayOfWeek.value % 7)
        val weekOfMonth = ((date.dayOfMonth + dayOfWeekOffset - 1) / 7) + 1
        Triple(date.year, date.monthValue, weekOfMonth)
    }

// ✅ Sort chronologically by year, month, week
    val sortedWeeks = weeklyGrouped
        .toSortedMap(compareBy({ it.first }, { it.second }, { it.third }))
        .toList()
    val currentWeekIndexInit = sortedWeeks.indexOfFirst { (key, _) ->
        val (year, month, weekOfMonth) = key
        year == today.year &&
                month == today.monthValue &&
                weekOfMonth == ((today.dayOfMonth - 1) / 7) + 1
    }.coerceAtLeast(0)  // fallback to 0 if not found

    var currentWeekIndex by remember {
        mutableStateOf(currentWeekIndexInit)
    }

 //   var currentWeekIndex by remember { mutableStateOf(0) }
    val currentWeekEntry = sortedWeeks.getOrNull(currentWeekIndex)
    val currentWeek = currentWeekEntry?.second ?: emptyList()

// ✅ Accurate label
    val (year, monthNum, weekOfMonth) = currentWeekEntry?.first ?: Triple(0, 0, 0)
    val monthName = if (monthNum in 1..12)
        Month.of(monthNum).name.lowercase().replaceFirstChar { it.titlecase() }
    else "This Month"

    val weekLabel = when (weekOfMonth) {
        1 -> "1st Week"
        2 -> "2nd Week"
        3 -> "3rd Week"
        4 -> "4th Week"
        5 -> "5th Week"
        else -> "${weekOfMonth}th Week"
    }

    // 🧱 UI Layout
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 📅 Header with navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { if (currentWeekIndex > 0) currentWeekIndex-- },
                enabled = currentWeekIndex > 0
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Week")
            }

            Text(
                text = "$monthName - $weekLabel",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            IconButton(
                onClick = { if (currentWeekIndex < sortedWeeks.size - 1) currentWeekIndex++ },
                enabled = currentWeekIndex < sortedWeeks.size - 1
            ) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Next Week")
            }
        }


        Spacer(Modifier.height(12.dp))

        // 📊 Bar Chart (7 days max)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            currentWeek.forEach { report ->
                val barColor = if (report.hasPunchOut) Color(0xFF4CAF50) else Color(0xFFE57373)
                val height = if (report.hasPunchOut)
                    (report.hoursWorked * 15).coerceAtLeast(8f)
                else 8f

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    // 🟩 / 🟥 Bar
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .height(height.dp)
                            .background(barColor, RoundedCornerShape(4.dp))
                    )

                    Spacer(Modifier.height(4.dp))

                    // 📅 Day (number only)
                    Text(
                        text = report.date.substring(8, 10),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    // ⏱️ Working hours
                    Text(
                        text = if (report.hasPunchOut)
                            String.format("%.1fh", report.hoursWorked)
                        else "--",
                        fontSize = 10.sp,
                        color = if (report.hasPunchOut) colorScheme.onBackground else Color.Red,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // 🔰 Legend
        Text(
            text = "🟩 Completed  |  🟥 Missing Punch-Out",
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}



@Preview
@Composable
fun pre(){
    CalendarScreen(navController = NavHostController(LocalContext.current))
}
