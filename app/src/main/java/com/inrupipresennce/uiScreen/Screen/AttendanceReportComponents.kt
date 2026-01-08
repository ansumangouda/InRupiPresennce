
package com.inrupipresennce.uiScreen.Screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.inrupipresennce.R
import com.inrupipresennce.data.valu.Constants
import com.inrupipresennce.ui.presence.PresenceCalendarViewModel
import com.inrupipresennce.ui.theme.borderBlue
import com.inrupipresennce.ui.theme.graentDark2
import com.inrupipresennce.ui.theme.graentlight1
import com.inrupipresennce.ui.theme.lightBlue
import com.inrupipresennce.ui.theme.topBorderblueDark
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun TopAppBar(imagePath: String?, onProfileClick: () -> Unit) {
    var currentTime by remember { mutableStateOf(LocalTime.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            delay(1000)
        }
    }

    val timeFormatter = remember { DateTimeFormatter.ofPattern("hh:mm:ss a", Locale.getDefault()) }
    val formattedTime = currentTime.format(timeFormatter)

    val currentDate = LocalDate.now()
    val dayOfWeek = currentDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val month = currentDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    val dayOfMonth = currentDate.dayOfMonth
    val year = currentDate.year

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Image(
            painter = rememberAsyncImagePainter(Constants.BASE_URL + (imagePath ?: "")),
            contentDescription = "Profile",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickable(onClick = onProfileClick)
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formattedTime,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp // Adjusted to better fit seconds
            )
            Text(
                text = "$dayOfWeek | $month $dayOfMonth $year",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
        IconButton(
            onClick = { /* TODO */ },
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFFE8F5E9))
        ) {
            Icon(
                imageVector = Icons.Default.Notifications, // Placeholder icon
                contentDescription = "Speaker",
                tint = Color(0xFF388E3C)
            )
        }
    }
}

@Composable
fun ActionButtons(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally)
    ) {
        ActionButton(text = "Payslip", painter = painterResource(id = R.drawable.ic_payslip), onClick = { navController.navigate("payslip") })
        ActionButton(text = "Leaves", painter = painterResource(id = R.drawable.leave), onClick = {  navController.navigate("leave") })
    }
}

@Composable
fun ActionButton(text: String, painter: Painter, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(graentlight1, graentDark2) // More visible gradient
                    )
                )
                .border(1.dp, Color(0xFFA5D6A7), CircleShape), // Matching border
            contentAlignment = Alignment.Center
        ) {
            Icon(painter = painter, contentDescription = text, tint = Color(0xFF7CB342), modifier = Modifier.size(32.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}


@Composable
fun AttendanceStats(summary: PresenceCalendarViewModel.AttendanceSummary) {


    val lightRed = Color(0xFFFCE4EC)
    val borderRed = Color(0xFFF48FB1)
    val topBorderRed = Color(0xFFE57373)

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            InfoCard(
                value = summary.remainingDays.toString(),
                label = "Working days",
                borderColor = borderBlue,
                topBorderColor = topBorderblueDark,
                backgroundColor = lightBlue,
                shadowColor = borderBlue,
                modifier = Modifier.weight(1f)
            )
            InfoCard(
                value = summary.onTimeDays.toString(),
                label = "On Time",
                borderColor = borderBlue,
                topBorderColor = topBorderblueDark,
                backgroundColor = lightBlue,
                shadowColor = borderBlue,
                modifier = Modifier.weight(1f)
            )
            InfoCard(
                value = summary.lateDays.toString(),
                label = "Late",
                borderColor = borderRed,
                topBorderColor = topBorderRed,
                backgroundColor = lightRed,
                shadowColor = borderRed,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            InfoCard(
                value = summary.presentDays.toString(),
                label = "Total Present",
                borderColor = borderBlue,
                topBorderColor = topBorderblueDark,
                backgroundColor = lightBlue,
                shadowColor = borderBlue,
                modifier = Modifier.weight(1f)
            )
            InfoCard(
                value = summary.absentDays.toString(),
                label = "Total Absent",
                borderColor = borderRed,
                topBorderColor = topBorderRed,
                backgroundColor = lightRed,
                shadowColor = borderRed,
                modifier = Modifier.weight(1f)
            )
        }
    }
}


@Composable
fun InfoCard(
    value: String,
    label: String,
    borderColor: Color,
    topBorderColor: Color,
    backgroundColor: Color,
    shadowColor: Color,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = cardShape,
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clip(cardShape)
            .background(backgroundColor)
            .border(BorderStroke(1.dp, borderColor), cardShape)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(topBorderColor)
        )
        Column(
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 8.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.DarkGray
            )
        }
    }
}
