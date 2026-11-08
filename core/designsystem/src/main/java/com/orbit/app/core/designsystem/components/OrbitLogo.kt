package com.orbit.app.core.designsystem.components

import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.orbit.app.core.designsystem.motion.OrbitMotionTokens
import com.orbit.app.core.designsystem.motion.rememberOrbitInfiniteTransition
import com.orbit.app.core.designsystem.theme.DisplayFontFamily
import com.orbit.app.core.designsystem.theme.OrbitTheme
import kotlin.math.cos
import kotlin.math.sin

enum class OrbitLogoMode {
    Mark,
    Wordmark,
    Mono
}

/**
 * Single-Source Geometry Constants:
 * ViewBox: 108x108. Core R=16, Ring Rx=28, Ry=12 tilted -25 degrees.
 */
object OrbitLogoGeometry {
    const val ViewBoxSize = 108f
    const val CoreRadius = 16f
    const val RingRx = 28f
    const val RingRy = 12f
    const val RingTiltDegrees = -25f
    const val SatelliteRadius = 3.5f
}

/**
 * OrbitLogo - High-precision vector logo composable rendered on Canvas.
 * Supports Mark, Wordmark, and Mono modes with satellite orbit animation.
 */
@Composable
fun OrbitLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    mode: OrbitLogoMode = OrbitLogoMode.Mark,
    animated: Boolean = true,
    tint: Color? = null,
    contentDescriptionText: String? = "Orbit Logo"
) {
    val themeColors = OrbitTheme.colors
    val primaryColor = MaterialTheme.colorScheme.primary

    val infiniteTransition = rememberOrbitInfiniteTransition(label = "OrbitLogoSatelliteOrbit")
    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = tween(
                durationMillis = if (animated) OrbitMotionTokens.DurationAmbient else 0,
                easing = OrbitMotionTokens.EasingLinear
            ),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "satelliteAngle"
    )

    Row(
        modifier = modifier.semantics {
            contentDescriptionText?.let { contentDescription = it }
        },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val scale = this.size.width / OrbitLogoGeometry.ViewBoxSize
            val center = Offset(this.size.width / 2f, this.size.height / 2f)

            val coreRadiusPx = OrbitLogoGeometry.CoreRadius * scale
            val ringRxPx = OrbitLogoGeometry.RingRx * scale
            val ringRyPx = OrbitLogoGeometry.RingRy * scale
            val strokeWidthPx = 3.5f * scale
            val satelliteRadiusPx = OrbitLogoGeometry.SatelliteRadius * scale

            val isMono = mode == OrbitLogoMode.Mono
            val monoColor = tint ?: primaryColor

            val glowOuterColor = if (isMono) monoColor.copy(alpha = 0.1f) else themeColors.glowSecondary
            val glowInnerColor = if (isMono) monoColor.copy(alpha = 0.2f) else themeColors.glowPrimary

            val coreBrush = if (isMono) {
                Brush.linearGradient(listOf(monoColor, monoColor))
            } else {
                themeColors.primaryGradient
            }

            val ringBrush = if (isMono) {
                Brush.linearGradient(listOf(monoColor, monoColor.copy(alpha = 0.4f)))
            } else {
                Brush.linearGradient(
                    colors = listOf(
                        themeColors.glassBorderStart,
                        themeColors.glassBorderEnd,
                        themeColors.glassBorderEnd.copy(alpha = 0.1f)
                    )
                )
            }

            // 1. Draw Glow Layers
            if (!isMono) {
                drawCircle(color = glowOuterColor, radius = 32f * scale, center = center)
                drawCircle(color = glowInnerColor, radius = 22f * scale, center = center)
            }

            // 2. Draw Core Sphere
            drawCircle(brush = coreBrush, radius = coreRadiusPx, center = center)

            // 3. Draw Tilted Ring Ellipse
            rotate(degrees = OrbitLogoGeometry.RingTiltDegrees, pivot = center) {
                drawOval(
                    brush = ringBrush,
                    topLeft = Offset(center.x - ringRxPx, center.y - ringRyPx),
                    size = androidx.compose.ui.geometry.Size(ringRxPx * 2f, ringRyPx * 2f),
                    style = Stroke(width = strokeWidthPx)
                )

                // 4. Draw Revolving Satellite Dot
                val radians = Math.toRadians(orbitAngle.toDouble())
                val satX = center.x + ringRxPx * cos(radians).toFloat()
                val satY = center.y + ringRyPx * sin(radians).toFloat()
                val satCenter = Offset(satX, satY)

                val satelliteColor = if (isMono) monoColor else themeColors.glowSecondary
                drawCircle(color = satelliteColor, radius = satelliteRadiusPx, center = satCenter)
                if (!isMono) {
                    drawCircle(color = Color.White, radius = satelliteRadiusPx * 0.4f, center = satCenter)
                }
            }
        }

        if (mode == OrbitLogoMode.Wordmark) {
            Spacer(modifier = Modifier.width(size * 0.25f))
            Text(
                text = "Orbit",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = DisplayFontFamily
                ),
                color = tint ?: MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
