package com.orbit.app.feature.chat.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orbit.app.core.designsystem.components.AgentOrb
import com.orbit.app.core.designsystem.components.OrbitChip
import com.orbit.app.core.designsystem.components.WaveformView
import com.orbit.app.core.designsystem.motion.OrbitMotionTokens
import com.orbit.app.core.designsystem.theme.OrbitTheme
import com.orbit.app.core.domain.model.AgentState
import com.orbit.app.core.domain.model.VoiceLocale
import kotlinx.coroutines.delay

@Composable
fun VoiceInputPanel(
    amplitude: Float,
    partialText: String,
    currentLocale: VoiceLocale,
    isSlideToCancel: Boolean,
    onCancelClick: () -> Unit,
    onStopClick: () -> Unit,
    onLanguageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = OrbitTheme.colors
    var elapsedTimeSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (elapsedTimeSeconds < 60) {
            delay(1000L)
            elapsedTimeSeconds++
        }
        if (elapsedTimeSeconds >= 60) {
            onStopClick()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.auroraGradient)
            .padding(24.dp)
            .navigationBarsPadding()
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Header: Close Chat & Language Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OrbitChip(
                    label = currentLocale.displayName,
                    selected = true,
                    onClick = onLanguageClick
                )

                // Mockup style "✕ Close chat" pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0x33FFFFFF))
                        .clickable { onCancelClick() }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close chat",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Close chat",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Centerpiece: Glowing 3D Metallic Amber Planet Sphere (170dp size matching mockup)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AgentOrb(
                    state = AgentState.Listening,
                    amplitude = amplitude,
                    size = 170.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Orbit is listening...",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Large Bold Prompt Transcript Display Text
                AnimatedContent(
                    targetState = isSlideToCancel,
                    transitionSpec = { fadeIn(tween(OrbitMotionTokens.DurationFast)) togetherWith fadeOut(tween(OrbitMotionTokens.DurationFast)) },
                    label = "transcriptAnimation"
                ) { cancelState ->
                    if (cancelState) {
                        Text(
                            text = "Release to cancel",
                            style = MaterialTheme.typography.titleMedium,
                            color = OrbitTheme.colors.danger
                        )
                    } else {
                        Text(
                            text = if (partialText.isNotEmpty()) partialText else "Deploy an autonomous AI agent to monitor your phone tasks.",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                lineHeight = 30.sp,
                                textAlign = TextAlign.Center
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .semantics { contentDescription = "Live transcript: $partialText" }
                        )
                    }
                }
            }

            // Bottom Mic Controls (Mockup Concentric Orange Mic Button)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                WaveformView(
                    amplitude = if (isSlideToCancel) 0f else amplitude,
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(36.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(Color(0x22FF5500))
                        .border(width = 1.dp, color = Color(0x66FF5500), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = onStopClick,
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .background(colors.primaryGradient)
                            .semantics { contentDescription = "Done speaking" }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}
