package com.orbit.app.assistant

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.orbit.app.MainActivity
import com.orbit.app.core.designsystem.components.AgentOrb
import com.orbit.app.core.designsystem.components.GlassCard
import com.orbit.app.core.designsystem.theme.OrbitTheme
import com.orbit.app.core.domain.model.AgentState
import com.orbit.app.core.domain.model.VoiceInputState
import com.orbit.app.core.domain.repository.AgentGateway
import com.orbit.app.core.domain.repository.VoiceInputController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AssistActivity : ComponentActivity() {

    @Inject
    lateinit var agentGateway: AgentGateway

    @Inject
    lateinit var voiceInputController: VoiceInputController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val isLocked = keyguardManager.isKeyguardLocked

        setContent {
            OrbitTheme {
                val voiceState by voiceInputController.state.collectAsStateWithLifecycle()
                val isVoiceActive = voiceState is VoiceInputState.Listening

                DisposableEffect(Unit) {
                    voiceInputController.start(com.orbit.app.core.domain.model.VoiceLocale.ENGLISH)
                    onDispose {
                        voiceInputController.stop()
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x99000000))
                        .clickable { finish() },
                    contentAlignment = Alignment.BottomCenter
                ) {
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clickable(enabled = false) {}
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AgentOrb(state = if (isVoiceActive) AgentState.Listening else AgentState.Idle, size = 48.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Orbit Assistant", style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = if (isLocked) "Lockscreen Mode (Limited)" else "Ready",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = if (isLocked) "Device is locked. Unlock to perform private actions." else "Listening for your voice command...",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { finish() },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Close")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        val intent = Intent(this@AssistActivity, MainActivity::class.java).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        startActivity(intent)
                                        finish()
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Open App")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
