package com.orbit.app.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.orbit.app.feature.activity.navigation.ActivityRoute
import com.orbit.app.feature.activity.navigation.activityScreen
import com.orbit.app.feature.chat.navigation.ChatRoute
import com.orbit.app.feature.chat.navigation.chatScreen
import com.orbit.app.feature.home.navigation.homeScreen
import com.orbit.app.feature.onboarding.navigation.OnboardingRoute
import com.orbit.app.feature.onboarding.navigation.onboardingScreen
import com.orbit.app.feature.routines.navigation.routinesScreen
import com.orbit.app.feature.settings.navigation.SettingsRoute
import com.orbit.app.feature.settings.navigation.settingsScreen
import com.orbit.app.feature.today.navigation.TodayRoute
import com.orbit.app.feature.today.navigation.todayScreen
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
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val startDestination = when {
        ProcessColdStartTracker.isColdStart -> IntroRoute
        !isOnboardingCompleted -> OnboardingRoute
        else -> ChatRoute
    }

    val showBottomBar = currentRoute != null &&
            !currentRoute.contains("IntroRoute") &&
            !currentRoute.contains("OnboardingRoute")

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute?.contains("ChatRoute") == true,
                        onClick = {
                            navController.navigate(ChatRoute) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Notifications, contentDescription = "Chat") },
                        label = { Text("Chat") }
                    )
                    NavigationBarItem(
                        selected = currentRoute?.contains("TodayRoute") == true,
                        onClick = {
                            navController.navigate(TodayRoute) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.DateRange, contentDescription = "Today") },
                        label = { Text("Today") }
                    )
                    NavigationBarItem(
                        selected = currentRoute?.contains("ActivityRoute") == true,
                        onClick = {
                            navController.navigate(ActivityRoute) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Info, contentDescription = "Activity") },
                        label = { Text("Activity") }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier.padding(innerPadding)
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

            todayScreen(
                onAskClick = {
                    navController.navigate(ChatRoute)
                },
                onSpeakClick = {
                    navController.navigate(ChatRoute)
                }
            )

            homeScreen()

            settingsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )

            activityScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )

            routinesScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
