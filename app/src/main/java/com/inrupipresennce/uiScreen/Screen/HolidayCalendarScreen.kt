package com.inrupipresennce.uiScreen.Screen

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inrupipresennce.R
import com.inrupipresennce.data.model.EventData
import com.inrupipresennce.data.model.Holiday
import com.inrupipresennce.data.model.TaskData
import com.inrupipresennce.data.model.request.CreateTaskRequest
import com.inrupipresennce.data.model.request.UpdateTaskRequest
import com.inrupipresennce.data.repositry.EventRepository
import com.inrupipresennce.data.repositry.HolidayRepository
import com.inrupipresennce.data.repositry.TaskRepository
import com.inrupipresennce.ui.theme.borderBlue
import com.inrupipresennce.ui.theme.graentDark2
import com.inrupipresennce.ui.theme.graentlight1
import com.inrupipresennce.ui.theme.graentlight3
import com.inrupipresennce.ui.theme.lightBlue
import com.inrupipresennce.ui.theme.topBorderblueDark
import com.inrupipresennce.uiScreen.ViewModelFactory.EventViewModelFactory
import com.inrupipresennce.uiScreen.ViewModelFactory.HolidayViewModelFactory
import com.inrupipresennce.uiScreen.ViewModelFactory.TaskViewModelFactory
import com.inrupipresennce.uiScreen.viewmodel.EventViewModel
import com.inrupipresennce.uiScreen.viewmodel.HolidayViewModel
import com.inrupipresennce.uiScreen.viewmodel.TaskViewModel
import com.inrupipresennce.utils.PreferenceHelper
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HolidayCalendarScreen(
    holidayViewModel: HolidayViewModel = viewModel(
        factory = HolidayViewModelFactory(HolidayRepository())
    ),
    taskViewModel: TaskViewModel = viewModel(
        factory = TaskViewModelFactory(TaskRepository())
    ),
    eventViewModel: EventViewModel = viewModel(
        factory = EventViewModelFactory(EventRepository())
    )
) {
    val context = LocalContext.current

    var isCalendarExpanded by remember { mutableStateOf(true) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val holidays by holidayViewModel.holidays.collectAsState()
    val events by eventViewModel.events.collectAsState()
    val holidayDates = holidays.values.flatten().mapNotNull { holiday -> holiday.date?.let { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) } }
    var displayedMonth by remember { mutableStateOf(YearMonth.now()) }
    val today = LocalDate.now()
    val todayFormatter = DateTimeFormatter.ofPattern("E, MMM dd")
    val tasks by taskViewModel.tasks.collectAsState()
    var showAddTaskPopup by remember { mutableStateOf(false) }

    val updateTaskResult by taskViewModel.updateTaskResult.collectAsState()

    LaunchedEffect(updateTaskResult) {
        updateTaskResult?.let {
            Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
            if (it.message == "Task updated successfully") {
                // Refresh tasks
                taskViewModel.getTasks(PreferenceHelper.getAdminId(context))
            }
            taskViewModel.onTaskUpdated()
        }
    }


    LaunchedEffect(Unit) {
        holidayViewModel.getHolidays()
        taskViewModel.getTasks(PreferenceHelper.getAdminId(context))
        eventViewModel.getEvents()
    }


    LazyColumn(modifier = Modifier
        .padding(1.dp)
        .padding(horizontal = 16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(today.format(todayFormatter), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = { isCalendarExpanded = !isCalendarExpanded }) {
                    Icon(painterResource(id = R.drawable.ic_pencil), contentDescription = "Toggle Calendar")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    displayedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    fontWeight = FontWeight.SemiBold
                )
                Row {
                    IconButton(onClick = { displayedMonth = displayedMonth.minusMonths(1) }) {
                        Icon(
                            painterResource(id = R.drawable.ic_chevron_left),
                            contentDescription = "Previous month"
                        )
                    }
                    IconButton(onClick = { displayedMonth = displayedMonth.plusMonths(1) }) {
                        Icon(
                            painterResource(id = R.drawable.ic_chevron_right),
                            contentDescription = "Next month"
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            AnimatedVisibility(visible = isCalendarExpanded) {
                CalendarView(displayedMonth, holidayDates)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            Tabs(selectedTabIndex = selectedTabIndex, onTabSelected = {
                selectedTabIndex = it
                if (isCalendarExpanded) {
                    isCalendarExpanded = false
                }
            })
            Spacer(modifier = Modifier.height(16.dp))
        }

        when (selectedTabIndex) {
            0 -> {
                item {
                    SearchBar()
                    Spacer(Modifier.height(16.dp))
                }
                items(holidays.entries.toList()) { (month, holidayList) ->
                    Column {
                        Text(month, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        holidayList.forEach { holiday ->
                            HolidayItem(holiday)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
            1 -> {
                if (showAddTaskPopup) {
                    item {
                        AssignTaskDialog(taskViewModel = taskViewModel, onDismiss = { showAddTaskPopup = false })
                    }
                }
                item {
                    SearchBar()
                    Spacer(Modifier.height(16.dp))
                }
                items(tasks) { task ->
                    TaskItem(
                        task = task,
                        onUpdateTask = { updatedTask, status, notes ->
                            taskViewModel.updateTask(
                                taskId = updatedTask.id,
                                request = UpdateTaskRequest(
                                    admin_id = PreferenceHelper.getAdminId(context),
                                    status = status,
                                    notes = notes
                                )
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    val addTaskGradient = Brush.linearGradient(
                        colors = listOf(graentDark2, graentlight1)
                    )
                    Button(
                        onClick = { showAddTaskPopup = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(addTaskGradient, shape = RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Task", tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Add Tasks", color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            2 -> {
                item {
                    SearchBar()
                    Spacer(Modifier.height(16.dp))
                }
                items(events) { event ->
                    EventItem(event)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignTaskDialog(
    taskViewModel: TaskViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var assignedTo by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }

    val priorities = listOf("Low", "Medium", "High", "Urgent")
    var selectedPriority by remember { mutableStateOf("") }
    var priorityExpanded by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }

    val createTaskResult by taskViewModel.createTaskResult.collectAsState()

    LaunchedEffect(createTaskResult) {
        createTaskResult?.let {
            Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
            if (it.message == "Task created successfully") {
                onDismiss()
            }
            taskViewModel.onTaskCreated()
        }
    }

    Dialog(onDismissRequest = {}) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(2.dp, Color(0xFF4A90FF)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {

                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    Spacer(modifier = Modifier.height(8.dp))

                    // Assigned To
                    OutlinedTextField(
                        value = assignedTo,
                        onValueChange = { assignedTo = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Assigned To") },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    )

                    // Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Title") }
                    )

                    // Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        label = { Text("Description") }
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {

                        // Priority Button
                        ExposedDropdownMenuBox(
                            expanded = priorityExpanded,
                            onExpandedChange = { priorityExpanded = !priorityExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            OutlinedTextField(
                                value = selectedPriority,
                                onValueChange = {},
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                readOnly = true,
                                label = { Text("Level of Priority") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityExpanded)
                                }
                            )

                            ExposedDropdownMenu(
                                expanded = priorityExpanded,
                                onDismissRequest = { priorityExpanded = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                            ) {
                                priorities.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(item) },
                                        onClick = {
                                            selectedPriority = item
                                            priorityExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Due Date
                        OutlinedTextField(
                            value = dueDate,
                            onValueChange = {},
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            label = { Text("Due Date") },
                            placeholder = { Text("mm/dd/yyyy") },
                            trailingIcon = {
                                IconButton(onClick = { showDatePicker = true }) {
                                    Icon(Icons.Default.DateRange, contentDescription = "Calendar")
                                }
                            }
                        )

                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    val assignTaskGradient = Brush.linearGradient(
                        colors = listOf(graentDark2, graentlight1)
                    )
                    Button(
                        onClick = {
                            val request = CreateTaskRequest(
                                admin_id = 58,
                                title = title,
                                description = description,
                                assigned_to = 24,
                                priority = selectedPriority.lowercase(),
                                due_date = "2026-01-05"
                            )
                            taskViewModel.createTask(request)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(assignTaskGradient, shape = RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        )
                    ) {
                        Text(
                            "Assign Task",
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    }
                }
            }

            // ❌ Close Button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 10.dp, y = (-10).dp)
                    .size(36.dp)
                    .background(Color(0xFFE57373), CircleShape)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
        }
    }
    if (showDatePicker) {

        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()

                        dueDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

}

@Composable
fun CalendarView(displayedMonth: YearMonth, holidayDates: List<LocalDate>) {
    val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")
    val firstOfMonth = displayedMonth.atDay(1)
    val daysInMonth = displayedMonth.lengthOfMonth()
    val startDayOfWeek = firstOfMonth.dayOfWeek.value % 7 // Sunday is 0
    val today = LocalDate.now()

    val calendarDays = mutableListOf<LocalDate?>()
    repeat(startDayOfWeek) {
        calendarDays.add(null)
    }
    for (day in 1..daysInMonth) {
        calendarDays.add(displayedMonth.atDay(day))
    }

    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            daysOfWeek.forEachIndexed { index, day ->
                Text(day, fontWeight = FontWeight.Bold, color = if (index == 0) Color.Red else MaterialTheme.colorScheme.onSurface)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        val rows = calendarDays.chunked(7)
        rows.forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                week.forEach { date ->
                    if (date != null) {
                        val isHoliday = holidayDates.contains(date)
                        val isToday = date.isEqual(today)
                        val isSunday = date.dayOfWeek == DayOfWeek.SUNDAY

                        Box(
                            modifier = Modifier.size(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isToday -> Brush.linearGradient(
                                                colors = listOf(
                                                    graentDark2,
                                                    graentlight1
                                                )
                                            )
                                            isSunday -> SolidColor(Color.Red)
                                            else -> SolidColor(Color.Transparent)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = date.dayOfMonth.toString(),
                                    color = when {
                                        isToday || isSunday -> Color.White
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                            if (isHoliday && !isSunday) {
                                Box(
                                    modifier = Modifier
                                        .padding(bottom = 4.dp)
                                        .size(5.dp)
                                        .align(Alignment.BottomCenter)
                                        .background(Color.Red, CircleShape)
                                )
                            }
                        }
                    } else {
                        Box(modifier = Modifier.size(40.dp))
                    }
                }
                if (week.size < 7) {
                    repeat(7 - week.size) {
                        Box(modifier = Modifier.size(40.dp)) {}
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun SearchBar() {
    var text by remember { mutableStateOf("") }
    TextField(
        value = text,
        onValueChange = { text = it },
        label = { Text("Search") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
        trailingIcon = {
            Icon(
                painterResource(id = R.drawable.ic_filter),
                contentDescription = "Filter"
            )
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
        )
    )
}

@Composable
fun Tabs(selectedTabIndex: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf("Holidays", "My Tasks", "Events")
    TabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurface,
        indicator = {}) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = {
                    onTabSelected(index)
                },
                text = { Text(title) },
                modifier = if (selectedTabIndex == index) Modifier.background(
                    MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp)
                ) else Modifier
            )
        }
    }
}

@Composable
fun TaskItem(
    task: TaskData,
    onUpdateTask: (task: TaskData, status: String, notes: String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var selectedStatus by remember { mutableStateOf("") }

    if (showUpdateDialog) {
        UpdateTaskDialog(
            task = task,
            status = selectedStatus,
            onDismiss = { showUpdateDialog = false },
            onConfirm = { notes ->
                onUpdateTask(task, selectedStatus, notes)
                showUpdateDialog = false
            }
        )
    }

    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFFFFFF), graentlight3)
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .background(gradientBrush)
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val color = when (task.priority?.lowercase()) {
                    "high" -> Color.Red
                    "low" -> Color.Green
                    "urgent" -> Color.DarkGray
                    "medium" -> Color(0xFFFFA500) // Orange
                    else -> Color.Green
                }
                Box(
                    modifier = Modifier
                        .size(4.dp, 40.dp)
                        .background(color)
                ) {}
                Spacer(modifier = Modifier.size(8.dp))
                Column {
                    Text(task.status ?: "", color = color, fontSize = 12.sp)
                    Text(
                        task.title ?: "",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        task.description ?: "",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Pending") },
                        onClick = {
                            selectedStatus = "pending"
                            showUpdateDialog = true
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("In Progress") },
                        onClick = {
                            selectedStatus = "in_progress"
                            showUpdateDialog = true
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Completed") },
                        onClick = {
                            selectedStatus = "completed"
                            showUpdateDialog = true
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Cancelled") },
                        onClick = {
                            selectedStatus = "cancelled"
                            showUpdateDialog = true
                            showMenu = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun UpdateTaskDialog(
    task: TaskData,
    status: String,
    onDismiss: () -> Unit,
    onConfirm: (notes: String) -> Unit
) {
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Task Status") },
        text = {
            Column {
                Text("Update status to: $status")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(notes) }
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

class DateShape(private val cornerRadius: Float, private val arrowWidth: Float) : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            moveTo(0f, cornerRadius)
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(0f, 0f, cornerRadius * 2, cornerRadius * 2),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
               )
            lineTo(size.width - arrowWidth, 0f)
            lineTo(size.width, size.height / 2)
            lineTo(size.width - arrowWidth, size.height)
            quadraticBezierTo(
                x1 = size.width, y1 = size.height / 2,
                x2 = size.width - arrowWidth, y2 = size.height
            )
            lineTo(cornerRadius, size.height)
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(0f, size.height - cornerRadius * 2, cornerRadius * 2, size.height),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            close()
        }
        return Outline.Generic(path)
    }
}


@Composable
fun HolidayItem(holiday: Holiday) {
    val date = holiday.date?.let {
        try {
            LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (_: Exception) {
            null
        }
    }


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = lightBlue)
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(DateShape(cornerRadius = 36.dp.value, arrowWidth = 70f))
                    .background(Brush.linearGradient(
                        colors = listOf(
                            graentDark2,
                            graentlight1
                        )
                    )
                    )
                    .padding(horizontal = 42.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (date != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.US).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = date.dayOfMonth.toString(),
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = date.month.getDisplayName(java.time.format.TextStyle.SHORT, Locale.US).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = holiday.name ?: "",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.padding(end = 16.dp)
            )
        }
    }
}

@Composable
fun EventItem(event: EventData) {
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFFFFFF),graentlight3)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier.background(gradientBrush).padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .height(IntrinsicSize.Min)
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(topBorderblueDark, shape = RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = event.eventType ?: "", color = Color(0xFF5F9DF5), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = event.title ?: "", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = event.description ?: "", color = Color.Gray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Row(
                            modifier = Modifier
                                .border(BorderStroke(1.dp, borderBlue), RoundedCornerShape(25.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_calendar),
                                contentDescription = "Date",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = event.eventDate ?: "", color = Color.Gray, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(
                            modifier = Modifier
                                .border(BorderStroke(1.dp, borderBlue), RoundedCornerShape(25.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_clock),
                                contentDescription = "Time",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = event.eventTime ?: "", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun HolidayItemPreview() {
    //AssignTaskDialog(onDismiss = {})
}
