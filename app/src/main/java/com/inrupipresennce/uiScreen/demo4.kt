package com.inrupipresennce.uiScreen


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inrupipresennce.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplyLeavePage() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Apply For Leave", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { /* Handle back navigation */ }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFAFAFA))
            )
        },
        containerColor = Color(0xFFFAFAFA)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            item { SectionTitle("Enter dates") }
            item { DateSelectionCard() }

            item { SectionTitle("Choose Leave Type") }
            item { LeaveTypeDropdown() }

            item { SectionTitle("Reason") }
            item { ReasonTextField() }

            item {
                Button(
                    onClick = { /*TODO*/ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB7DB4F))
                ) {
                    Text("Submit Now", color = Color.Black, modifier = Modifier.padding(vertical = 8.dp))
                }
            }
            item { UpcomingHolidaysSection() }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun DateSelectionCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F8E0)),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Enter dates",
                    fontWeight = FontWeight.SemiBold
                )
                Icon(Icons.Default.DateRange, contentDescription = "Calendar Icon")
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    label = { Text("Start Date") },
                    placeholder = { Text("mm/dd/yyyy") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFB7DB4F),
                        unfocusedBorderColor = Color(0xFFB7DB4F)
                    )
                )
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    label = { Text("End date") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFB7DB4F),
                        unfocusedBorderColor = Color(0xFFB7DB4F)
                    )
                )
            }
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { /*TODO*/ }) {
                    Text("Cancel", color = Color.Gray)
                }
                TextButton(onClick = { /*TODO*/ }) {
                    Text("OK", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


@Composable
fun LeaveTypeDropdown() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF0F8E0))
            .clickable { }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Casual Leave", color = Color.Gray)
        Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
    }
}


@Composable
fun ReasonTextField() {
    OutlinedTextField(
        value = "",
        onValueChange = {},
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        placeholder = { Text("Enter reason for leave here...") },
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF3B82F6), // Blue border as per image
            unfocusedBorderColor = Color(0xFF3B82F6)
        )
    )
}

@Composable
fun UpcomingHolidaysSection() {
    Column {
        Text(
            text = "Upcoming Holidays",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.face_scan), // Placeholder, replace with your Christmas icon
                contentDescription = "Christmas",
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text("Christmas...", fontWeight = FontWeight.SemiBold)
                Text("25 Dec", color = Color.Gray, fontSize = 14.sp)
            }
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_4")
@Composable
fun ApplyLeavePagePreview() {
    MaterialTheme {
        ApplyLeavePage()
    }
}
