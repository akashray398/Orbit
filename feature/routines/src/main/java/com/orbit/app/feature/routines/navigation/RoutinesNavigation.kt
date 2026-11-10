package com.orbit.app.feature.routines.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.orbit.app.feature.routines.ui.RoutinesScreen
import kotlinx.serialization.Serializable

@Serializable
object RoutinesRoute

fun NavController.navigateToRoutines(navOptions: NavOptions? = null) {
    navigate(RoutinesRoute, navOptions)
}

fun NavGraphBuilder.routinesScreen(onBackClick: () -> Unit = {}) {
    composable<RoutinesRoute> {
        RoutinesScreen(onBackClick = onBackClick)
    }
}
