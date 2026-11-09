package com.orbit.app.feature.activity.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.orbit.app.feature.activity.ui.ActivityScreen
import kotlinx.serialization.Serializable

@Serializable
object ActivityRoute

fun NavController.navigateToActivity(navOptions: NavOptions? = null) {
    navigate(ActivityRoute, navOptions)
}

fun NavGraphBuilder.activityScreen(onBackClick: () -> Unit = {}) {
    composable<ActivityRoute> {
        ActivityScreen(onBackClick = onBackClick)
    }
}
