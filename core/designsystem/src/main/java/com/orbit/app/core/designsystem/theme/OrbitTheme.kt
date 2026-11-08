package com.orbit.app.core.designsystem.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.orbit.app.core.designsystem.motion.LocalOrbitMotion
import com.orbit.app.core.designsystem.motion.OrbitMotionData
import com.orbit.app.core.designsystem.motion.isSystemReducedMotionEnabled

private val DarkMaterialColorScheme = darkColorScheme(
    primary = VioletPrimaryDark,
    secondary = CyanSecondaryDark,
    tertiary = MagentaTertiaryDark,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant
)

private val LightMaterialColorScheme = lightColorScheme(
    primary = VioletPrimaryLight,
    secondary = CyanSecondaryLight,
    tertiary = MagentaTertiaryLight,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onBackground = LightOnBackground,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant
)

@Composable
fun OrbitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    reducedMotion: Boolean = isSystemReducedMotionEnabled(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkMaterialColorScheme
        else -> LightMaterialColorScheme
    }

    val orbitColors = if (darkTheme) DarkOrbitColors else LightOrbitColors
    val orbitSpacing = OrbitSpacing()
    val orbitElevation = OrbitElevation()
    val orbitMotion = OrbitMotionData(reducedMotion = reducedMotion)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalOrbitColors provides orbitColors,
        LocalOrbitSpacing provides orbitSpacing,
        LocalOrbitElevation provides orbitElevation,
        LocalOrbitMotion provides orbitMotion
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = OrbitTypography,
            shapes = OrbitShapes,
            content = content
        )
    }
}

object OrbitTheme {
    val colors: OrbitColors
        @Composable
        @ReadOnlyComposable
        get() = LocalOrbitColors.current

    val spacing: OrbitSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalOrbitSpacing.current

    val elevation: OrbitElevation
        @Composable
        @ReadOnlyComposable
        get() = LocalOrbitElevation.current

    val motion: OrbitMotionData
        @Composable
        @ReadOnlyComposable
        get() = LocalOrbitMotion.current

    val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography

    val shapes: Shapes
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.shapes
}
