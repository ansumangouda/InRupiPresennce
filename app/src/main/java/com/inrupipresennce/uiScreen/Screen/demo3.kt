package com.inrupipresennce.uiScreen.Screen

import androidx.compose.foundation.BorderStroke


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class LeaveRequest(val fromDate: String, val toDate: String, val status: LeaveStatus)

enum class LeaveStatus(val color: Color) {
    Approved(Color(0xFF4CAF50)),
    Pending(Color(0xFFF9C80E)),
    Rejected(Color(0xFFF44336))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveApplicationPage() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Leave Application", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { /* Handle back navigation */ }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Button(
                    onClick = { /*TODO*/ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB7DB4F))
                ) {
                    Text("Apply now", color = Color.Black, modifier = Modifier.padding(vertical = 8.dp))
                }
            }

            item { LeaveBalanceSection() }

            item { LeaveRequestSection() }
        }
    }
}

@Composable
fun LeaveBalanceSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LeaveBalanceCard(value = "10/12", label = "Paid Balance", modifier = Modifier.weight(1f))
        LeaveBalanceCard(value = "1", label = "Paid Used", modifier = Modifier.weight(1f))
        LeaveBalanceCard(value = "1", label = "Unpaid Used", modifier = Modifier.weight(1f))
    }
}

@Composable
fun LeaveBalanceCard(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F8E0)),
        border = BorderStroke(1.dp, Color(0xFFB7DB4F))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun LeaveRequestSection() {
    val leaveRequests = remember {
        listOf(
            LeaveRequest("25 Aug, 26", "25 Aug, 26", LeaveStatus.Approved),
            LeaveRequest("25 Aug, 26", "25 Aug, 26", LeaveStatus.Pending),
            LeaveRequest("25 Aug, 26", "25 Aug, 26", LeaveStatus.Rejected),
            LeaveRequest("25 Aug, 26", "25 Aug, 26", LeaveStatus.Approved),
            LeaveRequest("25 Aug, 26", "25 Aug, 26", LeaveStatus.Approved)
        )
    }

    Column(modifier = Modifier.padding(top = 24.dp)) {
        // Section Header with Dropdown
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Leave Request Info", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Row(
                modifier = Modifier
                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clickable { },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "2025", fontSize = 14.sp)
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Year")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                .padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
            Text("From", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, color = Color.Gray)
            Text("To", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, color = Color.Gray)
            Text("Status", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, color = Color.Gray)
            Spacer(modifier = Modifier.width(24.dp)) // for alignment with the icon
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Leave request items
        leaveRequests.forEach { request ->
            LeaveRequestItem(request)
            Divider(color = Color.LightGray.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun LeaveRequestItem(request: LeaveRequest) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(request.fromDate, modifier = Modifier.weight(1f), fontSize = 14.sp)
        Text(request.toDate, modifier = Modifier.weight(1f), fontSize = 14.sp)
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(request.status.color.copy(alpha = 0.1f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = request.status.name,
                color = request.status.color,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
        }
        Icon(
            Icons.Default.MoreVert,
            contentDescription = "More Options",
            modifier = Modifier.size(24.dp)
        )
    }
}


@Preview(showBackground = true, device = "id:pixel_4")
@Composable
fun LeaveApplicationPagePreview() {
    MaterialTheme {
        LeaveApplicationPage()
    }
}
