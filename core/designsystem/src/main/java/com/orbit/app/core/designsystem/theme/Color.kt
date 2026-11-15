package com.orbit.app.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Core Brand Palette Tokens (Vibrant Amber / Flame Orange & Obsidian Dark)
val DarkBackground = Color(0xFF080403)
val DarkSurface = Color(0xFF140A07)
val DarkSurfaceVariant = Color(0xFF22110B)
val DarkOnBackground = Color(0xFFF8FAFC)
val DarkOnSurface = Color(0xFFF8FAFC)
val DarkOnSurfaceVariant = Color(0xFFA89F9B)

val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFE2E8F0)
val LightOnBackground = Color(0xFF0F172A)
val LightOnSurface = Color(0xFF0F172A)
val LightOnSurfaceVariant = Color(0xFF64748B)

val VioletPrimaryDark = Color(0xFFFF5500) // Vibrant Amber Flame
val VioletPrimaryLight = Color(0xFFE64A00)

val CyanSecondaryDark = Color(0xFFFF8800) // Warm Gold / Sunburst
val CyanSecondaryLight = Color(0xFFD97700)

val MagentaTertiaryDark = Color(0xFFFF3344)
val MagentaTertiaryLight = Color(0xFFDC2626)

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
    glassFill = Color(0xF2160C08), // High opacity 95% Obsidian Warm Dark Surface
    glassBorderStart = Color(0x66FF5500),
    glassBorderEnd = Color(0x33FF8800),
    glowPrimary = Color(0x66FF5500),
    glowSecondary = Color(0x4DFF8800),
    success = SuccessGreen,
    warning = WarningYellow,
    danger = DangerRed,
    primaryGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFFF5500), Color(0xFFD93800))
    ),
    auroraGradient = Brush.radialGradient(
        colors = listOf(Color(0x99992200), Color(0x4D441100), DarkBackground),
        radius = 1100f
    )
)

val LightOrbitColors = OrbitColors(
    glassFill = Color(0xF2FFFFFF),
    glassBorderStart = Color(0x33E64A00),
    glassBorderEnd = Color(0x1AD97700),
    glowPrimary = Color(0x1AE64A00),
    glowSecondary = Color(0x1AD97700),
    success = SuccessGreen,
    warning = WarningYellow,
    danger = DangerRed,
    primaryGradient = Brush.linearGradient(
        colors = listOf(VioletPrimaryLight, CyanSecondaryLight)
    ),
    auroraGradient = Brush.radialGradient(
        colors = listOf(Color(0x1AE64A00), Color(0x0DD97700), LightBackground),
        radius = 1100f
    )
)

val LocalOrbitColors = staticCompositionLocalOf { DarkOrbitColors }
