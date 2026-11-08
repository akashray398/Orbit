package com.orbit.app.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.orbit.app.core.designsystem.motion.LocalOrbitMotion
import com.orbit.app.core.designsystem.motion.OrbitMotionTokens
import com.orbit.app.core.designsystem.theme.OrbitTheme
import kotlin.math.sin

/**
 * WaveformView - Real-time audio waveform visualizer component.
 * Displays mirrored bars driven by input amplitude with zero per-frame allocations,
 * and falls back to a static level meter when reducedMotion is active.
 */
@Composable
fun WaveformView(
    amplitude: Float,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    barCount: Int = 40,
    contentDescriptionText: String = "Live Audio Waveform"
) {
    val reducedMotion = LocalOrbitMotion.current.reducedMotion
    val primaryGradient = OrbitTheme.colors.primaryGradient
    val fallbackColor = OrbitTheme.colors.glowPrimary

    val animatedAmplitude by animateFloatAsState(
        targetValue = amplitude.coerceIn(0f, 1f),
        animationSpec = OrbitMotionTokens.snappy(),
        label = "waveformAmplitude"
    )

    // Pre-calculated bar weights for shape contouring
    val barWeights = remember(barCount) {
        FloatArray(barCount) { index ->
            val progress = index.toFloat() / (barCount - 1)
            sin(progress * Math.PI).toFloat() // Bell curve distribution
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .semantics { contentDescription = contentDescriptionText },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(height)) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val centerY = canvasHeight / 2f

            if (reducedMotion) {
                // Reduced Motion Fallback Level Meter
                val meterWidth = canvasWidth * animatedAmplitude
                drawRoundRect(
                    color = fallbackColor,
                    topLeft = Offset(0f, centerY - 4.dp.toPx()),
                    size = Size(meterWidth, 8.dp.toPx()),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
                return@Canvas
            }

            val totalGaps = barCount - 1
            val gapWidth = 3.dp.toPx()
            val barWidth = ((canvasWidth - (totalGaps * gapWidth)) / barCount).coerceAtLeast(2.dp.toPx())
            val maxBarHeight = canvasHeight * 0.85f
            val minBarHeight = 4.dp.toPx()

            for (i in 0 until barCount) {
                val x = i * (barWidth + gapWidth)
                val weight = barWeights[i]
                val barH = (minBarHeight + (animatedAmplitude * maxBarHeight * weight)).coerceIn(minBarHeight, maxBarHeight)
                val topY = centerY - (barH / 2f)

                drawRoundRect(
                    brush = primaryGradient,
                    topLeft = Offset(x, topY),
                    size = Size(barWidth, barH),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                )
            }
        }
    }
}
