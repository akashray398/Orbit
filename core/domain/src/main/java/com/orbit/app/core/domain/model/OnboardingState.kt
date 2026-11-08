package com.orbit.app.core.domain.model

data class OnboardingState(
    val onboardingCompleted: Boolean = false,
    val lastCompletedPage: Int = 0,
    val permissionStepSeen: Boolean = false,
    val askedPermissions: Set<String> = emptySet()
)
