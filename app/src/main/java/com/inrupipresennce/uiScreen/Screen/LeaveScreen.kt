package com.inrupipresennce.uiScreen.Screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.inrupipresennce.data.model.LeaveItem
import com.inrupipresennce.ui.theme.graentDark2
import com.inrupipresennce.ui.theme.graentlight1
import com.inrupipresennce.uiScreen.ViewModelFactory.LeaveViewModelFactory
import com.inrupipresennce.uiScreen.viewmodel.LeaveViewModel
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: LeaveViewModel = viewModel(factory = LeaveViewModelFactory(context))

    val summary by viewModel.leaveSummary.collectAsState()
    val leaves by viewModel.leaves.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var selectedYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }

    LaunchedEffect(selectedYear) {
        viewModel.loadLeaves(selectedYear)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Leave Application", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Button(
                onClick = { navController.navigate("apply_leave") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(graentlight1, graentDark2)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Apply For Leave", color = Color.White)
                }
            }

            Spacer(Modifier.height(16.dp))

            summary?.let {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        value = "${it.paid_added - it.paid_used}/${it.paid_added}",
                        label = "Paid Balance",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        value = it.paid_used.toString(),
                        label = "Paid Used",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        value = it.unpaid_used.toString(),
                        label = "Unpaid Used",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Leave Request Info", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                YearSpinner(selectedYear) { year ->
                    selectedYear = year
                }
            }

            Spacer(Modifier.height(16.dp))

            if (isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(leaves) { leave ->
                        LeaveRequestItem(leave)
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.border(1.dp, Color(0xFF5F9DF5), RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FBFF))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)
            Text(text = label, fontSize = 14.sp, color = Color.Gray)
        }
    }
}

@Composable
fun LeaveRequestItem(leave: LeaveItem) {
    val statusColor = when (leave.status.lowercase()) {
        "approved" -> Color(0xFF4CAF50)
        "pending" -> Color(0xFFFF9800)
        "rejected" -> Color(0xFFF44336)
        else -> Color.Gray
    }

    fun formatDate(dateString: String): String {
        return try {
            val odt = OffsetDateTime.parse(dateString)
            val formatter = DateTimeFormatter.ofPattern("dd MMM, yy")
            odt.format(formatter)
        } catch (e: Exception) {
            dateString // Return original if parsing fails
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = formatDate(leave.from_date), fontSize = 14.sp, modifier = Modifier.weight(1f))
            Text(text = formatDate(leave.to_date), fontSize = 14.sp, modifier = Modifier.weight(1f))
            Text(
                text = leave.status,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { /* Handle more options */ }) {
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More options")
            }
        }
    }
}

@Composable
fun YearSpinner(selectedYear: Int, onYearSelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val years = (2020..2030).toList()

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FBFF)),
        modifier = Modifier.border(1.dp, Color(0xFF5F9DF5), RoundedCornerShape(8.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(text = selectedYear.toString(), fontWeight = FontWeight.Bold)
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            years.forEach { year ->
                DropdownMenuItem(
                    text = { Text(year.toString()) },
                    onClick = {
                        onYearSelected(year)
                        expanded = false
                    }
                )
            }
        }
    }
}
