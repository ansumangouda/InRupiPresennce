package com.inrupipresennce.navigation


import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    BottomNavItem("Home", R.drawable.ic_home, "face"),
    BottomNavItem("Calendar", R.drawable.calendar, "attendance"),
    BottomNavItem("Face Scan", R.drawable.face_scan2, "face"),
    BottomNavItem("Chat", R.drawable.ic_chat, "attendance"),
    BottomNavItem("Logout", R.drawable.log_out, "logout")
)

@Composable
fun CustomBottomNavBar(
    items: List<BottomNavItem>,
    currentRoute: String,
    onItemSelected: (BottomNavItem) -> Unit,
    onLogoutConfirmed: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "fab-transition")
    val fabOffset by infiniteTransition.animateFloat(
        initialValue = -20f,
        targetValue = -25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fab-offset"
    )


    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(70.dp)
    ) {

        // 🔹 Bottom Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 10.dp)
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(graentDark2, graentlight1)
                    )
                ),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {

            val leftItems = items.take(2)
            val rightItems = items.takeLast(2)

            leftItems.forEach { item ->
                BottomNavMenuItem(
                    item = item,
                    isSelected = currentRoute == item.route,
                    onClick = {
                        if (item.route == "logout") showLogoutDialog = true
                        else onItemSelected(item)
                    }
                )
            }

            Spacer(modifier = Modifier.width(56.dp)) // 👈 space for FAB

            rightItems.forEach { item ->
                BottomNavMenuItem(
                    item = item,
                    isSelected = currentRoute == item.route,
                    onClick = {
                        if (item.route == "logout") showLogoutDialog = true
                        else onItemSelected(item)
                    }
                )
            }
        }

        // 🔹 Floating Center Button
        val fabItem = items[2]

        FloatingCenterButton(
            item = fabItem,
            isSelected = currentRoute == fabItem.route,
            onClick = { onItemSelected(fabItem) },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = fabOffset.dp) // 👈 FLOAT EFFECT
        )
    }

    if (showLogoutDialog) LogoutDialog(onLogoutConfirmed) {
        showLogoutDialog = false
    }
}


@Composable
fun BottomNavMenuItem(item: BottomNavItem, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(30.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = item.iconRes),
                contentDescription = item.label,
                tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = item.label,
                color = if (isSelected) Color.White else Color.White,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun FloatingCenterButton(
    item: BottomNavItem,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(72.dp)
            .shadow(12.dp, CircleShape)
            .clip(CircleShape)
            .background(Color.White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(4.dp) // Add padding to ensure content fits within the circle
        ) {
            Icon(
                painter = painterResource(id = item.iconRes),
                contentDescription = item.label,
                tint = if (isSelected) graentDark2 else Color.Gray,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = item.label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) graentDark2 else Color.Gray
            )
        }
    }
}
@Composable
fun LogoutDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                PreferenceHelper.clear(context)
                onConfirm()
            }) {
                Text("Yes, Logout", color = Color.Red)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Logout") },
        text = { Text("Are you sure you want to log out?") }
    )
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
