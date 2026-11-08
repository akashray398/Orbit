package com.orbit.app.core.designsystem.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.orbit.app.core.designsystem.theme.OrbitTheme

/*
 * Backdrop Blur Limitation in Android / Jetpack Compose & Fallback Strategy:
 * -----------------------------------------------------------------------
 * Prior to Android 12 (API 31), hardware-accelerated RenderEffect backdrop blur was not
 * supported natively by the Android View hierarchy without expensive off-screen bitmap copy passes.
 * In Jetpack Compose, `Modifier.blur()` applies a blur effect to content rendered inside the target node,
 * but API < 31 devices do not support real-time backdrop sampling behind composables.
 *
 * Fallback Approach:
 * On API 31+, we apply `Modifier.blur(16.dp)` combined with a translucent fill.
 * On API 26-30, we fall back gracefully to a semi-translucent glass fill (`colors.glassFill`)
 * with a subtle 1dp linear gradient border (`colors.glassBorderStart` to `colors.glassBorderEnd`),
 * achieving an elegant frosted glass aesthetic without performance bottlenecks or frame drops.
 */

/**
 * GlassCard - Translucent container with gradient border, rounded corners, optional glow,
 * and backdrop blur fallback handling.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = OrbitTheme.shapes.medium,
    enableGlow: Boolean = false,
    glowElevation: Dp = 12.dp,
    borderWidth: Dp = 1.dp,
    borderBrush: Brush = Brush.horizontalGradient(
        colors = listOf(
            OrbitTheme.colors.glassBorderStart,
            OrbitTheme.colors.glassBorderEnd
        )
    ),
    content: @Composable BoxScope.() -> Unit
) {
    val colors = OrbitTheme.colors

    val blurModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Modifier.blur(16.dp)
    } else {
        Modifier
    }

    val glowModifier = if (enableGlow) {
        Modifier.shadow(
            elevation = glowElevation,
            shape = shape,
            ambientColor = colors.glowPrimary,
            spotColor = colors.glowSecondary
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .then(glowModifier)
            .clip(shape)
            .then(blurModifier)
            .background(colors.glassFill, shape = shape)
            .border(width = borderWidth, brush = borderBrush, shape = shape)
    ) {
        content()
    }
}
