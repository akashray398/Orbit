package com.orbit.app.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.orbit.app.core.designsystem.theme.OrbitTheme
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
import com.orbit.app.splash.OrbitSplashScreen
import com.orbit.app.splash.SplashViewModel
import kotlinx.serialization.Serializable

@Serializable
object SplashRoute

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
        ProcessColdStartTracker.isColdStart -> SplashRoute
        !isOnboardingCompleted -> OnboardingRoute
        else -> ChatRoute
    }

    val isTopLevelRoute = currentRoute != null && (
            currentRoute.contains("ChatRoute") ||
                    currentRoute.contains("TodayRoute") ||
                    currentRoute.contains("ActivityRoute")
            )

    val showBottomBar = isTopLevelRoute && !ProcessColdStartTracker.isColdStart

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = Color(0xEE070B1A)
                ) {
                    val navItemColors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OrbitTheme.colors.glowSecondary,
                        selectedTextColor = OrbitTheme.colors.glowSecondary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = Color(0x337C5CFF)
                    )

                    NavigationBarItem(
                        selected = currentRoute?.contains("ChatRoute") == true,
                        onClick = {
                            navController.navigate(ChatRoute) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat") },
                        label = { Text("Chat") },
                        colors = navItemColors
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
                        icon = { Icon(Icons.Default.Today, contentDescription = "Today") },
                        label = { Text("Today") },
                        colors = navItemColors
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
                        icon = { Icon(Icons.Default.History, contentDescription = "Activity") },
                        label = { Text("Activity") },
                        colors = navItemColors
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
            composable<SplashRoute>(
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() }
            ) {
                OrbitSplashScreen(
                    onSplashComplete = {
                        ProcessColdStartTracker.isColdStart = false
                        val nextRoute = if (!isOnboardingCompleted) OnboardingRoute else ChatRoute
                        navController.navigate(nextRoute) {
                            popUpTo<SplashRoute> { inclusive = true }
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
