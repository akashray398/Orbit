package com.orbit.app.core.domain.model

enum class ThemeMode {
    System,
    Light,
    Dark
}

enum class ReducedMotionOverride {
    System,
    On,
    Off
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.System,
    val dynamicColorEnabled: Boolean = false,
    val reducedMotionOverride: ReducedMotionOverride = ReducedMotionOverride.System,
    val hapticsEnabled: Boolean = true,
    val lastActiveConversationId: String = "default_conversation",
    val showDebugTools: Boolean = true
)
