package com.orbit.app.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.orbit.app.core.designsystem.motion.pressScale
import com.orbit.app.core.designsystem.theme.OrbitTheme

enum class OrbitButtonVariant {
    Primary,
    Secondary,
    Text
}

enum class OrbitButtonSize {
    Small,
    Medium,
    Large
}

/**
 * OrbitButton - Primary, Secondary, and Text button variants with motion, haptics,
 * loading indicator, and accessibility semantics.
 */
@Composable
fun OrbitButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: OrbitButtonVariant = OrbitButtonVariant.Primary,
    size: OrbitButtonSize = OrbitButtonSize.Medium,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
    contentDescriptionText: String? = null,
    content: @Composable () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val colors = OrbitTheme.colors
    val shapes = OrbitTheme.shapes

    val shape = when (size) {
        OrbitButtonSize.Small -> shapes.small
        OrbitButtonSize.Medium -> shapes.medium
        OrbitButtonSize.Large -> shapes.large
    }

    val paddingValues = when (size) {
        OrbitButtonSize.Small -> PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        OrbitButtonSize.Medium -> PaddingValues(horizontal = 20.dp, vertical = 10.dp)
        OrbitButtonSize.Large -> PaddingValues(horizontal = 28.dp, vertical = 14.dp)
    }

    val minHeight = when (size) {
        OrbitButtonSize.Small -> 36.dp
        OrbitButtonSize.Medium -> 48.dp
        OrbitButtonSize.Large -> 56.dp
    }

    val textStyle = when (size) {
        OrbitButtonSize.Small -> MaterialTheme.typography.labelMedium
        OrbitButtonSize.Medium -> MaterialTheme.typography.labelLarge
        OrbitButtonSize.Large -> MaterialTheme.typography.titleMedium
    }

    val isClickable = enabled && !loading

    val modifierWithBackground = when (variant) {
        OrbitButtonVariant.Primary -> {
            if (enabled) {
                modifier
                    .shadow(elevation = 8.dp, shape = shape, ambientColor = colors.glowPrimary, spotColor = colors.glowPrimary)
                    .background(colors.primaryGradient, shape = shape)
            } else {
                modifier.background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), shape = shape)
            }
        }
        OrbitButtonVariant.Secondary -> {
            if (enabled) {
                modifier
                    .background(colors.glassFill, shape = shape)
                    .border(width = 1.dp, brush = colors.primaryGradient, shape = shape)
            } else {
                modifier
                    .background(colors.glassFill.copy(alpha = 0.05f), shape = shape)
                    .border(width = 1.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), shape = shape)
            }
        }
        OrbitButtonVariant.Text -> {
            modifier
        }
    }

    val contentColor = when (variant) {
        OrbitButtonVariant.Primary -> if (enabled) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        OrbitButtonVariant.Secondary -> if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        OrbitButtonVariant.Text -> if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    }

    Box(
        modifier = modifierWithBackground
            .clip(shape)
            .pressScale(enabled = isClickable)
            .defaultMinSize(minHeight = minHeight)
            .clickable(
                enabled = isClickable,
                role = Role.Button,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            )
            .padding(paddingValues)
            .semantics {
                role = Role.Button
                contentDescriptionText?.let { contentDescription = it }
            },
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
        } else {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                ProvideTextStyle(value = textStyle) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (leadingIcon != null) {
                            leadingIcon()
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        content()
                    }
                }
            }
        }
    }
}
