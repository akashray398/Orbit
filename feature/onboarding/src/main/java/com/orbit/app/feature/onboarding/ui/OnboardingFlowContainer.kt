package com.orbit.app.feature.onboarding.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.orbit.app.core.designsystem.motion.OrbitMotionTokens

@Composable
fun OnboardingFlowContainer(
    onOnboardingComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AnimatedContent(
        targetState = uiState.isPermissionsStep,
        transitionSpec = {
            fadeIn(tween(OrbitMotionTokens.DurationMedium)) togetherWith fadeOut(tween(OrbitMotionTokens.DurationMedium))
        },
        label = "onboardingStepTransition",
        modifier = modifier
    ) { showPermissions ->
        if (showPermissions) {
            OnboardingPermissionsScreen(
                askedPermissions = uiState.onboardingState.askedPermissions,
                onPermissionAsked = viewModel::onPermissionAsked,
                onContinueClick = {
                    viewModel.completeOnboarding(onOnboardingComplete)
                }
            )
        } else {
            OnboardingPagerScreen(
                initialPage = uiState.onboardingState.lastCompletedPage,
                onPageChanged = viewModel::onPageChanged,
                onSkipClick = {
                    viewModel.onShowPermissionsStep()
                },
                onGetStartedClick = {
                    viewModel.onShowPermissionsStep()
                }
            )
        }
    }
}
