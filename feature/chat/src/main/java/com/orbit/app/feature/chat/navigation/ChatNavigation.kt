package com.orbit.app.feature.chat.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
object ChatRoute

fun NavController.navigateToChat(navOptions: NavOptions? = null) {
    navigate(ChatRoute, navOptions)
}

fun NavGraphBuilder.chatScreen() {
    composable<ChatRoute> {
        // Placeholder for Chat screen (Prompt 6)
    }
}
