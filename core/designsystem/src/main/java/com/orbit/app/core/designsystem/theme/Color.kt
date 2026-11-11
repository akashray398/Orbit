package com.orbit.app.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Core Brand Palette Tokens
val DarkBackground = Color(0xFF070B1A)
val DarkSurface = Color(0xFF0F172A)
val DarkSurfaceVariant = Color(0xFF1E293B)
val DarkOnBackground = Color(0xFFF8FAFC)
val DarkOnSurface = Color(0xFFF8FAFC)
val DarkOnSurfaceVariant = Color(0xFF94A3B8)

val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFE2E8F0)
val LightOnBackground = Color(0xFF0F172A)
val LightOnSurface = Color(0xFF0F172A)
val LightOnSurfaceVariant = Color(0xFF64748B)

val VioletPrimaryDark = Color(0xFF7C5CFF)
val VioletPrimaryLight = Color(0xFF6D28D9)

val CyanSecondaryDark = Color(0xFF22D3EE)
val CyanSecondaryLight = Color(0xFF0891B2)

val MagentaTertiaryDark = Color(0xFFF43F5E)
val MagentaTertiaryLight = Color(0xFFE11D48)

val SuccessGreen = Color(0xFF10B981)
val WarningYellow = Color(0xFFF59E0B)
val DangerRed = Color(0xFFEF4444)

@Immutable
data class OrbitColors(
    val glassFill: Color,
    val glassBorderStart: Color,
    val glassBorderEnd: Color,
    val glowPrimary: Color,
    val glowSecondary: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val primaryGradient: Brush,
    val auroraGradient: Brush
)

val DarkOrbitColors = OrbitColors(
    glassFill = Color(0xEE0F172A), // High opacity 93% Slate Dark Surface for high contrast
    glassBorderStart = Color(0x667C5CFF),
    glassBorderEnd = Color(0x3322D3EE),
    glowPrimary = Color(0x337C5CFF),
    glowSecondary = Color(0x3322D3EE),
    success = SuccessGreen,
    warning = WarningYellow,
    danger = DangerRed,
    primaryGradient = Brush.linearGradient(
        colors = listOf(VioletPrimaryDark, CyanSecondaryDark)
    ),
    auroraGradient = Brush.radialGradient(
        colors = listOf(Color(0x267C5CFF), Color(0x1A22D3EE), DarkBackground),
        radius = 1200f
    )
)

val LightOrbitColors = OrbitColors(
    glassFill = Color(0xF2FFFFFF), // High opacity 95% White Surface
    glassBorderStart = Color(0x336D28D9),
    glassBorderEnd = Color(0x1A0891B2),
    glowPrimary = Color(0x1A6D28D9),
    glowSecondary = Color(0x1A0891B2),
    success = SuccessGreen,
    warning = WarningYellow,
    danger = DangerRed,
    primaryGradient = Brush.linearGradient(
        colors = listOf(VioletPrimaryLight, CyanSecondaryLight)
    ),
    auroraGradient = Brush.radialGradient(
        colors = listOf(Color(0x1A6D28D9), Color(0x0D0891B2), LightBackground),
        radius = 1200f
    )
)

val LocalOrbitColors = staticCompositionLocalOf { DarkOrbitColors }
