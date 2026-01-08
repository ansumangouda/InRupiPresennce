
package com.inrupipresennce.navigation


import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.inrupipresennce.uiScreen.Screen.ApplyLeaveScreen
import com.inrupipresennce.uiScreen.Screen.CalendarScreen
import com.inrupipresennce.uiScreen.Screen.Facescreen
import com.inrupipresennce.uiScreen.Screen.HolidayCalendarScreen
import com.inrupipresennce.uiScreen.Screen.LeaveScreen
import com.inrupipresennce.uiScreen.Screen.LoginScreen
import com.inrupipresennce.uiScreen.Screen.LogoutButton
import com.inrupipresennce.uiScreen.Screen.PayslipScreen
import com.inrupipresennce.uiScreen.Screen.PdfViewScreen
import com.inrupipresennce.uiScreen.Screen.SplashScreen
import com.inrupipresennce.uiScreen.ViewModelFactory.LoginViewModelFactory
import com.inrupipresennce.uiScreen.viewmodel.AttendanceViewModel

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
            Facescreen(
                viewModel = viewModel,
              //  navController = navController
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

        composable("payslip") {
            PayslipScreen(navController = navController)
        }

        composable("leave") {
            LeaveScreen(navController = navController)
        }
        composable("holiday_calendar") {
            HolidayCalendarScreen()
        }

        composable("apply_leave") {
            ApplyLeaveScreen(navController = navController)
        }

        composable(
            route = "pdfView?url={pdfUrl}",
            arguments = listOf(navArgument("pdfUrl") { type = NavType.StringType })
        ) { backStackEntry ->
            val pdfUrl = backStackEntry.arguments?.getString("pdfUrl")
            if (pdfUrl != null) {
                PdfViewScreen(pdfUrl = pdfUrl)
            }
        }


    }
}
