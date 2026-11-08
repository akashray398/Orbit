package com.orbit.app.feature.chat.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.orbit.app.feature.chat.ui.ChatScreen
import kotlinx.serialization.Serializable

@Serializable
object ChatRoute

fun NavController.navigateToChat(navOptions: NavOptions? = null) {
    navigate(ChatRoute, navOptions)
}

fun NavGraphBuilder.chatScreen(onSettingsClick: () -> Unit = {}) {
    composable<ChatRoute> {
        ChatScreen(onSettingsClick = onSettingsClick)
    }
}
