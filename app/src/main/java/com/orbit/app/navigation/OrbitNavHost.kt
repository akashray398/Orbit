package com.orbit.app.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.orbit.app.feature.chat.navigation.ChatRoute
import com.orbit.app.feature.chat.navigation.chatScreen
import com.orbit.app.feature.home.navigation.homeScreen
import com.orbit.app.feature.onboarding.navigation.OnboardingRoute
import com.orbit.app.feature.onboarding.navigation.onboardingScreen
import com.orbit.app.feature.settings.navigation.SettingsRoute
import com.orbit.app.feature.settings.navigation.settingsScreen
import com.orbit.app.splash.OrbitIntroScreen
import com.orbit.app.splash.SplashViewModel
import kotlinx.serialization.Serializable

@Serializable
object IntroRoute

object ProcessColdStartTracker {
    var isColdStart: Boolean = true
}

@Composable
fun OrbitNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    splashViewModel: SplashViewModel = hiltViewModel()
) {
    val isOnboardingCompleted by splashViewModel.isOnboardingCompleted.collectAsStateWithLifecycle()

    val startDestination = when {
        ProcessColdStartTracker.isColdStart -> IntroRoute
        !isOnboardingCompleted -> OnboardingRoute
        else -> ChatRoute
    }

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
                    val nextRoute = if (!isOnboardingCompleted) OnboardingRoute else ChatRoute
                    navController.navigate(nextRoute) {
                        popUpTo<IntroRoute> { inclusive = true }
                    }
                }
            )
        }

        onboardingScreen(
            onOnboardingComplete = {
                navController.navigate(ChatRoute) {
                    popUpTo<OnboardingRoute> { inclusive = true }
                }
            }
        )

        chatScreen(
            onSettingsClick = {
                navController.navigate(SettingsRoute)
            }
        )

        homeScreen()
        settingsScreen()
    }
}
