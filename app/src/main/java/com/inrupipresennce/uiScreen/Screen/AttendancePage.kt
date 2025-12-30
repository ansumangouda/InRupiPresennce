package com.inrupipresennce.uiScreen.Screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.inrupipresennce.R
import java.util.Calendar

@Composable
fun AttendancePage() {
    Scaffold(
        bottomBar = { AttendanceBottomBarNav() }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            item { AttendancePageHeader() }
            item { QuickActionsSection() }
            item { StatsSection() }
            item { CalendarSection() }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { WorkingHoursSection() }

        }
    }
}


@Composable
fun AttendancePageHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Image(
            painter = painterResource(R.drawable.face_scan), // Replace with your image
            contentDescription = "User Avatar",
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "12:00 pm",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Wednesday | Dec 10 2025",
                fontSize = 13.sp,
                color = Color.Black.copy(alpha = 0.7f)
            )
        }

        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    Color(0xFFE8F3C9),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_bell), // Replace with your bell icon
                contentDescription = "Notifications",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}


@Composable
fun QuickActionsSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        QuickActionItem(R.drawable.ic_payslip, "Payslip") // Replace with actual icons
        QuickActionItem(R.drawable.ic_payslip, "Leaves") // <-- CORRECTED ICON
    }
}


@Composable
fun QuickActionItem(iconRes: Int, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .border(2.dp, Color(0xFFB7DB4F), CircleShape)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                modifier = Modifier.size(32.dp),
                tint = Color(0xFF7BA93B)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, fontSize = 12.sp)
    }
}

@Composable
fun StatsSection() {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        // VVV WHERE THE CHANGE IS VVV
        Row(
            modifier = Modifier.height(IntrinsicSize.Min), // Enforce same height for children
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                "22",
                "Working days",
                Color(0xFFE8F3C9),
                Color(0xFF7BA93B),
                Modifier.weight(1f)
            )
            StatCard("5", "On Time", Color(0xFFE8F3C9), Color(0xFF7BA93B), Modifier.weight(1f))
            StatCard("0", "Late", Color(0xFFFFEBEE), Color.Red, Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.height(IntrinsicSize.Min), // Enforce same height for children
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                "5",
                "Total Present",
                Color(0xFFE8F3C9),
                Color(0xFF7BA93B),
                Modifier.weight(1f)
            )
            StatCard("0", "Total Absent", Color(0xFFFFEBEE), Color.Red, Modifier.weight(1f))
        }
        // ^^^ WHERE THE CHANGE IS ^^^
    }
}


@Composable
fun StatCard(
    value: String,
    label: String,
    backgroundColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(), // <-- ADD THIS MODIFIER
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor)
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(label, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun CalendarSection() {
    val calendar = Calendar.getInstance()
    // For demonstration, using December 2025
    calendar.set(2025, Calendar.DECEMBER, 1)

    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // Sunday is 1, we want 0-indexed

    // --- Calculate the number of rows ---
    val totalCells = firstDayOfWeek + daysInMonth
    val numberOfRows = (totalCells + 6) / 7 // Integer division trick to get ceiling
    // Increase the assumed row height to make space for the shadow
    val gridHeight = ((50 * numberOfRows) + 10).dp // Add extra space for bottom shadow

    val monthName = "December 2025"
    val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Previous Month",
                modifier = Modifier.size(24.dp)
            )
            Text(monthName, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = "Next Month",
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.height(16.dp))

        // Days of week header
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            daysOfWeek.forEach { day ->
                Text(
                    day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.height(8.dp))

        // Days grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            // --- KEY CHANGE: Use contentPadding to give space for the shadow ---
            contentPadding = PaddingValues(bottom = 12.dp),
            userScrollEnabled = false,
            // Apply the calculated fixed height
            modifier = Modifier.height(gridHeight)
        ) {
            // Empty cells for padding before the first day
            items(firstDayOfWeek) {
                Box(modifier = Modifier.fillMaxSize()) // Just fill the cell
            }

            // Actual days
            items(daysInMonth) { day ->
                val dayOfMonth = day + 1
                val isPresent = dayOfMonth in 8..12 // Sample present days
                val isLate = dayOfMonth == 6 || dayOfMonth == 13 || dayOfMonth == 20 || dayOfMonth == 27
                val isLeave = dayOfMonth in 2..5

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(4.dp)
                        .aspectRatio(1f)
                        .graphicsLayer(
                            shadowElevation = 8f, // Adjusted for a more subtle look
                            spotShadowColor = Color(0xFF477B0D),
                            shape = CircleShape,
                            // Move content up to make shadow appear lower
                            translationY = -4f
                        )
                        .clip(CircleShape)
                        .background(
                            when {
                                isLate -> Color(0xFFFFEBEE)
                                else -> Color(0xFFE8F3C9)
                            }
                        )
                        .border(
                            1.dp,
                            if (isLate) Color.Red else Color(0xFFB7DB4F),
                            CircleShape
                        )
                ) {
                    if (isPresent) {
                        Image(
                            painter = painterResource(id = R.drawable.face_scan),
                            contentDescription = "Present",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } else {
                        Text(
                            text = dayOfMonth.toString(),
                            color = if (isLate) Color.Red else Color.Black,
                            fontWeight = if (isLate) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun WorkingHoursSection() {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Working hours",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        // You can add a graph or more details here
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Color.LightGray, RoundedCornerShape(8.dp))
        )
    }
}


@Composable
fun AttendanceBottomBarNav() {
    NavigationBar(
        containerColor = Color(0xFFB7DB4F),
        modifier = Modifier
            .padding(12.dp)
            .clip(RoundedCornerShape(20.dp))
    ) {
        val items = listOf("Home", "Attendance", "Calendar", "My Team")
        val icons = listOf(
            Icons.Default.Home,
            Icons.Default.Home,
            Icons.Default.Home,
            Icons.Default.Person
        )

        items.forEachIndexed { index, item ->
            val isSelected = item == "Attendance"
            NavigationBarItem(
                selected = isSelected,
                onClick = { /* Handle navigation */ },
                icon = {
                    Icon(
                        icons[index],
                        contentDescription = item,
                        tint = if (isSelected) Color.White else Color.Black.copy(0.6f)
                    )
                },
                label = {
                    Text(
                        item,
                        color = if (isSelected) Color.White else Color.Black.copy(0.6f)
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Preview(showBackground = true, device = Devices.PIXEL_4)
@Composable
fun AttendancePagePreview() {
    MaterialTheme {
        AttendancePage()
    }
}
