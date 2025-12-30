package com.inrupipresennce.uiScreen.Screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inrupipresennce.R
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarPage() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendar", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { /* Handle back navigation */ }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = { CalendarBottomNavBar() },
        containerColor = Color.White
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            item { DateHeader() }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { CalendarView() }
            item { Spacer(modifier = Modifier.height(24.dp)) }
            item { SearchBar() }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { EventTabs() }
        }
    }
}

@Composable
fun DateHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Mon, Dec 25",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = { /*TODO*/ }) {
            Icon(Icons.Default.Edit, contentDescription = "Edit")
        }
    }
    Divider(color = Color.LightGray.copy(alpha = 0.5f), modifier = Modifier.padding(top = 8.dp))
}

@Composable
fun CalendarView() {
    val monthName = "August 2025"
    val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")
    val daysInMonth = 31
    val startPadding = 6 // Days to skip at the start of the month

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(monthName, fontWeight = FontWeight.SemiBold)
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Month")
            }
            Row {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
            }
        }
        Spacer(Modifier.height(16.dp))

        // Days of week header
        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { day ->
                Text(
                    day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            }
        }
        Spacer(Modifier.height(8.dp))

        // Days grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            userScrollEnabled = false,
            modifier = Modifier.height(250.dp) // Fixed height to show 5 rows
        ) {
            items(startPadding) {
                Box(modifier = Modifier.size(40.dp))
            }
            items(daysInMonth) { day ->
                val dayOfMonth = day + 1
                val isToday = dayOfMonth == 17
                val isSelected = dayOfMonth == 25

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isSelected -> Color(0xFFB7DB4F)
                                else -> Color.Transparent
                            }
                        )
                        .then(
                            if (isToday) Modifier.border(
                                BorderStroke(1.dp, Color(0xFFB7DB4F)),
                                CircleShape
                            ) else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dayOfMonth.toString(),
                        color = if (isSelected) Color.White else Color.Black,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun SearchBar() {
    OutlinedTextField(
        value = "",
        onValueChange = {},
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Search", color = Color.Gray) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
        trailingIcon = { Icon(Icons.Default.Edit, contentDescription = "Filter") }, // Replaced painterResource
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color(0xFFF0F8E0).copy(alpha = 0.5f),
            focusedContainerColor = Color(0xFFF0F8E0).copy(alpha = 0.5f),
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = Color(0xFFB7DB4F)
        )
    )
}

@Composable
fun EventTabs() {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Holidays", "My Tasks", "Events")

    Column {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.White,
            contentColor = Color.Black,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = Color(0xFFB7DB4F),
                    height = 3.dp
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            title,
                            color = if (selectedTabIndex == index) Color.Black else Color.Gray,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }
        // Content for the selected tab
        Box(modifier = Modifier.padding(top = 16.dp)) {
            when (selectedTabIndex) {
                0 -> HolidayCard()
                1 -> Text("My Tasks Content", modifier = Modifier.align(Alignment.Center))
                2 -> Text("Events Content", modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun HolidayCard() {
    // VVV THIS IS THE FIX VVV
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F8E0)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            // Background Image
            Image(
                painter = painterResource(id = R.drawable.bg), // Replace with your Christmas decoration image
                contentDescription = null, // Decorative image
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop // Crop the image to fill the card
            )
            // Content layered on top
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.1f)) // Optional: Add a scrim for better text readability
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Christmas Day", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF252F18))
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Celebrate Christmas, a season of joy, giving, and togetherness, as we share warmth and festive cheer.",
                        fontSize = 12.sp,
                        color = Color(0xFF526341).copy(alpha = 0.9f),
                        lineHeight = 16.sp
                    )
                }
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFB7DB4F).copy(alpha = 0.8f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = "Date", modifier = Modifier.size(12.dp), tint = Color.Black)
                    Spacer(Modifier.width(4.dp))
                    Text("25 Dec", fontSize = 12.sp, color = Color.Black)
                }
            }
        }
    }
    // ^^^ THIS IS THE FIX ^^^
}

@Composable
fun CalendarBottomNavBar() {
    NavigationBar(
        containerColor = Color(0xFFB7DB4F),
        modifier = Modifier
            .padding(12.dp)
            .clip(RoundedCornerShape(20.dp))
    ) {
        val items = listOf("Home", "Attendance", "Calendar", "Chat")
        val icons = listOf(
            Icons.Default.Home,
            Icons.Default.Home,
            Icons.Default.Home,
            Icons.Default.Home
        )
        var selectedIndex by remember { mutableIntStateOf(2) }

        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedIndex == index,
                onClick = { selectedIndex = index },
                icon = { Icon(icons[index], contentDescription = item) },
                label = { Text(item) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = Color.Black.copy(0.6f),
                    unselectedTextColor = Color.Black.copy(0.6f)
                )
            )
        }
    }
}


@Preview(showBackground = true, device = "id:pixel_4")
@Composable
fun CalendarPagePreview() {
    MaterialTheme {
        CalendarPage()
    }
}
