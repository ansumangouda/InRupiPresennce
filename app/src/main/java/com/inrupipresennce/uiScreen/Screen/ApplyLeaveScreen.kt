package com.inrupipresennce.uiScreen.Screen

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.inrupipresennce.R
import com.inrupipresennce.ui.theme.graentDark2
import com.inrupipresennce.ui.theme.graentlight1
import com.inrupipresennce.uiScreen.ViewModelFactory.LeaveViewModelFactory
import com.inrupipresennce.uiScreen.viewmodel.LeaveViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplyLeaveScreen(navController: NavController) {

    val context = LocalContext.current
    val viewModel: LeaveViewModel = viewModel(
        factory = LeaveViewModelFactory(context)
    )

    var startDate by remember { mutableStateOf<Calendar?>(null) }
    var endDate by remember { mutableStateOf<Calendar?>(null) }
    var leaveType by remember { mutableStateOf("casual") }
    var reason by remember { mutableStateOf("") }
    val message by viewModel.message.collectAsState()

    val displayFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    val apiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val lightGreenBg = Color(0xFFF8FBFF)
    val borderColor = Color(0xFF5F9DF5)
    val buttonColor = Color(0xFFD0E6A5)

    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.message.value = null
        }
    }

    val today = Calendar.getInstance()

    val startDatePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val newStartDate = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            startDate = newStartDate
            if (endDate != null && newStartDate.after(endDate)) {
                endDate = null
            }
        },
        today.get(Calendar.YEAR),
        today.get(Calendar.MONTH),
        today.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.minDate = today.timeInMillis
    }

    val endDatePickerDialog = startDate?.let {
        val minEndDate = it.clone() as Calendar
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                endDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
            },
            it.get(Calendar.YEAR),
            it.get(Calendar.MONTH),
            it.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = minEndDate.timeInMillis
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Apply For Leave", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {

            Text("Enter dates", fontWeight = FontWeight.Bold, fontSize = 16.sp,)
            Spacer(Modifier.height(8.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = lightGreenBg)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Enter dates", modifier = Modifier.weight(1f),color = Color.Black)
                        Icon(imageVector = Icons.Default.DateRange, contentDescription = "Calendar Icon", tint = Color.Black)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DateTextField(
                            date = startDate?.let { displayFormat.format(it.time) } ?: "",
                            label = "Start Date",
                            modifier = Modifier.weight(1f)
                        ) {
                            startDatePickerDialog.show()
                        }
                        DateTextField(
                            date = endDate?.let { displayFormat.format(it.time) } ?: "",
                            label = "End Date",
                            enabled = startDate != null,
                            modifier = Modifier.weight(1f)
                        ) {
                            endDatePickerDialog?.show()
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Text("Choose Leave Type", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            LeaveTypeDropdown(leaveType) { leaveType = it }

            Spacer(Modifier.height(16.dp))

            Text("Reason", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            Card(
                modifier = Modifier.border(1.dp, borderColor, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = lightGreenBg),
            ) {
                TextField(
                    value = reason,
                    onValueChange = { reason = it },
                    placeholder = { Text("Enter reason for leave here...",color = Color.Black) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = lightGreenBg,
                        unfocusedContainerColor = lightGreenBg,
                        disabledContainerColor = lightGreenBg,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    )
                )
            }


            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    val start = startDate
                    val end = endDate
                    if (start != null && end != null) {
                        val apiStartDate = apiFormat.format(start.time)
                        val apiEndDate = apiFormat.format(end.time)
                        viewModel.applyLeave(apiStartDate, apiEndDate, leaveType, reason) {
                            navController.popBackStack()
                        }
                    } else {
                        Toast.makeText(context, "Please select start and end dates", Toast.LENGTH_SHORT).show()
                    }
                },
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
                )  {
                    Text("Submit Now", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(24.dp))

            Text("Upcoming Holidays", fontWeight = FontWeight.Bold, fontSize = 18.sp,color = Color.Black)
            Spacer(Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(1) { // Dummy item
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(id = R.drawable.leave), // Replace with your drawable
                            contentDescription = "Christmas",
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Text("Christmas", fontSize = 14.sp,color = Color.Black)
                        Text("25 Dec", fontSize = 12.sp,color = Color.Black)
                    }
                }
            }
        }
    }
}

@Composable
private fun DateTextField(
    date: String,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    TextField(
        value = date,
        onValueChange = { },
        label = { Text(label) },
        placeholder = { Text("dd-MM-yyyy") },
        modifier = modifier.clickable(enabled = enabled) { onClick() },
        enabled = false, // Always disabled to show custom click handling
        colors = TextFieldDefaults.colors(
            disabledTextColor = Color.Black,
            disabledContainerColor = Color.White,
            disabledLabelColor = Color.Gray,
            disabledIndicatorColor = Color.Gray
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveTypeDropdown(
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    val leaveTypes = listOf("casual", "sick")
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            value = selectedType.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
            onValueChange = {},
            readOnly = true,
            placeholder = { Text("Choose Leave Type") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            leaveTypes.forEach { leaveType ->
                DropdownMenuItem(
                    text = { Text(leaveType.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            Locale.getDefault()
                        ) else it.toString()
                    }) },
                    onClick = {
                        onTypeSelected(leaveType)
                        expanded = false
                    }
                )
            }
        }
    }
}
