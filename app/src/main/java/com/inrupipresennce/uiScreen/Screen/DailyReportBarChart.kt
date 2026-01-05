package com.inrupipresennce.uiScreen.Screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inrupipresennce.ui.presence.PresenceCalendarViewModel
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter

@Composable
fun DailyReportBarChart(viewModel: PresenceCalendarViewModel) {
    val records by viewModel.presenceRecords.collectAsState()
    val dailyReports = viewModel.getDailyWorkReport(records)

    val today = LocalDate.now()



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
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.width(40.dp)
                ) {
                    // 🟩 / 🟥 Bar
                    Box(
                        modifier = Modifier
                            .width(24.dp)
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
    }
}
