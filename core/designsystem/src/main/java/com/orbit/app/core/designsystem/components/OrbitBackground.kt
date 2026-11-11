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
 * OrbitBackground - Futuristic dark navy background featuring two vibrant,
 * glowing floating ambient blobs (Electric Violet & Cyan) with smooth radial falloff.
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
        targetValue = if (reducedMotion) 0f else 70f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = OrbitMotionTokens.EasingLinear),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob1Offset"
    )

    val blobOffset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (reducedMotion) 0f else -60f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = OrbitMotionTokens.EasingLinear),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob2Offset"
    )

    // Luminous, vibrant Electric Violet and Cyan glow colors
    val violetGlowCenter = Color(0x807C5CFF) // 50% opacity Electric Violet
    val violetGlowEdge = Color(0x227C5CFF)

    val cyanGlowCenter = Color(0x7022D3EE)   // 44% opacity Cyan
    val cyanGlowEdge = Color(0x1F22D3EE)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.auroraGradient)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val r1 = width * 0.55f
            val r2 = width * 0.50f

            val center1 = Offset(width * 0.15f + blobOffset1, height * 0.22f + blobOffset2)
            val center2 = Offset(width * 0.85f - blobOffset2, height * 0.78f - blobOffset1)

            // Vibrant Violet Ambient Glow Blob (Top-Left)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        violetGlowCenter,
                        violetGlowEdge,
                        Color.Transparent
                    ),
                    center = center1,
                    radius = r1
                ),
                radius = r1,
                center = center1
            )

            // Vibrant Cyan Ambient Glow Blob (Bottom-Right)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        cyanGlowCenter,
                        cyanGlowEdge,
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
