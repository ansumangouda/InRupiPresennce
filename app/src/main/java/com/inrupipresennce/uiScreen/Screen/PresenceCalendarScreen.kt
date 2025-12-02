package com.inrupipresennce.ui.presence

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.inrupipresennce.data.api.model.PresenceRecord
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.ceil

@Composable
fun PresenceCalendarScreen(viewModel: PresenceCalendarViewModel, context: Context) {

    val records by viewModel.presenceRecords.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()



    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        PresenceMonthCalendar(records = records)
    }
}

@Composable
fun PresenceMonthCalendar(records: List<PresenceRecord>) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    val imageMap = remember(records) {
        records.associateBy {
            LocalDate.parse(it.punch_in_at.substring(0, 10))
        }
    }

    val firstDay = currentMonth.atDay(1)
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfWeek = (firstDay.dayOfWeek.value % 7)
    val weeks = ceil((daysInMonth + firstDayOfWeek) / 7.0).toInt()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 🗓️ Header with next/previous buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
            }

            Text(
                text = "${currentMonth.month.name.lowercase().replaceFirstChar { it.titlecase() }} ${currentMonth.year}",
                fontWeight = FontWeight.Bold,
                fontSize = MaterialTheme.typography.titleLarge.fontSize
            )

            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Next Month")
            }
        }

        Spacer(Modifier.height(12.dp))

        // Week headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                Text(day, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(Modifier.height(8.dp))

        // Calendar grid
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var day = 1
            repeat(weeks) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    repeat(7) { column ->
                        val currentDay = day - firstDayOfWeek + column
                        if (currentDay in 1..daysInMonth) {
                            val date = currentMonth.atDay(currentDay)
                            val record = imageMap[date]
                            DayImageBox(date = date, imageUrl = record?.punch_in_image)
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                            )
                        }
                    }
                    day += 7
                }
            }
        }
    }
}


@Composable
fun DayImageBox(
    date: LocalDate,
    imageUrl: String?
) {
    val isSunday = date.dayOfWeek.value == 7
    var showPopup by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(48.dp)
            .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
            .clip(RoundedCornerShape(4.dp))
            .background(
                if (isSunday) Color(0xFFFFEBEE) else Color.White, // 🟥 light red for Sunday
                RoundedCornerShape(4.dp)
            )
            .clickable(enabled = imageUrl != null && !isSunday) {
                // 👆 open popup only if image exists and not Sunday
                showPopup = true
            },
        contentAlignment = Alignment.Center
    ) {
        when {
            isSunday -> {
                // 🟥 Sunday box — red date text
                Text(
                    text = date.dayOfMonth.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
            }

            imageUrl != null -> {
                // 📸 Show attendance image
                Image(
                    painter = rememberAsyncImagePainter(imageUrl),
                    contentDescription = "Presence Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            else -> {
                // 🗓️ Normal day — show date text
                Text(
                    text = date.dayOfMonth.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
            }
        }
    }

    // 🖼️ Popup Dialog for Full Image
    if (showPopup && imageUrl != null) {
        Dialog(onDismissRequest = { showPopup = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberAsyncImagePainter(imageUrl),
                    contentDescription = "Full Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                IconButton(
                    onClick = { showPopup = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(28.dp)
                        .background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(50))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.Black
                    )
                }
            }
        }
    }
}


