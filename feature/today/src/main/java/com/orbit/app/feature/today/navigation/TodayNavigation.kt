package com.orbit.app.feature.today.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.orbit.app.feature.today.ui.TodayScreen
import kotlinx.serialization.Serializable

@Serializable
object TodayRoute

fun NavController.navigateToToday(navOptions: NavOptions? = null) {
    navigate(TodayRoute, navOptions)
}

fun NavGraphBuilder.todayScreen(
    onAskClick: () -> Unit = {},
    onSpeakClick: () -> Unit = {}
) {
    composable<TodayRoute> {
        TodayScreen(onAskClick = onAskClick, onSpeakClick = onSpeakClick)
    }
}
