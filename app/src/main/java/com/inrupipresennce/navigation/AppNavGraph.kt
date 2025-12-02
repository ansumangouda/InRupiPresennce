package com.inrupipresennce.navigation


import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.inrupipresennce.uiScreen.Screen.CalendarScreen
import com.inrupipresennce.uiScreen.Screen.FaceAttendanceScreen
import com.inrupipresennce.uiScreen.Screen.LoginScreen
import com.inrupipresennce.uiScreen.Screen.LogoutButton
import com.inrupipresennce.uiScreen.Screen.SplashScreen
import com.inrupipresennce.uiScreen.ViewModelFactory.LoginViewModelFactory
import com.inrupipresennce.uiScreen.viewmodel.AttendanceViewModel
import com.inrupipresennce.uiScreen.viewmodel.LoginViewModel
import com.inrupipresennce.utils.PreferenceHelper

import kotlinx.coroutines.delay

@Composable
fun AppNavGraph(
    viewModel: AttendanceViewModel,
    navController: NavHostController,
    innerPadding: PaddingValues
) {
    val context = LocalContext.current
    val loginFactory = LoginViewModelFactory(context)
      var startDestination by remember { mutableStateOf("splash") }



    // Automatically skip login if user is logged in
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(innerPadding)
    ) {

        composable("splash") {
            SplashScreen(
                onFinished = { goToFace ->
                    if (goToFace) {
                        navController.navigate("face") {
                            popUpTo("splash") { inclusive = true }
                        }
                    } else {
                        navController.navigate("login") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                }
            )
        }

        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("face") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("face") {
            FaceAttendanceScreen(
                viewModel = viewModel,
                navController = navController
            )
        }

        composable("attendance") {
            CalendarScreen(
                navController = navController
            )
        }
        composable("logout") {
            LogoutButton(
                navController = navController
            )
        }


    }
}
