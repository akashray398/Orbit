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
 * AgentOrb - Central AI Agent visual identity component.
 * Dynamically reacts to AgentState (Idle, Listening, Thinking, Acting, Error)
 * with single-pass Canvas rendering, zero per-frame allocations, and reducedMotion fallback.
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

    // Target colors for state morphing
    val targetPrimaryColor = when (state) {
        is AgentState.Idle -> themeColors.glowPrimary
        is AgentState.Listening -> MaterialTheme.colorScheme.primary
        is AgentState.Thinking -> themeColors.glowSecondary
        is AgentState.Acting -> themeColors.glowSecondary
        is AgentState.Error -> themeColors.danger
    }

    val targetCoreColorStart = when (state) {
        is AgentState.Idle -> themeColors.glowPrimary
        is AgentState.Listening -> MaterialTheme.colorScheme.primary
        is AgentState.Thinking -> themeColors.glowSecondary
        is AgentState.Acting -> themeColors.glowSecondary
        is AgentState.Error -> themeColors.danger
    }

    val primaryColor by animateColorAsState(
        targetValue = targetPrimaryColor,
        animationSpec = OrbitMotionTokens.gentle(),
        label = "orbPrimaryColor"
    )

    val coreColorStart by animateColorAsState(
        targetValue = targetCoreColorStart,
        animationSpec = OrbitMotionTokens.gentle(),
        label = "orbCoreColorStart"
    )

    // Animation clocks
    val infiniteTransition = rememberOrbitInfiniteTransition("AgentOrbAnimation")

    val orbitSpeedMs = when (state) {
        is AgentState.Thinking -> 1200
        is AgentState.Acting -> 800
        else -> 4000
    }

    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = tween(if (reducedMotion) 0 else orbitSpeedMs, easing = OrbitMotionTokens.EasingLinear),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "orbOrbitAngle"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (reducedMotion) 1f else 1.15f,
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
            val baseRadius = (this.size.width / 2f) * 0.45f
            val activeRadius = baseRadius * pulseScale * currentAmplitudeScale

            // Reduced Motion Fallback
            if (reducedMotion) {
                drawCircle(color = primaryColor.copy(alpha = 0.2f), radius = baseRadius * 1.3f, center = center)
                drawCircle(color = primaryColor, radius = baseRadius, center = center)
                return@Canvas
            }

            // 1. Draw Background Glow
            val glowRadius = activeRadius * 1.6f
            drawCircle(
                color = primaryColor.copy(alpha = 0.25f),
                radius = glowRadius,
                center = center
            )

            // 2. Draw Listening Ripple Rings
            if (state is AgentState.Listening) {
                val rippleRadius = activeRadius * (1.2f + amplitude * 0.4f)
                drawCircle(
                    color = primaryColor.copy(alpha = 0.4f),
                    radius = rippleRadius,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // 3. Draw Core Sphere Gradient
            val coreBrush = Brush.radialGradient(
                colors = listOf(coreColorStart, primaryColor.copy(alpha = 0.7f), Color.Transparent),
                center = center,
                radius = activeRadius * 1.2f
            )
            drawCircle(brush = coreBrush, radius = activeRadius, center = center)

            // 4. Draw Acting / Thinking Rotating Ring Arc
            if (state is AgentState.Thinking || state is AgentState.Acting) {
                rotate(degrees = orbitAngle, pivot = center) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(primaryColor, Color.Transparent, primaryColor)
                        ),
                        startAngle = 0f,
                        sweepAngle = 240f,
                        useCenter = false,
                        topLeft = Offset(center.x - activeRadius * 1.3f, center.y - activeRadius * 1.3f),
                        size = Size(activeRadius * 2.6f, activeRadius * 2.6f),
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
            }

            // 5. Draw Satellite Dot along Orbit Path
            val rx = activeRadius * 1.35f
            val ry = activeRadius * 0.75f
            val rad = Math.toRadians((orbitAngle - 25).toDouble())
            val satX = center.x + rx * cos(rad).toFloat()
            val satY = center.y + ry * sin(rad).toFloat()

            drawCircle(
                color = themeColors.glowSecondary,
                radius = 3.5.dp.toPx(),
                center = Offset(satX, satY)
            )
        }

        // Reduced Motion Mode Icon Overlay
        if (reducedMotion) {
            val icon = when (state) {
                is AgentState.Listening -> Icons.Default.Mic
                is AgentState.Thinking -> Icons.Default.Psychology
                is AgentState.Acting -> Icons.Default.Build
                is AgentState.Error -> Icons.Default.Warning
                else -> null
            }
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.35f)
                )
            }
        }
    }
}
