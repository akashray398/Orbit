package com.orbit.app.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orbit.app.core.designsystem.components.OrbitBackground
import com.orbit.app.core.designsystem.components.OrbitLogoGeometry
import com.orbit.app.core.designsystem.motion.LocalOrbitMotion
import com.orbit.app.core.designsystem.motion.OrbitMotionTokens
import com.orbit.app.core.designsystem.motion.SplashMotionSpec
import com.orbit.app.core.designsystem.theme.DisplayFontFamily
import com.orbit.app.core.designsystem.theme.OrbitTheme
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

private data class IntroParticle(
    val angleRad: Float,
    val speed: Float,
    val radius: Float
)

@Composable
fun OrbitIntroScreen(
    onIntroComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reducedMotion = LocalOrbitMotion.current.reducedMotion
    val progress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val themeColors = OrbitTheme.colors

    val particles = remember {
        Array(SplashMotionSpec.ParticleCount) { index ->
            val angle = (index * (360f / SplashMotionSpec.ParticleCount)) * (Math.PI / 180f).toFloat()
            val speed = 30f + (index % 5) * 15f
            val radius = 2f + (index % 3) * 1.5f
            IntroParticle(angleRad = angle, speed = speed, radius = radius)
        }
    }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = SplashMotionSpec.TotalIntroDurationMs,
                easing = OrbitMotionTokens.EasingLinear
            )
        )
        onIntroComplete()
    }

    val currentProgress = progress.value
    val elapsedTimeMs = (currentProgress * SplashMotionSpec.TotalIntroDurationMs).toLong()
    val isTapToSkipAvailable = elapsedTimeMs >= SplashMotionSpec.TapToSkipThresholdMs

    OrbitBackground(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = isTapToSkipAvailable
            ) {
                scope.launch {
                    progress.snapTo(1f)
                    onIntroComplete()
                }
            }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val p = currentProgress

            val ringSweepProgress = ((p - 0.0f) / SplashMotionSpec.Phase1End).coerceIn(0f, 1f)
            val coreScaleProgress = ((p - SplashMotionSpec.Phase2Start) / (SplashMotionSpec.Phase2End - SplashMotionSpec.Phase2Start)).coerceIn(0f, 1f)
            val coreScale = if (reducedMotion) 1f else (coreScaleProgress * 1.08f).coerceAtMost(1f)
            val pulseProgress = ((p - SplashMotionSpec.Phase3Start) / (SplashMotionSpec.Phase3End - SplashMotionSpec.Phase3Start)).coerceIn(0f, 1f)
            val textProgress = ((p - SplashMotionSpec.Phase4Start) / (SplashMotionSpec.Phase4End - SplashMotionSpec.Phase4Start)).coerceIn(0f, 1f)
            val textAlpha = if (reducedMotion) 1f else textProgress
            val textOffsetY = if (reducedMotion) 0f else (1f - textProgress) * 20f

            val fullOvalPath = remember { Path() }
            val segmentPath = remember { Path() }
            val pathMeasure = remember { PathMeasure() }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Animated Glowing Logo
                Canvas(modifier = Modifier.size(180.dp)) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val scale = size.width / OrbitLogoGeometry.ViewBoxSize

                    val coreRadiusPx = OrbitLogoGeometry.CoreRadius * scale * coreScale
                    val ringRxPx = OrbitLogoGeometry.RingRx * scale
                    val ringRyPx = OrbitLogoGeometry.RingRy * scale
                    val strokeWidthPx = 3.5f * scale

                    // Phase 3: Pulse Ring
                    if (!reducedMotion && pulseProgress > 0f && pulseProgress < 1f) {
                        val pulseRadius = coreRadiusPx + pulseProgress * (size.width * 0.35f)
                        val pulseAlpha = (1f - pulseProgress) * 0.4f
                        drawCircle(
                            color = themeColors.glowPrimary.copy(alpha = pulseAlpha),
                            radius = pulseRadius,
                            center = center
                        )
                    }

                    // Phase 3: Particles
                    if (!reducedMotion && pulseProgress > 0f) {
                        for (particle in particles) {
                            val dist = pulseProgress * particle.speed * scale
                            val px = center.x + cos(particle.angleRad.toDouble()).toFloat() * (coreRadiusPx + dist)
                            val py = center.y + sin(particle.angleRad.toDouble()).toFloat() * (coreRadiusPx + dist)
                            val particleAlpha = (1f - pulseProgress) * 0.7f
                            drawCircle(
                                color = themeColors.glowSecondary.copy(alpha = particleAlpha),
                                radius = particle.radius * scale,
                                center = Offset(px, py)
                            )
                        }
                    }

                    // Phase 2: Core Sphere
                    if (coreScale > 0f) {
                        drawCircle(
                            brush = themeColors.primaryGradient,
                            radius = coreRadiusPx,
                            center = center
                        )
                    }

                    // Phase 1 & 2: Ring Sweep
                    if (ringSweepProgress > 0f) {
                        rotate(degrees = OrbitLogoGeometry.RingTiltDegrees, pivot = center) {
                            fullOvalPath.reset()
                            fullOvalPath.addOval(
                                androidx.compose.ui.geometry.Rect(
                                    center.x - ringRxPx,
                                    center.y - ringRyPx,
                                    center.x + ringRxPx,
                                    center.y + ringRyPx
                                )
                            )

                            pathMeasure.setPath(fullOvalPath, false)
                            val totalLength = pathMeasure.length
                            segmentPath.reset()
                            pathMeasure.getSegment(0f, totalLength * ringSweepProgress, segmentPath, true)

                            drawPath(
                                path = segmentPath,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        themeColors.glassBorderStart,
                                        themeColors.glassBorderEnd,
                                        themeColors.glassBorderEnd.copy(alpha = 0.1f)
                                    )
                                ),
                                style = Stroke(width = strokeWidthPx)
                            )

                            if (coreScaleProgress > 0.2f) {
                                val satAngleRad = (ringSweepProgress * 360f) * (Math.PI / 180f).toFloat()
                                val satX = center.x + ringRxPx * cos(satAngleRad.toDouble()).toFloat()
                                val satY = center.y + ringRyPx * sin(satAngleRad.toDouble()).toFloat()
                                val satRadius = OrbitLogoGeometry.SatelliteRadius * scale

                                drawCircle(
                                    color = themeColors.glowSecondary,
                                    radius = satRadius,
                                    center = Offset(satX, satY)
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = satRadius * 0.4f,
                                    center = Offset(satX, satY)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Wordmark "Orbit" & Description Tagline
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.graphicsLayer {
                        alpha = textAlpha
                        translationY = textOffsetY
                    }
                ) {
                    Text(
                        text = "Orbit",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontFamily = DisplayFontFamily,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your personal AI agent on autopilot.",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}
