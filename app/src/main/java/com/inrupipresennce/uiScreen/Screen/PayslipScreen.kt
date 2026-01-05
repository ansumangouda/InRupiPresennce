
package com.inrupipresennce.uiScreen.Screen

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.inrupipresennce.R
import com.inrupipresennce.data.model.PayslipData
import com.inrupipresennce.uiScreen.ViewModelFactory.PayslipViewModelFactory
import com.inrupipresennce.uiScreen.viewmodel.PayslipViewModel
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun PayslipScreen(navController: NavHostController) {

    val context = LocalContext.current

    val viewModel: PayslipViewModel = viewModel(
        factory = PayslipViewModelFactory(context)
    )

    val payslips by viewModel.payslips.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val count by viewModel.count.collectAsState()
    val message by viewModel.message.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadPayslips()
    }

    Column(modifier = Modifier.fillMaxSize()) {

        Text(
            text = "Payslips for 2025 ($count)",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        when {
            isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            payslips.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = message ?: "No payslips found")
                }
            }

            else -> {
                LazyColumn {
                    items(payslips) { payslip ->
                        PayslipRow(payslip, navController)
                    }
                }
            }
        }
    }
}
@Composable
fun PayslipRow(payslip: PayslipData, navController: NavHostController) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            painter = painterResource(id = R.drawable.ic_payslip),
            contentDescription = null,
            tint = Color(0xFF7CB342)
        )

        Spacer(Modifier.width(12.dp))

        Text(
            text = payslip.monthName,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = {
            val encodedUrl = URLEncoder.encode(payslip.pdfUrl, StandardCharsets.UTF_8.toString())
            navController.navigate("pdfView?url=$encodedUrl")
        }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_eye),
                contentDescription = "View",
                tint = Color(0xFF7CB342)
            )
        }

        IconButton(onClick = {
            val request = DownloadManager.Request(payslip.pdfUrl.toUri())
                .setTitle("Payslip - ${payslip.monthName}")
                .setDescription("Downloading")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "payslip_${payslip.monthName}.pdf")
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
        }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_download),
                contentDescription = "Download",
                tint = Color(0xFF7CB342)
            )
        }
    }
}

