package com.orbit.app.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 * Unified Single Splash Screen in Deep Dark Navy Theme.
 * Displays the live floating agent orb, brand title, and tagline description
 * before transitioning directly into the main application.
 */
@Composable
fun OrbitSplashScreen(
    onSplashComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alphaAnim = remember { Animatable(0f) }
    val offsetYAnim = remember { Animatable(20f) }
    val infiniteTransition = rememberOrbitInfiniteTransition(label = "SplashOrbFloating")

    val floatOffsetY by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = OrbitMotionTokens.EasingLinear),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orbFloat"
    )

    LaunchedEffect(Unit) {
        alphaAnim.animateTo(1f, animationSpec = tween(600, easing = OrbitMotionTokens.EasingLinear))
        offsetYAnim.animateTo(0f, animationSpec = tween(600, easing = OrbitMotionTokens.EasingStandard))

        delay(1800L)
        onSplashComplete()
    }

    // Force Dark Theme for the Splash Screen to prevent any white/bright flash on cold start
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
                            translationY = offsetYAnim.value
                        }
                ) {
                    // Live Motion Agent Orb
                    Box(
                        modifier = Modifier.graphicsLayer {
                            translationY = floatOffsetY
                        }
                    ) {
                        AgentOrb(
                            state = AgentState.Idle,
                            size = 130.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Orbit Brand Title
                    Text(
                        text = "ORBIT",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontFamily = DisplayFontFamily,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 4.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Personal AI Agent",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        ),
                        color = OrbitTheme.colors.glowSecondary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Glass Description Card
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(0.92f)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Your phone on autopilot.",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Manage alarms, reminders, notes, tasks, and notifications hands-free with voice AI commands.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
