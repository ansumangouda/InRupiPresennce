package com.inrupipresennce.navigation


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.util.copy
import com.inrupipresennce.R
import com.inrupipresennce.ui.theme.graentDark2
import com.inrupipresennce.ui.theme.graentlight1
import com.inrupipresennce.utils.PreferenceHelper


data class BottomNavItem(
    val label: String,
    val iconRes: Int,
    val route: String
)

val bottomNavItems = listOf(
    BottomNavItem("Face Sean", R.drawable.face_scan2,"face"),
    BottomNavItem("Attendance", R.drawable.calendar,"attendance"),
    BottomNavItem("Logout", R.drawable.log_out ,"logout")
)

@Composable
fun CustomBottomNavBar(
    items: List<BottomNavItem>,
    currentRoute: String,
    onItemSelected: (BottomNavItem) -> Unit,
    onLogoutConfirmed: () -> Unit // 👈 callback for logout
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(50))
            .background(
                Brush.linearGradient(
                    colors = listOf(graentDark2, graentlight1)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route
                val backgroundColor =
                    if (isSelected) Color(0xFFFFFFFF) else Color.Transparent

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .background(backgroundColor.copy(alpha = if (isSelected) 0.2f else 0f))
                        .clickable {
                            if (item.route == "logout") {
                                showLogoutDialog = true // 👈 show popup
                            } else {
                                onItemSelected(item)    // normal navigation
                            }
                        }
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = item.iconRes),
                            contentDescription = item.label,
                            tint = if (isSelected) Color(0xFFFFFFFF) else Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(22.dp)
                        )
                        if (isSelected) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = item.label,
                                color = Color(0xFFFFFFFF),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // 👇 Logout confirmation dialog
    if (showLogoutDialog) {
        val context = LocalContext.current
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    PreferenceHelper.clear(context)
                    onLogoutConfirmed() // navigate or reset session
                }) {
                    Text("Yes, Logout", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to log out?") }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewBottomNavBar() {
    var selectedRoute by remember { mutableStateOf("home") }

    CustomBottomNavBar(
        items = bottomNavItems,
        currentRoute = selectedRoute,
        onItemSelected = { selectedRoute = it.route },
        onLogoutConfirmed = {}
    )
}
