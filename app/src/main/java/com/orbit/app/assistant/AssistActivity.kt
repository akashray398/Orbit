package com.orbit.app.assistant

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.orbit.app.MainActivity
import com.orbit.app.core.designsystem.components.AgentOrb
import com.orbit.app.core.designsystem.components.GlassCard
import com.orbit.app.core.designsystem.components.OrbitChip
import com.orbit.app.core.designsystem.components.WaveformView
import com.orbit.app.core.designsystem.theme.OrbitTheme
import com.orbit.app.core.domain.model.AgentEvent
import com.orbit.app.core.domain.model.AgentState
import com.orbit.app.core.domain.model.VoiceInputEvent
import com.orbit.app.core.domain.model.VoiceInputState
import com.orbit.app.core.domain.model.VoiceLocale
import com.orbit.app.core.domain.repository.AgentGateway
import com.orbit.app.core.domain.repository.SpeechOutputController
import com.orbit.app.core.domain.repository.VoiceInputController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AssistActivity : ComponentActivity() {

    @Inject
    lateinit var agentGateway: AgentGateway

    @Inject
    lateinit var voiceInputController: VoiceInputController

    @Inject
    lateinit var speechOutputController: SpeechOutputController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val isLocked = keyguardManager.isKeyguardLocked

        setContent {
            OrbitTheme {
                val voiceState by voiceInputController.state.collectAsStateWithLifecycle()
                var agentState by remember { mutableStateOf<AgentState>(AgentState.Idle) }
                var responseText by remember { mutableStateOf("") }
                var toolLabel by remember { mutableStateOf<String?>(null) }
                var userTranscript by remember { mutableStateOf("") }

                val amplitude = (voiceState as? VoiceInputState.Listening)?.amplitude ?: 0f
                val partialText = (voiceState as? VoiceInputState.Listening)?.partialText ?: ""

                DisposableEffect(Unit) {
                    voiceInputController.start(VoiceLocale.ENGLISH)
                    onDispose {
                        voiceInputController.stop()
                        speechOutputController.stop()
                    }
                }

                LaunchedEffect(Unit) {
                    voiceInputController.events.collect { event ->
                        when (event) {
                            is VoiceInputEvent.FinalResult -> {
                                val trimmed = event.text.trim()
                                if (trimmed.isNotBlank()) {
                                    userTranscript = trimmed
                                    responseText = ""
                                    toolLabel = null

                                    agentGateway.send(trimmed).collect { agentEvt ->
                                        when (agentEvt) {
                                            is AgentEvent.StateChanged -> {
                                                agentState = agentEvt.state
                                                if (agentEvt.state is AgentState.Acting) {
                                                    toolLabel = (agentEvt.state as AgentState.Acting).toolLabel
                                                }
                                            }
                                            is AgentEvent.TextDelta -> {
                                                responseText += agentEvt.delta
                                            }
                                            is AgentEvent.Completed -> {
                                                agentState = AgentState.Idle
                                                if (responseText.isNotBlank()) {
                                                    speechOutputController.speak(responseText, VoiceLocale.ENGLISH)
                                                }
                                            }
                                            is AgentEvent.Failed -> {
                                                agentState = AgentState.Idle
                                                responseText = agentEvt.error.message ?: "Failed to process request."
                                            }
                                            else -> {}
                                        }
                                    }
                                }
                            }
                            else -> {}
                        }
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
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AgentOrb(
                                    state = if (voiceState is VoiceInputState.Listening) AgentState.Listening else agentState,
                                    amplitude = amplitude,
                                    size = 48.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Orbit Assistant", style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = when {
                                            isLocked -> "Lockscreen Mode (Limited)"
                                            voiceState is VoiceInputState.Listening -> "Listening..."
                                            agentState is AgentState.Thinking -> "Thinking..."
                                            agentState is AgentState.Acting -> toolLabel ?: "Acting..."
                                            else -> "Ready"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Live Waveform when listening
                            if (voiceState is VoiceInputState.Listening) {
                                WaveformView(
                                    amplitude = amplitude,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // Tool Action Chip
                            val label = toolLabel
                            if (label != null) {
                                OrbitChip(
                                    label = label,
                                    selected = true,
                                    onClick = {},
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Build,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // Transcript / Response Text
                            val displayText = when {
                                responseText.isNotEmpty() -> responseText
                                partialText.isNotEmpty() -> partialText
                                userTranscript.isNotEmpty() -> "You: $userTranscript"
                                isLocked -> "Device is locked. Unlock for private commands."
                                else -> "Listening for your command..."
                            }

                            Text(
                                text = displayText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
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
                                    Text("Open Full App")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
