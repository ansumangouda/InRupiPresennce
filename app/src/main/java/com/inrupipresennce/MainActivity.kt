package com.inrupipresennce

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.inrupipresennce.data.repositry.AttendanceRepository
import com.inrupipresennce.navigation.AppNavGraph
import com.inrupipresennce.navigation.CustomBottomNavBar
import com.inrupipresennce.navigation.bottomNavItems
import com.inrupipresennce.ui.theme.InRupiPresennceTheme
import com.inrupipresennce.uiScreen.ViewModelFactory.AttendanceViewModelFactory
import com.inrupipresennce.uiScreen.viewmodel.AttendanceViewModel
import com.inrupipresennce.utils.PreferenceHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val repo = AttendanceRepository()
            val factory = AttendanceViewModelFactory(repo, this)
            val viewModel =
                androidx.lifecycle.ViewModelProvider(this, factory)[AttendanceViewModel::class.java]

            InRupiPresennceTheme {
                val navController = rememberNavController()
                val currentBackStack by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStack?.destination?.route ?: "login"

                // 🔹 Local state for logout confirmation popup
                var showLogoutDialog by remember { mutableStateOf(false) }

                Scaffold(

                    bottomBar = {
                        // ✅ Show bottom bar only for main routes
                        if (currentRoute.startsWith("face") ||
                            currentRoute.startsWith("attendance")
                        ) {
                            CustomBottomNavBar(
                                items = bottomNavItems,
                                currentRoute = currentRoute,
                                onItemSelected = { item ->
                                    when (item.route) {
                                        "logout" -> showLogoutDialog = true  // 👈 show popup instead of navigate
                                        else -> {
                                            navController.navigate(item.route) {
                                                popUpTo("face") { inclusive = false }
                                                launchSingleTop = true
                                            }
                                        }
                                    }
                                },
                                onLogoutConfirmed = {
                                    showLogoutDialog = true
                                } // (if needed for consistency)
                            )
                        }
                    }

                ) { innerPadding ->
                    AppNavGraph(
                        viewModel = viewModel,
                        navController = navController,
                        innerPadding = innerPadding
                    )
                }

                // 🔹 Logout confirmation dialog
                if (showLogoutDialog) {
                    AlertDialog(
                        onDismissRequest = { showLogoutDialog = false },
                        confirmButton = {
                            TextButton(onClick = {
                                PreferenceHelper.clear(this)
                                showLogoutDialog = false
                                navController.navigate("login") {
                                    popUpTo(0)
                                }
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
        }
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    InRupiPresennceTheme {
        Greeting("Android")
    }
}