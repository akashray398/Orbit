package com.orbit.app.core.designsystem.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.orbit.app.core.designsystem.motion.LocalOrbitMotion
import com.orbit.app.core.designsystem.motion.OrbitMotionTokens
import com.orbit.app.core.designsystem.motion.rememberOrbitInfiniteTransition
import com.orbit.app.core.designsystem.theme.OrbitTheme

/**
 * OrbitBackground - Futuristic background featuring a dark aurora gradient
 * and two glowing ambient blobs (Violet & Cyan) with smooth floating motion.
 */
@Composable
fun OrbitBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val colors = OrbitTheme.colors
    val reducedMotion = LocalOrbitMotion.current.reducedMotion
    val infiniteTransition = rememberOrbitInfiniteTransition(label = "OrbitBackgroundTransition")

    val blobOffset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (reducedMotion) 0f else 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = OrbitMotionTokens.EasingLinear),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob1Offset"
    )

    val blobOffset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (reducedMotion) 0f else -50f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = OrbitMotionTokens.EasingLinear),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob2Offset"
    )

    // Luminous Electric Violet and Cyan glow colors
    val violetGlow = Color(0x407C5CFF)
    val cyanGlow = Color(0x3D22D3EE)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.auroraGradient)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val r1 = width * 0.48f
            val r2 = width * 0.42f

            val center1 = Offset(width * 0.2f + blobOffset1, height * 0.25f + blobOffset2)
            val center2 = Offset(width * 0.8f - blobOffset2, height * 0.75f - blobOffset1)

            // Violet Ambient Blob (Top-Left)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        violetGlow,
                        violetGlow.copy(alpha = 0.18f),
                        Color.Transparent
                    ),
                    center = center1,
                    radius = r1
                ),
                radius = r1,
                center = center1
            )

            // Cyan Ambient Blob (Bottom-Right)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        cyanGlow,
                        cyanGlow.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = center2,
                    radius = r2
                ),
                radius = r2,
                center = center2
            )
        }

        content()
    }
}
