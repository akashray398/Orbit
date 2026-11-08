package com.orbit.app.core.designsystem.motion

import android.content.Context
import android.provider.Settings
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import com.orbit.app.core.designsystem.theme.LocalOrbitColors

object OrbitMotionTokens {
    const val DurationInstant = 100
    const val DurationFast = 200
    const val DurationMedium = 350
    const val DurationSlow = 600
    const val DurationAmbient = 3000

    val EasingStandard: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val EasingEmphasizedDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    val EasingEmphasizedAccelerate: Easing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
    val EasingLinear: Easing = LinearEasing

    fun <T> gentle(): SpringSpec<T> = spring(
        stiffness = Spring.StiffnessLow,
        dampingRatio = Spring.DampingRatioNoBouncy
    )

    fun <T> bouncy(): SpringSpec<T> = spring(
        stiffness = Spring.StiffnessMediumLow,
        dampingRatio = Spring.DampingRatioMediumBouncy
    )

    fun <T> snappy(): SpringSpec<T> = spring(
        stiffness = Spring.StiffnessHigh,
        dampingRatio = Spring.DampingRatioNoBouncy
    )
}

@Immutable
data class OrbitMotionData(
    val reducedMotion: Boolean = false
)

val LocalOrbitMotion = staticCompositionLocalOf { OrbitMotionData() }

@Composable
fun isSystemReducedMotionEnabled(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        try {
            val scale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            )
            scale == 0f
        } catch (_: Exception) {
            false
        }
    }
}

fun Modifier.pressScale(
    enabled: Boolean = true,
    pressedScale: Float = 0.96f
): Modifier = composed {
    val reducedMotion = LocalOrbitMotion.current.reducedMotion
    if (!enabled || reducedMotion) return@composed this

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1.0f,
        animationSpec = OrbitMotionTokens.bouncy(),
        label = "pressScaleAnimation"
    )

    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

fun Modifier.shimmer(): Modifier = composed {
    val reducedMotion = LocalOrbitMotion.current.reducedMotion
    val colors = LocalOrbitColors.current

    if (reducedMotion) {
        return@composed this.background(
            Brush.linearGradient(
                listOf(colors.glassFill, colors.glassFill)
            )
        )
    }

    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = OrbitMotionTokens.DurationAmbient,
                easing = OrbitMotionTokens.EasingLinear
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslation"
    )

    val shimmerColors = listOf(
        colors.glassFill,
        colors.glowPrimary.copy(alpha = 0.4f),
        colors.glassFill
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 200f, translateAnim - 200f),
        end = Offset(translateAnim, translateAnim)
    )

    this.background(brush)
}

@Composable
fun rememberOrbitInfiniteTransition(
    label: String = "InfiniteTransition"
): OrbitInfiniteTransition {
    val reducedMotion = LocalOrbitMotion.current.reducedMotion
    val transition = rememberInfiniteTransition(label = label)
    return remember(transition, reducedMotion) {
        OrbitInfiniteTransition(transition, reducedMotion)
    }
}

class OrbitInfiniteTransition(
    val transition: InfiniteTransition,
    val reducedMotion: Boolean
) {
    @Composable
    fun animateFloat(
        initialValue: Float,
        targetValue: Float,
        animationSpec: InfiniteRepeatableSpec<Float>,
        label: String = "FloatAnimation"
    ): State<Float> {
        return if (reducedMotion) {
            remember(initialValue) { mutableFloatStateOf(initialValue) }
        } else {
            transition.animateFloat(
                initialValue = initialValue,
                targetValue = targetValue,
                animationSpec = animationSpec,
                label = label
            )
        }
    }
}
