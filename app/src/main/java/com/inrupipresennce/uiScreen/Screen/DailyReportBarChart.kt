package com.inrupipresennce.uiScreen.Screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inrupipresennce.data.model.PresenceRecord
import com.inrupipresennce.ui.presence.PresenceCalendarViewModel
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter

@Composable
fun DailyReportBarChart(viewModel: PresenceCalendarViewModel) {
    val records by viewModel.presenceRecords.collectAsState()
    val dailyReports = viewModel.getDailyWorkReport(records)

    val today = LocalDate.now()
    val selectedDate by viewModel.selectedDate.collectAsState()




    if (dailyReports.isEmpty()) {
        Text("No attendance data available", color = Color.Gray, modifier = Modifier.padding(16.dp))
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
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
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Week")
            }
        }


        Spacer(Modifier.height(16.dp))



        // ✅ CAPSULE DAY UI
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(currentWeek.size) { index ->
                val report = currentWeek[index]
                val date = LocalDate.parse(report.date, formatter)

                DayCapsule(
                    dayNumber = date.dayOfMonth.toString().padStart(2, '0'),
                    dayName = date.dayOfWeek.name.take(3)
                        .lowercase()
                        .replaceFirstChar { it.titlecase() },
                    hoursWorked = report.hoursWorked,
                    hasPunchOut = report.hasPunchOut,
                    isSelected = report.date == selectedDate,
                    onClick = {
                        viewModel.selectDate(report.date)   // highlight capsule
                        val detail = viewModel.getDayDetail(report.date, records)
                        viewModel.showDayDetail(detail)
                    }

                )
            }
        }
        val dayDetail by viewModel.selectedDayDetail.collectAsState()

        dayDetail?.let {
            DayDetailDialog(
                detail = it,
                onDismiss = { viewModel.dismissDayDetail() }
            )
        }

    }
}
@Composable
fun DayDetailDialog(
    detail: PresenceCalendarViewModel.DayDetail,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

                Text(
                    text = "Attendance Details",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                DetailRow("Date", detail.date)
                DetailRow("Punch In", formatTime(detail.punchIn))
                DetailRow("Punch Out", formatTime(detail.punchOut))

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Close",
                    modifier = Modifier
                        .align(Alignment.End)
                        .background(Color(0xFF8BC34A), RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable { onDismiss() },
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

fun formatTime(dateTime: String?): String {
    return if (dateTime.isNullOrBlank()) "--"
    else dateTime.substring(11, 16) // HH:mm
}




@Composable
fun DayCapsule(
    dayNumber: String,
    dayName: String,
    hoursWorked: Float,
    hasPunchOut: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = when {
        isSelected -> colorScheme.primary
        hasPunchOut -> Color(0xFFF8FBFF)
        else -> Color(0xFFF8FBFF)
    }

    val textColor = if (isSelected) colorScheme.onPrimary else Color(0xFF2E3A1F)

    Box(
        modifier = Modifier
            .width(50.dp)
            .height(90.dp)
            .shadow(if (isSelected) 12.dp else 4.dp, RoundedCornerShape(40.dp))
            .background(bgColor, RoundedCornerShape(40.dp))
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    )
    {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(dayNumber, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textColor)
            Text(dayName, fontSize = 9.sp, fontWeight = FontWeight.Medium, color = textColor)
            Text(
                text = if (hasPunchOut) String.format("%.1f hr", hoursWorked) else "--",
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (hasPunchOut) textColor else Color.Red
            )
        }
    }
}
