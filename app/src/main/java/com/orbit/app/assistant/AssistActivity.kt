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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.orbit.app.MainActivity
import com.orbit.app.core.designsystem.components.GlassCard
import com.orbit.app.core.designsystem.components.OrbitTextField
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
import kotlinx.coroutines.launch
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
                var inputText by remember { mutableStateOf("") }

                val partialText = (voiceState as? VoiceInputState.Listening)?.partialText ?: ""

                DisposableEffect(Unit) {
                    voiceInputController.start(VoiceLocale.ENGLISH)
                    onDispose {
                        voiceInputController.stop()
                        speechOutputController.stop()
                    }
                }

                fun processPrompt(prompt: String) {
                    val trimmed = prompt.trim()
                    if (trimmed.isBlank()) return

                    userTranscript = trimmed
                    responseText = ""
                    toolLabel = null
                    inputText = ""

                    lifecycleScope.launch {
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

                LaunchedEffect(Unit) {
                    voiceInputController.events.collect { event ->
                        when (event) {
                            is VoiceInputEvent.FinalResult -> {
                                processPrompt(event.text)
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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Floating Gemini Style Action Card (Appears when prompt is processed)
                        val activeText = if (responseText.isNotEmpty()) responseText else partialText
                        if (activeText.isNotEmpty() || toolLabel != null || userTranscript.isNotEmpty()) {
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                                    .clickable(enabled = false) {},
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Alarm,
                                            contentDescription = null,
                                            tint = OrbitTheme.colors.glowSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = toolLabel ?: "Orbit Agent",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = OrbitTheme.colors.success,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Big Bold Time or Response Display
                                    Text(
                                        text = when {
                                            responseText.isNotEmpty() -> responseText
                                            partialText.isNotEmpty() -> partialText
                                            else -> "Processing..."
                                        },
                                        style = MaterialTheme.typography.headlineLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 28.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        // Floating Input Bar Pill
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = false) {},
                            shape = RoundedCornerShape(32.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = {
                                    val intent = Intent(this@AssistActivity, MainActivity::class.java).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    startActivity(intent)
                                    finish()
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Open App",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                OrbitTextField(
                                    value = inputText,
                                    onValueChange = { inputText = it },
                                    placeholder = if (isLocked) "Unlock device to ask Orbit..." else "Ask Orbit...",
                                    modifier = Modifier.weight(1f)
                                )

                                if (inputText.isNotBlank()) {
                                    IconButton(onClick = { processPrompt(inputText) }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Send,
                                            contentDescription = "Send",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                } else {
                                    IconButton(onClick = {
                                        voiceInputController.start(VoiceLocale.ENGLISH)
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Mic,
                                            contentDescription = "Mic",
                                            tint = if (voiceState is VoiceInputState.Listening) OrbitTheme.colors.glowSecondary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
