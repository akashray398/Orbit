package com.orbit.app.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.orbit.app.core.designsystem.theme.OrbitTheme

/**
 * GlassCard - Translucent container with gradient border, rounded corners, optional glow,
 * and high-contrast frosted background. Text inside is 100% crisp and unblurred across all API levels.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = OrbitTheme.shapes.medium,
    enableGlow: Boolean = false,
    glowElevation: Dp = 8.dp,
    borderWidth: Dp = 1.dp,
    containerColor: Color = OrbitTheme.colors.glassFill,
    borderBrush: Brush = Brush.horizontalGradient(
        colors = listOf(
            OrbitTheme.colors.glassBorderStart,
            OrbitTheme.colors.glassBorderEnd
        )
    ),
    content: @Composable BoxScope.() -> Unit
) {
    val colors = OrbitTheme.colors

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
            .background(containerColor, shape = shape)
            .border(width = borderWidth, brush = borderBrush, shape = shape)
    ) {
        content()
    }
}
