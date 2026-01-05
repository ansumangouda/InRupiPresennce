package com.inrupipresennce.ui.presence

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.inrupipresennce.data.model.PresenceRecord
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.ceil

private val dayBoxBackgroundColor = Color(0xFFE6F4E6)
private val dayBoxBorderColor = Color(0xFFA5D6A7)
private val sundayBackgroundColor = Color(0xFFFFEBEE)
private val sundayBorderColor = Color(0xFFE57373)
private val selectedBorderColor = Color(0xFF673AB7)

@Composable
fun PresenceCalendarScreen(viewModel: PresenceCalendarViewModel, context: Context) {

    val records by viewModel.presenceRecords.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentYearMonth by viewModel.currentYearMonth.collectAsState()

    Box(Modifier.fillMaxSize()) {
        PresenceMonthCalendar(
            records = records,
            currentMonth = currentYearMonth,
            onPreviousMonth = { viewModel.goToPreviousMonth() },
            onNextMonth = { viewModel.goToNextMonth() }
        )
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun PresenceMonthCalendar(
    records: List<PresenceRecord>,
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }

    val imageMap = remember(records, currentMonth) {
        records.associateBy {
            LocalDate.parse(it.punch_in_at.substring(0, 10))
        }
    }

    val firstDay = currentMonth.atDay(1)
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfWeek = (firstDay.dayOfWeek.value % 7)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPreviousMonth,
                modifier = Modifier
                    .size(36.dp)
                    .background(dayBoxBackgroundColor, CircleShape)
                    .border(1.dp, dayBoxBorderColor, CircleShape)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous Month",
                    tint = Color.DarkGray
                )
            }

            Text(
                text = "${
                    currentMonth.month.name.lowercase().replaceFirstChar { it.titlecase() }
                } ${currentMonth.year}",
                fontWeight = FontWeight.Bold,
                fontSize = MaterialTheme.typography.titleLarge.fontSize
            )

            IconButton(
                onClick = onNextMonth,
                modifier = Modifier
                    .size(36.dp)
                    .background(dayBoxBackgroundColor, CircleShape)
                    .border(1.dp, dayBoxBorderColor, CircleShape)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next Month",
                    tint = Color.DarkGray
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                Box(
                    modifier = Modifier.weight(1f), contentAlignment = Alignment.Center
                ) {
                    Text(day, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        val weeks = ceil((firstDayOfWeek + daysInMonth) / 7.0).toInt()
        Column(
            modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            repeat(weeks) { weekIndex ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    repeat(7) { dayIndexInWeek ->
                        Box(
                            modifier = Modifier.weight(1f), contentAlignment = Alignment.Center
                        ) {
                            val dayOfMonth = (weekIndex * 7 + dayIndexInWeek) - firstDayOfWeek + 1
                            if (dayOfMonth in 1..daysInMonth) {
                                val date = currentMonth.atDay(dayOfMonth)
                                val record = imageMap[date]
                                DayImageBox(
                                    date = date,
                                    imageUrl = record?.punch_in_image,
                                    isSelected = date == selectedDate,
                                    onDateSelected = { selectedDate = it })
                            } else {
                                Box(modifier = Modifier.size(40.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DayImageBox(
    date: LocalDate, imageUrl: String?, isSelected: Boolean, onDateSelected: (LocalDate) -> Unit
) {
    val isSunday = date.dayOfWeek.value == 7
    var showPopup by remember { mutableStateOf(false) }

    val backgroundColor = when {
        isSunday -> sundayBackgroundColor
        else -> dayBoxBackgroundColor
    }

    val borderColor = when {
        isSelected -> selectedBorderColor
        isSunday -> sundayBorderColor
        else -> dayBoxBorderColor
    }

    val borderWidth = if (isSelected) 2.dp else 1.dp

    Box(
        modifier = Modifier
            .size(40.dp)
            .shadow(elevation = 4.dp, shape = CircleShape)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(borderWidth, borderColor, CircleShape)
            .clickable {
                onDateSelected(date)
                if (imageUrl != null && !isSunday) {
                    showPopup = true
                }
            }, contentAlignment = Alignment.Center
    ) {
        when {
            isSunday -> {
                Text(
                    text = date.dayOfMonth.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
            }

            imageUrl != null -> {
                Image(
                    painter = rememberAsyncImagePainter(imageUrl),
                    contentDescription = "Presence Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            else -> {
                Text(
                    text = date.dayOfMonth.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
            }
        }
    }

    if (showPopup && imageUrl != null) {
        Dialog(onDismissRequest = { showPopup = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White), contentAlignment = Alignment.Center
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
