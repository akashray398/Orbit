package com.orbit.app.feature.home.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.orbit.app.core.common.ui.UiState
import com.orbit.app.core.designsystem.components.GlassCard
import com.orbit.app.core.designsystem.components.OrbitBackground
import com.orbit.app.core.designsystem.components.OrbitEmptyState
import com.orbit.app.core.designsystem.components.OrbitErrorState
import com.orbit.app.core.designsystem.components.OrbitLoadingIndicator
import com.orbit.app.core.designsystem.components.OrbitLogo
import com.orbit.app.core.designsystem.components.OrbitLogoMode
import com.orbit.app.core.designsystem.showcase.DesignSystemShowcaseScreen

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showShowcase by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    if (showShowcase) {
        DesignSystemShowcaseScreen(
            onBackClick = { showShowcase = false }
        )
    } else {
        OrbitBackground(modifier = modifier) {
            Scaffold(
                containerColor = Color.Transparent,
                modifier = Modifier.fillMaxSize()
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    when (val state = uiState) {
                        is UiState.Loading -> {
                            OrbitLoadingIndicator(
                                contentDescriptionText = "Loading Orbit status"
                            )
                        }
                        is UiState.Success -> {
                            GlassCard(
                                enableGlow = true,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    OrbitLogo(
                                        size = 56.dp,
                                        mode = OrbitLogoMode.Wordmark,
                                        animated = true
                                    )
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Text(
                                        text = state.data,
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .combinedClickable(
                                                onClick = {},
                                                onLongClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    showShowcase = true
                                                }
                                            )
                                            .semantics {
                                                contentDescription = state.data
                                            }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Personal AI Agent Active",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        is UiState.Empty -> {
                            OrbitEmptyState(
                                title = "No Agent Activity",
                                message = state.message
                            )
                        }
                        is UiState.Error -> {
                            OrbitErrorState(
                                message = state.message,
                                onRetry = state.onRetry
                            )
                        }
                    }
                }
            }
        }
    }
}
