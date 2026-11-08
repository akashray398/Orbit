package com.orbit.app.feature.onboarding.ui

import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.orbit.app.core.designsystem.components.GlassCard
import com.orbit.app.core.designsystem.components.OrbitLogo
import com.orbit.app.core.designsystem.components.OrbitLogoMode
import com.orbit.app.core.designsystem.motion.LocalOrbitMotion
import com.orbit.app.core.designsystem.motion.OrbitMotionTokens
import com.orbit.app.core.designsystem.motion.rememberOrbitInfiniteTransition
import com.orbit.app.core.designsystem.theme.OrbitTheme

@Composable
fun OnboardingIllustrationPage1(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(180.dp)
            .semantics { contentDescription = "Orbit AI Agent Illustration" },
        contentAlignment = Alignment.Center
    ) {
        OrbitLogo(
            size = 140.dp,
            mode = OrbitLogoMode.Mark,
            animated = true
        )
    }
}

@Composable
fun OnboardingIllustrationPage2(modifier: Modifier = Modifier) {
    val reducedMotion = LocalOrbitMotion.current.reducedMotion
    val infiniteTransition = rememberOrbitInfiniteTransition("ChatBubbleTransition")

    val alphaBubble2 by infiniteTransition.animateFloat(
        initialValue = if (reducedMotion) 1f else 0.4f,
        targetValue = 1f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = tween(OrbitMotionTokens.DurationMedium, easing = OrbitMotionTokens.EasingStandard),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "bubble2Alpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .semantics { contentDescription = "Voice and Text Commands Illustration" },
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "\"Set a reminder for 5 PM\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = OrbitTheme.colors.success,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Reminder set for 5:00 PM",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun OnboardingIllustrationPage3(modifier: Modifier = Modifier) {
    val themeColors = OrbitTheme.colors
    val reducedMotion = LocalOrbitMotion.current.reducedMotion
    val transition = rememberOrbitInfiniteTransition("ShieldPulseTransition")

    val pulseRadius by transition.animateFloat(
        initialValue = 50f,
        targetValue = if (reducedMotion) 50f else 75f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = tween(1500, easing = OrbitMotionTokens.EasingStandard),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "shieldPulse"
    )

    Box(
        modifier = modifier
            .size(160.dp)
            .semantics { contentDescription = "User Control and Confirmation Motif" },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(160.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            drawCircle(
                color = themeColors.glowPrimary,
                radius = pulseRadius,
                center = center
            )
            drawCircle(
                color = themeColors.glassBorderStart,
                radius = 55f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
        }
        Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )
    }
}

@Composable
fun OnboardingIllustrationPage4(modifier: Modifier = Modifier) {
    val themeColors = OrbitTheme.colors
    val reducedMotion = LocalOrbitMotion.current.reducedMotion
    val transition = rememberOrbitInfiniteTransition("LockGlowTransition")

    val glowAlpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = if (reducedMotion) 0.2f else 0.6f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = tween(1800, easing = OrbitMotionTokens.EasingStandard),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "lockGlow"
    )

    Box(
        modifier = modifier
            .size(160.dp)
            .semantics { contentDescription = "Private by Design Lock Motif" },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(160.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            drawCircle(
                color = themeColors.glowSecondary.copy(alpha = glowAlpha),
                radius = 60f,
                center = center
            )
        }
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = OrbitTheme.colors.success,
            modifier = Modifier.size(60.dp)
        )
    }
}
