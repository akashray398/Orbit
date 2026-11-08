package com.orbit.app.core.designsystem.components

import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.orbit.app.core.designsystem.motion.OrbitMotionTokens
import com.orbit.app.core.designsystem.motion.rememberOrbitInfiniteTransition
import com.orbit.app.core.designsystem.theme.OrbitTheme

/**
 * OrbitBackground - Futuristic animated background featuring an aurora gradient
 * and slowly drifting ambient glow blobs. Zero per-frame allocations.
 * Disables drifting animation when reducedMotion is active.
 */
@Composable
fun OrbitBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val colors = OrbitTheme.colors
    val infiniteTransition = rememberOrbitInfiniteTransition(label = "OrbitBackgroundTransition")

    val blobOffset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = tween(OrbitMotionTokens.DurationAmbient, easing = OrbitMotionTokens.EasingLinear),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "blob1Offset"
    )

    val blobOffset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -80f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = tween(4000, easing = OrbitMotionTokens.EasingLinear),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "blob2Offset"
    )

    val primaryGlow = colors.glowPrimary
    val secondaryGlow = colors.glowSecondary

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.auroraGradient)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Blob 1 (Top-Left primary glow)
            drawCircle(
                color = primaryGlow,
                radius = width * 0.45f,
                center = Offset(width * 0.2f + blobOffset1, height * 0.25f + blobOffset2)
            )

            // Blob 2 (Bottom-Right secondary glow)
            drawCircle(
                color = secondaryGlow,
                radius = width * 0.4f,
                center = Offset(width * 0.8f - blobOffset2, height * 0.75f - blobOffset1)
            )
        }

        content()
    }
}
