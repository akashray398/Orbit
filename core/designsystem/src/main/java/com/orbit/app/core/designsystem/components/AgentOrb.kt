package com.orbit.app.core.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.orbit.app.core.designsystem.motion.LocalOrbitMotion
import com.orbit.app.core.designsystem.motion.OrbitMotionTokens
import com.orbit.app.core.designsystem.motion.rememberOrbitInfiniteTransition
import com.orbit.app.core.designsystem.theme.OrbitTheme
import com.orbit.app.core.domain.model.AgentState
import kotlin.math.cos
import kotlin.math.sin

/**
 * AgentOrb - 3D Metallic Amber Planet Sphere matching the exact mockup design.
 * Rendered with 3D spherical lighting, specular highlights, and state-driven liquid motion.
 */
@Composable
fun AgentOrb(
    state: AgentState,
    modifier: Modifier = Modifier,
    amplitude: Float = 0f,
    size: Dp = 120.dp,
    contentDescriptionText: String? = "Agent Orb"
) {
    val reducedMotion = LocalOrbitMotion.current.reducedMotion
    val themeColors = OrbitTheme.colors

    // Target glow color based on state
    val targetPrimaryColor = when (state) {
        is AgentState.Idle -> Color(0xFFFF5500)
        is AgentState.Listening -> Color(0xFFFF8800)
        is AgentState.Thinking -> Color(0xFFFFBB00)
        is AgentState.Acting -> Color(0xFFFF8800)
        is AgentState.Error -> themeColors.danger
    }

    val primaryColor by animateColorAsState(
        targetValue = targetPrimaryColor,
        animationSpec = OrbitMotionTokens.gentle(),
        label = "orbPrimaryColor"
    )

    val infiniteTransition = rememberOrbitInfiniteTransition("AgentOrbAnimation")

    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = tween(if (reducedMotion) 0 else 3000, easing = OrbitMotionTokens.EasingLinear),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "orbOrbitAngle"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (reducedMotion) 1f else 1.08f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = tween(OrbitMotionTokens.DurationSlow, easing = OrbitMotionTokens.EasingStandard),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "orbPulseScale"
    )

    val currentAmplitudeScale by animateFloatAsState(
        targetValue = 1.0f + (amplitude.coerceIn(0f, 1f) * 0.35f),
        animationSpec = OrbitMotionTokens.snappy(),
        label = "orbAmplitudeScale"
    )

    Box(
        modifier = modifier
            .size(size)
            .semantics { contentDescriptionText?.let { contentDescription = it } },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = (this.size.width / 2f) * 0.48f
            val activeRadius = baseRadius * pulseScale * currentAmplitudeScale

            // 1. Soft Ambient Outer Glow Aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.4f), primaryColor.copy(alpha = 0.1f), Color.Transparent),
                    center = center,
                    radius = activeRadius * 1.7f
                ),
                radius = activeRadius * 1.7f,
                center = center
            )

            // 2. Listening Ripple Ring
            if (state is AgentState.Listening) {
                val rippleRadius = activeRadius * (1.15f + amplitude * 0.3f)
                drawCircle(
                    color = primaryColor.copy(alpha = 0.5f),
                    radius = rippleRadius,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // 3. 3D Metallic Planet Sphere Body (Top-Left Light Specular Highlight)
            val lightSourceOffset = Offset(center.x - activeRadius * 0.35f, center.y - activeRadius * 0.35f)
            val sphereBrush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFFDD88), // Specular light highlight point
                    primaryColor,       // Mid-body Flame Amber
                    Color(0xFFC83200),
                    Color(0xFF4A1002), // Dark shaded shadow edge
                    Color(0xFF180300)
                ),
                center = lightSourceOffset,
                radius = activeRadius * 1.35f
            )

            drawCircle(brush = sphereBrush, radius = activeRadius, center = center)

            // 4. Subtle Specular Shine Edge
            val specularOffset = Offset(center.x - activeRadius * 0.28f, center.y - activeRadius * 0.28f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.65f), Color.Transparent),
                    center = specularOffset,
                    radius = activeRadius * 0.45f
                ),
                radius = activeRadius * 0.45f,
                center = specularOffset
            )

            // 5. Acting / Thinking Orbit Ring Arc
            if (state is AgentState.Thinking || state is AgentState.Acting) {
                rotate(degrees = orbitAngle, pivot = center) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(primaryColor, Color.Transparent, Color.White, primaryColor)
                        ),
                        startAngle = 0f,
                        sweepAngle = 220f,
                        useCenter = false,
                        topLeft = Offset(center.x - activeRadius * 1.25f, center.y - activeRadius * 1.25f),
                        size = Size(activeRadius * 2.5f, activeRadius * 2.5f),
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                }
            }
        }
    }
}
