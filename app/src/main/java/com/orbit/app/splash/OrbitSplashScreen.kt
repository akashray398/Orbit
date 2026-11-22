package com.orbit.app.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orbit.app.core.designsystem.components.AgentOrb
import com.orbit.app.core.designsystem.components.GlassCard
import com.orbit.app.core.designsystem.components.OrbitBackground
import com.orbit.app.core.designsystem.motion.OrbitMotionTokens
import com.orbit.app.core.designsystem.motion.rememberOrbitInfiniteTransition
import com.orbit.app.core.designsystem.theme.DisplayFontFamily
import com.orbit.app.core.designsystem.theme.OrbitTheme
import com.orbit.app.core.domain.model.AgentState
import kotlinx.coroutines.delay

/**
 * Executive Single Splash Screen featuring a 3D metallic glowing planet sphere,
 * scale-and-fade brand wordmark, glowing progress indicator, and frosted glass card.
 */
@Composable
fun OrbitSplashScreen(
    onSplashComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alphaAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0.88f) }
    val progressAnim = remember { Animatable(0f) }
    val infiniteTransition = rememberOrbitInfiniteTransition(label = "SplashOrbLevitation")

    val floatOffsetY by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = OrbitMotionTokens.EasingLinear),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orbLevitation"
    )

    LaunchedEffect(Unit) {
        alphaAnim.animateTo(1f, animationSpec = tween(700, easing = OrbitMotionTokens.EasingLinear))
        scaleAnim.animateTo(1f, animationSpec = OrbitMotionTokens.bouncy())
        progressAnim.animateTo(1f, animationSpec = tween(2200, easing = OrbitMotionTokens.EasingLinear))

        delay(300L)
        onSplashComplete()
    }

    OrbitTheme(darkTheme = true) {
        OrbitBackground(modifier = modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .padding(24.dp)
                        .graphicsLayer {
                            alpha = alphaAnim.value
                            scaleX = scaleAnim.value
                            scaleY = scaleAnim.value
                        }
                ) {
                    // Floating 3D Metallic Amber Planet Sphere
                    Box(
                        modifier = Modifier.graphicsLayer {
                            translationY = floatOffsetY
                        }
                    ) {
                        AgentOrb(
                            state = AgentState.Idle,
                            size = 150.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Gradient Brand Title "ORBIT"
                    Text(
                        text = "ORBIT",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontFamily = DisplayFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 44.sp,
                            letterSpacing = 6.sp,
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFFFFAA00), Color(0xFFFF5500), Color(0xFFFF8800))
                            )
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Personal AI Agent",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 2.sp
                        ),
                        color = Color(0xFFFF8800)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Frosted Glass Description Card
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(0.92f),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Your phone, on autopilot.",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Execute alarms, reminders, notes, tasks, calls, and notifications hands-free with voice AI commands.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(36.dp))

                    // Animated Loading Line Progress Indicator
                    Box(
                        modifier = Modifier
                            .height(3.dp)
                            .fillMaxWidth(0.35f)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0x33FF5500))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(progressAnim.value)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFFFF5500), Color(0xFFFF8800))
                                    )
                                )
                        )
                    }
                }
            }
        }
    }
}
