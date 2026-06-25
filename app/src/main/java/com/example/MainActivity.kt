package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.Destinations
import com.example.ui.OnboardingScreen
import com.example.ui.BodyProfileScreen
import com.example.ui.CameraScreen
import com.example.ui.ProcessingScreen
import com.example.ui.ViewportResultScreen
import com.example.ui.HistoryScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = Destinations.ONBOARDING
                ) {
                    composable(Destinations.ONBOARDING) {
                        OnboardingScreen(navController)
                    }
                    composable(Destinations.PROFILE) {
                        BodyProfileScreen(navController)
                    }
                    composable(Destinations.CAMERA) {
                        CameraScreen(navController)
                    }
                    composable(Destinations.PROCESSING) {
                        ProcessingScreen(navController)
                    }
                    composable(Destinations.STU_VIEWPORT) {
                        ViewportResultScreen(navController)
                    }
                    composable(Destinations.HISTORY) {
                        HistoryScreen(navController)
                    }
                }
            }
        }
    }
}

