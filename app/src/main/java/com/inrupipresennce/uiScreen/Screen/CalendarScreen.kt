
package com.inrupipresennce.uiScreen.Screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.inrupipresennce.data.valu.Constants
import com.inrupipresennce.ui.presence.PresenceCalendarScreen
import com.inrupipresennce.ui.presence.PresenceCalendarViewModel
import com.inrupipresennce.uiScreen.ViewModelFactory.PresenceCalendarViewModelFactory
import com.inrupipresennce.utils.FullScreenImageDialog
import com.inrupipresennce.utils.PreferenceHelper


@Composable
fun CalendarScreen(navController: NavHostController) {
    AttendanceOverviewScreen(navController)
}

@Composable
fun AttendanceOverviewScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: PresenceCalendarViewModel = viewModel(
        factory = PresenceCalendarViewModelFactory(context),
        key = "SharedPresenceVM"
    )

    val records by viewModel.presenceRecords.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()


    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val summary = viewModel.calculateAttendanceSummary(records)
    var showFullImage by remember { mutableStateOf(false) }
    val imagePath = PreferenceHelper.getImagePath(context)



    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            TopAppBar(
                imagePath = imagePath,
                onProfileClick = { showFullImage = true }
            )
        }
        item { ActionButtons(navController) }
        item { Spacer(Modifier.height(16.dp)) }
        item { AttendanceStats(summary = summary) }
        item { Spacer(Modifier.height(24.dp)) }
        item { PresenceCalendarScreen(viewModel, context) }
        item { Spacer(Modifier.height(24.dp)) }
        item {
            Text(
                text = "Working hours",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp)
            )
        }
        item { DailyReportBarChart(viewModel) }
    }

    if (showFullImage) {
        FullScreenImageDialog(
            imageUrl = Constants.BASE_URL + (imagePath ?: ""),
            onDismiss = { showFullImage = false }
        )
    }

}
