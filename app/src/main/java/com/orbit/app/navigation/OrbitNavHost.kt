package com.orbit.app.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.orbit.app.feature.chat.navigation.chatScreen
import com.orbit.app.feature.home.navigation.HomeRoute
import com.orbit.app.feature.home.navigation.homeScreen
import com.orbit.app.feature.settings.navigation.settingsScreen
import com.orbit.app.splash.OrbitIntroScreen
import kotlinx.serialization.Serializable

@Serializable
object IntroRoute

@Serializable
object OnboardingRoute

object ProcessColdStartTracker {
    var isColdStart: Boolean = true
}

@Composable
fun OrbitNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val startDestination = if (ProcessColdStartTracker.isColdStart) IntroRoute else HomeRoute

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable<IntroRoute>(
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            OrbitIntroScreen(
                onIntroComplete = {
                    ProcessColdStartTracker.isColdStart = false
                    navController.navigate(HomeRoute) {
                        popUpTo<IntroRoute> { inclusive = true }
                    }
                }
            )
        }

        composable<OnboardingRoute>(
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            OnboardingStubScreen(
                onOnboardingComplete = {
                    navController.navigate(HomeRoute) {
                        popUpTo<OnboardingRoute> { inclusive = true }
                    }
                }
            )
        }

        homeScreen()
        chatScreen()
        settingsScreen()
    }
}
