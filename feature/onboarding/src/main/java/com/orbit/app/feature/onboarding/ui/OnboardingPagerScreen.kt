package com.orbit.app.feature.onboarding.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.orbit.app.core.designsystem.R
import com.orbit.app.core.designsystem.components.OrbitBackground
import com.orbit.app.core.designsystem.components.OrbitButton
import com.orbit.app.core.designsystem.components.OrbitButtonSize
import com.orbit.app.core.designsystem.components.OrbitButtonVariant
import com.orbit.app.core.designsystem.motion.OrbitMotionTokens
import com.orbit.app.core.designsystem.theme.OrbitTheme
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@Composable
fun OnboardingPagerScreen(
    initialPage: Int,
    onPageChanged: (Int) -> Unit,
    onSkipClick: () -> Unit,
    onGetStartedClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { 4 })
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            onPageChanged(page)
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    // System Back handles navigating to previous page
    BackHandler(enabled = pagerState.currentPage > 0) {
        scope.launch {
            pagerState.animateScrollToPage(pagerState.currentPage - 1)
        }
    }

    OrbitBackground(modifier = modifier) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (pagerState.currentPage > 0) {
                        TextButton(onClick = {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                        }) {
                            Text(
                                text = stringResource(R.string.onboarding_back),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }

                    TextButton(onClick = onSkipClick) {
                        Text(
                            text = stringResource(R.string.onboarding_skip),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) { page ->
                    OnboardingPageItem(
                        page = page,
                        pageOffsetCalculator = {
                            val currentPage = pagerState.currentPage
                            val offset = pagerState.currentPageOffsetFraction
                            (currentPage - page) + offset
                        }
                    )
                }

                // Bottom Controls: Page Indicator & Action Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OnboardingPageIndicator(
                        pageCount = 4,
                        currentPage = pagerState.currentPage,
                        pageOffsetFraction = pagerState.currentPageOffsetFraction
                    )

                    val isLastPage = pagerState.currentPage == 3
                    OrbitButton(
                        onClick = {
                            if (isLastPage) {
                                onGetStartedClick()
                            } else {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        },
                        variant = OrbitButtonVariant.Primary,
                        size = OrbitButtonSize.Medium
                    ) {
                        AnimatedContent(
                            targetState = isLastPage,
                            transitionSpec = { fadeIn(tween(OrbitMotionTokens.DurationFast)) togetherWith fadeOut(tween(OrbitMotionTokens.DurationFast)) },
                            label = "buttonLabelAnimation"
                        ) { last ->
                            Text(
                                text = if (last) {
                                    stringResource(R.string.onboarding_get_started)
                                } else {
                                    stringResource(R.string.onboarding_next)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageItem(
    page: Int,
    pageOffsetCalculator: () -> Float
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Parallax Illustration Layer (moves faster)
        Box(
            modifier = Modifier
                .graphicsLayer {
                    val pageOffset = pageOffsetCalculator()
                    translationX = pageOffset * size.width * 0.45f
                    alpha = 1f - (pageOffset.absoluteValue * 0.8f).coerceAtMost(1f)
                },
            contentAlignment = Alignment.Center
        ) {
            when (page) {
                0 -> OnboardingIllustrationPage1()
                1 -> OnboardingIllustrationPage2()
                2 -> OnboardingIllustrationPage3()
                3 -> OnboardingIllustrationPage4()
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Parallax Title Layer (medium speed)
        Text(
            text = stringResource(
                when (page) {
                    0 -> R.string.onboarding_page1_title
                    1 -> R.string.onboarding_page2_title
                    2 -> R.string.onboarding_page3_title
                    else -> R.string.onboarding_page4_title
                }
            ),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.graphicsLayer {
                val pageOffset = pageOffsetCalculator()
                translationX = pageOffset * size.width * 0.25f
                alpha = 1f - (pageOffset.absoluteValue * 0.6f).coerceAtMost(1f)
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Parallax Body Layer (standard speed)
        Text(
            text = stringResource(
                when (page) {
                    0 -> R.string.onboarding_page1_body
                    1 -> R.string.onboarding_page2_body
                    2 -> R.string.onboarding_page3_body
                    else -> R.string.onboarding_page4_body
                }
            ),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.graphicsLayer {
                val pageOffset = pageOffsetCalculator()
                translationX = pageOffset * size.width * 0.15f
                alpha = 1f - (pageOffset.absoluteValue * 0.5f).coerceAtMost(1f)
            }
        )
    }
}

@Composable
private fun OnboardingPageIndicator(
    pageCount: Int,
    currentPage: Int,
    pageOffsetFraction: Float
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.semantics {
            stateDescription = "Page ${currentPage + 1} of $pageCount"
        }
    ) {
        repeat(pageCount) { index ->
            val isSelected = index == currentPage
            val color by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary else OrbitTheme.colors.glassBorderEnd,
                animationSpec = OrbitMotionTokens.gentle(),
                label = "indicatorColor"
            )
            val width = if (isSelected) 24.dp else 8.dp

            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}
