package com.orbit.app.feature.onboarding.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.orbit.app.feature.onboarding.ui.OnboardingFlowContainer
import kotlinx.serialization.Serializable

@Serializable
object OnboardingRoute

fun NavController.navigateToOnboarding(navOptions: NavOptions? = null) {
    navigate(OnboardingRoute, navOptions)
}

fun NavGraphBuilder.onboardingScreen(onOnboardingComplete: () -> Unit) {
    composable<OnboardingRoute> {
        OnboardingFlowContainer(onOnboardingComplete = onOnboardingComplete)
    }
}
