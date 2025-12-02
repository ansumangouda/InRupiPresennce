package com.inrupipresennce.uiScreen.Screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.inrupipresennce.utils.PreferenceHelper
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinished: (Boolean) -> Unit
) {
    val context = LocalContext.current

    // Load lottie animation
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset("logo_ani.json")
    )

    // Play animation once
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1
    )

    // Navigation logic after animation ends
    LaunchedEffect(progress) {
        if (progress == 1f) {
            delay(500) // tiny delay for smoothness

            val isLoggedIn = PreferenceHelper.isLoggedIn(context)
            val faceVerified = PreferenceHelper.isFaceVerified(context)

            onFinished(isLoggedIn && faceVerified)
        }
    }

    // UI Layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.fillMaxSize()
        )
    }
}
