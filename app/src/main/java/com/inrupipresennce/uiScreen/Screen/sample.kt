package com.inrupipresennce.uiScreen.Screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth
import kotlin.random.Random

data class CalendarDay(
    val date: LocalDate,
    val isCurrentMonth: Boolean,
    val events: List<String> = emptyList()
)
fun generateMonthDays(yearMonth: YearMonth): List<CalendarDay> {
    val firstDayOfMonth = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7  // Sunday = 0

    val totalCells = ((daysInMonth + firstDayOfWeek + 6) / 7) * 7 // Round to full weeks

    val startDate = firstDayOfMonth.minusDays(firstDayOfWeek.toLong())

    return (0 until totalCells).map { offset ->
        val date = startDate.plusDays(offset.toLong())
        CalendarDay(
            date = date,
            isCurrentMonth = date.month == yearMonth.month,
            events = if (Random.nextBoolean() && date.month == yearMonth.month)
                listOf("Event") else emptyList()
        )
    }
}
@Composable
fun CustomCalendarScreen() {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val days = remember(currentMonth) { generateMonthDays(currentMonth) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        // --- Header with Month / Year and arrows ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous", tint = Color.White)
            }
            Text(
                text = "${currentMonth.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${currentMonth.year}",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )
            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Weekday Headers ---
        val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            daysOfWeek.forEach {
                Text(text = it, color = Color.Gray, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- Grid of Days ---
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(days) { day ->
                CalendarDayCell(day = day)
            }
        }
    }
}
@Composable
fun CalendarDayCell(day: CalendarDay) {
    val isToday = day.date == LocalDate.now()

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    isToday -> Color(0xFF4A90E2)
                    day.isCurrentMonth -> Color(0xFF1E1E1E)
                    else -> Color(0xFF2C2C2C)
                }
            )
            .clickable { /* TODO: handle day click */ },
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier.padding(top = 6.dp)
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                color = if (isToday) Color.White else Color(0xFFDADADA),
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Show small event dots
            day.events.take(3).forEach { _ ->
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFC107))
                )
                Spacer(modifier = Modifier.height(2.dp))
            }
        }
    }
}





@Preview(showBackground = true)
@Composable
fun CalendarScreenPreview() {
}




