package com.orbit.app.feature.chat.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Stop
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
import androidx.compose.ui.unit.dp
import com.orbit.app.core.designsystem.components.OrbitChip
import com.orbit.app.core.designsystem.components.WaveformView
import com.orbit.app.core.designsystem.motion.OrbitMotionTokens
import com.orbit.app.core.designsystem.theme.OrbitTheme
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

    // Timer limit 60 seconds
    LaunchedEffect(Unit) {
        while (elapsedTimeSeconds < 60) {
            delay(1000L)
            elapsedTimeSeconds++
        }
        if (elapsedTimeSeconds >= 60) {
            onStopClick()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.glassFill)
            .padding(16.dp)
            .navigationBarsPadding()
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OrbitChip(
                label = currentLocale.displayName,
                selected = true,
                onClick = onLanguageClick
            )

            Text(
                text = String.format("%02d:%02d / 01:00", elapsedTimeSeconds / 60, elapsedTimeSeconds % 60),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            IconButton(onClick = onCancelClick) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel Voice Input",
                    tint = OrbitTheme.colors.danger
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Live Waveform
        WaveformView(
            amplitude = if (isSlideToCancel) 0f else amplitude,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Partial Transcript / Status Text
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
                    text = if (partialText.isNotEmpty()) partialText else "Listening...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.semantics {
                        contentDescription = "Live transcript: $partialText"
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val btnBgModifier = if (isSlideToCancel) {
            Modifier.background(colors.danger)
        } else {
            Modifier.background(colors.primaryGradient)
        }

        // Stop / Send Action Button
        IconButton(
            onClick = onStopClick,
            modifier = Modifier
                .size(56.dp)
                .clip(OrbitTheme.shapes.medium)
                .then(btnBgModifier)
                .semantics { contentDescription = "Done speaking" }
        ) {
            Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
