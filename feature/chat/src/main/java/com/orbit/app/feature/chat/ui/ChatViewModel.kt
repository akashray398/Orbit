package com.orbit.app.feature.chat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orbit.app.core.common.coroutine.DispatcherProvider
import com.orbit.app.core.domain.model.AgentEvent
import com.orbit.app.core.domain.model.AgentState
import com.orbit.app.core.domain.model.ChatMessage
import com.orbit.app.core.domain.model.ChatMessageRole
import com.orbit.app.core.domain.model.ChatMessageStatus
import com.orbit.app.core.domain.model.ContentPart
import com.orbit.app.core.domain.model.LlmEvent
import com.orbit.app.core.domain.model.LlmMessage
import com.orbit.app.core.domain.model.LlmRequest
import com.orbit.app.core.domain.model.SuggestionChipModel
import com.orbit.app.core.domain.model.ToolSpec
import com.orbit.app.core.domain.model.VoiceError
import com.orbit.app.core.domain.model.VoiceInputEvent
import com.orbit.app.core.domain.model.VoiceInputState
import com.orbit.app.core.domain.model.VoiceLocale
import com.orbit.app.core.domain.repository.AgentGateway
import com.orbit.app.core.domain.repository.ChatRepository
import com.orbit.app.core.domain.repository.LlmClient
import com.orbit.app.core.domain.repository.SpeechOutputController
import com.orbit.app.core.domain.repository.VoiceInputController
import com.orbit.app.core.domain.repository.VoiceSettingsRepository
import com.orbit.app.core.domain.usecase.PendingConfirmation
import com.orbit.app.core.domain.usecase.PendingConfirmationSource
import com.orbit.app.core.domain.usecase.UiConfirmationGate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class LlmTestDialogState(
    val showDialog: Boolean = false,
    val title: String = "",
    val streamedText: String = "",
    val toolCallDetails: String? = null,
    val usageText: String? = null,
    val isTesting: Boolean = false
)

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val agentState: AgentState = AgentState.Idle,
    val inputText: String = "",
    val suggestionChips: List<SuggestionChipModel> = listOf(
        SuggestionChipModel("1", "Plan my day"),
        SuggestionChipModel("2", "Set an alarm for 6 AM"),
        SuggestionChipModel("3", "What did I miss today?"),
        SuggestionChipModel("4", "Remind me to drink water"),
        SuggestionChipModel("5", "Open Calendar")
    ),
    val isOnline: Boolean = true,
    val isStreaming: Boolean = false,
    val voiceLocale: VoiceLocale = VoiceLocale.ENGLISH,
    val spokenRepliesEnabled: Boolean = false,
    val autoSendVoice: Boolean = true,
    val keepScreenOn: Boolean = true,
    val llmTestState: LlmTestDialogState = LlmTestDialogState(),
    val showTraceDialog: Boolean = false,
    val traceLogs: List<String> = emptyList(),
    val pendingConfirmation: PendingConfirmation? = null,
    val snackbarMessage: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val agentGateway: AgentGateway,
    val llmClient: LlmClient,
    val voiceInputController: VoiceInputController,
    val speechOutputController: SpeechOutputController,
    private val voiceSettingsRepository: VoiceSettingsRepository,
    val confirmationSource: PendingConfirmationSource,
    val confirmationGate: UiConfirmationGate,
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    val voiceInputState: StateFlow<VoiceInputState> = voiceInputController.state
    val isSpeaking: StateFlow<Boolean> = speechOutputController.isSpeaking

    private var streamingJob: Job? = null
    private val inMemoryTrace = mutableListOf<String>()

    init {
        observeMessages()
        observeVoiceSettings()
        observeVoiceEvents()
        observeVoiceInputState()
        observeConfirmations()
    }

    private fun observeConfirmations() {
        viewModelScope.launch(dispatchers.main) {
            confirmationSource.pendingConfirmation.collect { pending ->
                _uiState.update { it.copy(pendingConfirmation = pending) }
            }
        }
    }

    fun approveConfirmation() {
        confirmationGate.approveCurrent()
    }

    fun denyConfirmation() {
        confirmationGate.denyCurrent()
    }

    private fun observeMessages() {
        viewModelScope.launch(dispatchers.io) {
            chatRepository.messages.collect { messageList ->
                _uiState.update { it.copy(messages = messageList) }
            }
        }
    }

    private fun observeVoiceSettings() {
        viewModelScope.launch(dispatchers.io) {
            voiceSettingsRepository.voiceLocale.collect { locale ->
                _uiState.update { it.copy(voiceLocale = locale) }
            }
        }
        viewModelScope.launch(dispatchers.io) {
            voiceSettingsRepository.spokenRepliesEnabled.collect { enabled ->
                _uiState.update { it.copy(spokenRepliesEnabled = enabled) }
            }
        }
        viewModelScope.launch(dispatchers.io) {
            voiceSettingsRepository.autoSendVoice.collect { autoSend ->
                _uiState.update { it.copy(autoSendVoice = autoSend) }
            }
        }
        viewModelScope.launch(dispatchers.io) {
            voiceSettingsRepository.keepScreenOn.collect { keepScreen ->
                _uiState.update { it.copy(keepScreenOn = keepScreen) }
            }
        }
    }

    private fun observeVoiceInputState() {
        viewModelScope.launch(dispatchers.main) {
            voiceInputController.state.collect { state ->
                when (state) {
                    is VoiceInputState.Listening -> {
                        stopSpeaking()
                        _uiState.update { it.copy(agentState = AgentState.Listening) }
                    }
                    is VoiceInputState.Idle -> {
                        if (_uiState.value.agentState is AgentState.Listening) {
                            _uiState.update { it.copy(agentState = AgentState.Idle) }
                        }
                    }
                    is VoiceInputState.Error -> {
                        _uiState.update { it.copy(agentState = AgentState.Idle) }
                        val errorMsg = when (state.error) {
                            is VoiceError.NoSpeech -> "No speech detected. Please try speaking again."
                            is VoiceError.NoMatch -> "I didn't catch that. Please try again."
                            is VoiceError.Busy -> "Microphone is busy or in use by another application."
                            is VoiceError.Network -> "Network issue during speech recognition."
                            is VoiceError.PermissionDenied -> "Microphone permission required."
                            is VoiceError.LanguageUnavailable -> "Voice language is unavailable on this device."
                            else -> "Voice input error. Please try again."
                        }
                        showSnackbarNotice(errorMsg)
                    }
                    else -> {}
                }
            }
        }
    }

    private fun observeVoiceEvents() {
        viewModelScope.launch(dispatchers.main) {
            voiceInputController.events.collect { event ->
                when (event) {
                    is VoiceInputEvent.FinalResult -> {
                        val trimmed = event.text.trim()
                        if (trimmed.isNotBlank()) {
                            if (_uiState.value.autoSendVoice) {
                                sendMessage(trimmed)
                            } else {
                                onInputTextChange(trimmed)
                            }
                        } else {
                            showSnackbarNotice("I didn't catch that. Please try speaking again.")
                        }
                    }
                    is VoiceInputEvent.Cancelled -> {
                        _uiState.update { it.copy(agentState = AgentState.Idle) }
                    }
                }
            }
        }
    }

    fun showSnackbarNotice(msg: String) {
        _uiState.update { it.copy(snackbarMessage = msg) }
    }

    fun clearSnackbarNotice() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun runLlmTest(withTool: Boolean) {
        val title = if (withTool) "LLM Tool-Call Test" else "LLM Stream Test"
        _uiState.update {
            it.copy(
                llmTestState = LlmTestDialogState(
                    showDialog = true,
                    title = title,
                    isTesting = true
                )
            )
        }

        viewModelScope.launch(dispatchers.io) {
            val promptText = if (withTool) "What time is it right now?" else "Introduce yourself as Orbit AI agent."
            val tools = if (withTool) {
                listOf(
                    ToolSpec(
                        name = "get_current_time",
                        description = "Returns current system date and time",
                        parametersJsonSchema = "{\"type\":\"object\",\"properties\":{}}"
                    )
                )
            } else emptyList()

            val request = LlmRequest(
                messages = listOf(
                    LlmMessage(
                        role = "user",
                        content = listOf(ContentPart.Text(promptText))
                    )
                ),
                tools = tools
            )

            var textAcc = ""

            llmClient.stream(request).collect { event ->
                when (event) {
                    is LlmEvent.TextDelta -> {
                        textAcc += event.text
                        _uiState.update { state ->
                            state.copy(llmTestState = state.llmTestState.copy(streamedText = textAcc))
                        }
                    }
                    is LlmEvent.ToolCallRequested -> {
                        val details = "ID: ${event.id}\nName: ${event.name}\nArguments: ${event.argumentsJson}"
                        _uiState.update { state ->
                            state.copy(llmTestState = state.llmTestState.copy(toolCallDetails = details))
                        }
                    }
                    is LlmEvent.Usage -> {
                        val usage = "Tokens: ${event.inputTokens} in / ${event.outputTokens} out"
                        _uiState.update { state ->
                            state.copy(llmTestState = state.llmTestState.copy(usageText = usage))
                        }
                    }
                    is LlmEvent.Done -> {
                        _uiState.update { state ->
                            state.copy(llmTestState = state.llmTestState.copy(isTesting = false))
                        }
                    }
                    is LlmEvent.Failed -> {
                        val err = event.error.message ?: "Failed"
                        _uiState.update { state ->
                            state.copy(
                                llmTestState = state.llmTestState.copy(
                                    streamedText = "Error: $err",
                                    isTesting = false
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    fun openAgentTrace() {
        _uiState.update {
            it.copy(
                showTraceDialog = true,
                traceLogs = inMemoryTrace.toList()
            )
        }
    }

    fun dismissTraceDialog() {
        _uiState.update { it.copy(showTraceDialog = false) }
    }

    fun dismissLlmTestDialog() {
        _uiState.update { it.copy(llmTestState = LlmTestDialogState()) }
    }

    fun startVoiceInput() {
        stopSpeaking()
        voiceInputController.start(_uiState.value.voiceLocale)
    }

    fun stopVoiceInput() {
        voiceInputController.stop()
    }

    fun cancelVoiceInput() {
        voiceInputController.cancel()
    }

    fun setVoiceLocale(locale: VoiceLocale) {
        viewModelScope.launch(dispatchers.io) {
            voiceSettingsRepository.setVoiceLocale(locale)
        }
    }

    fun toggleSpokenReplies() {
        viewModelScope.launch(dispatchers.io) {
            voiceSettingsRepository.setSpokenRepliesEnabled(!_uiState.value.spokenRepliesEnabled)
        }
    }

    fun setSpokenRepliesEnabled(enabled: Boolean) {
        viewModelScope.launch(dispatchers.io) {
            voiceSettingsRepository.setSpokenRepliesEnabled(enabled)
        }
    }

    fun setAutoSendVoice(enabled: Boolean) {
        viewModelScope.launch(dispatchers.io) {
            voiceSettingsRepository.setAutoSendVoice(enabled)
        }
    }

    fun setKeepScreenOn(enabled: Boolean) {
        viewModelScope.launch(dispatchers.io) {
            voiceSettingsRepository.setKeepScreenOn(enabled)
        }
    }

    fun stopSpeaking() {
        speechOutputController.stop()
    }

    fun onInputTextChange(newText: String) {
        _uiState.update { it.copy(inputText = newText) }
    }

    fun sendMessage(text: String = _uiState.value.inputText) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || _uiState.value.isStreaming) return

        stopSpeaking()
        _uiState.update { it.copy(inputText = "") }

        viewModelScope.launch(dispatchers.io) {
            val userMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                role = ChatMessageRole.User,
                content = trimmed,
                status = ChatMessageStatus.Complete
            )
            chatRepository.addMessage(userMsg)

            startAgentStream(trimmed)
        }
    }

    private fun startAgentStream(userText: String) {
        streamingJob?.cancel()
        streamingJob = viewModelScope.launch(dispatchers.io) {
            val agentMsgId = UUID.randomUUID().toString()
            val initialAgentMsg = ChatMessage(
                id = agentMsgId,
                role = ChatMessageRole.Agent,
                content = "",
                status = ChatMessageStatus.Streaming
            )

            _uiState.update { it.copy(isStreaming = true) }
            chatRepository.addMessage(initialAgentMsg)
            inMemoryTrace.add("User: $userText")

            var accumulatedText = ""

            agentGateway.send(userText).collect { event ->
                when (event) {
                    is AgentEvent.StateChanged -> {
                        val state = event.state
                        _uiState.update { it.copy(agentState = state) }
                        if (state is AgentState.Acting) {
                            chatRepository.updateMessage(agentMsgId) { msg ->
                                msg.copy(actionToolLabel = state.toolLabel)
                            }
                        }
                    }
                    is AgentEvent.ToolStarted -> {
                        inMemoryTrace.add("ToolStarted: ${event.toolName} -> ${event.describeText}")
                    }
                    is AgentEvent.ToolFinished -> {
                        inMemoryTrace.add("ToolFinished: ${event.callId} status=${event.status} summary=${event.summary}")
                    }
                    is AgentEvent.TextDelta -> {
                        accumulatedText += event.delta
                        chatRepository.updateMessage(agentMsgId) { msg ->
                            msg.copy(content = accumulatedText)
                        }
                    }
                    is AgentEvent.Completed -> {
                        chatRepository.updateMessage(agentMsgId) { msg ->
                            msg.copy(status = ChatMessageStatus.Complete)
                        }
                        _uiState.update { it.copy(agentState = AgentState.Idle, isStreaming = false) }
                        inMemoryTrace.add("Agent Completed: ${accumulatedText.take(60)}...")

                        if (_uiState.value.spokenRepliesEnabled && accumulatedText.isNotBlank()) {
                            speechOutputController.speak(accumulatedText, _uiState.value.voiceLocale)
                        }
                    }
                    is AgentEvent.Failed -> {
                        val errorMsg = event.error.message ?: "Failed"
                        chatRepository.updateMessage(agentMsgId) { msg ->
                            msg.copy(
                                status = ChatMessageStatus.Failed,
                                content = if (accumulatedText.isNotEmpty()) accumulatedText else errorMsg
                            )
                        }
                        _uiState.update { it.copy(agentState = AgentState.Error(errorMsg), isStreaming = false) }
                        inMemoryTrace.add("Agent Failed: $errorMsg")
                    }
                    else -> {}
                }
            }
        }
    }

    fun cancelStreaming() {
        streamingJob?.cancel()
        _uiState.update { it.copy(agentState = AgentState.Idle, isStreaming = false) }
        viewModelScope.launch(dispatchers.io) {
            val lastMsg = _uiState.value.messages.lastOrNull { it.role == ChatMessageRole.Agent }
            if (lastMsg != null && lastMsg.status == ChatMessageStatus.Streaming) {
                chatRepository.updateMessage(lastMsg.id) { msg ->
                    msg.copy(
                        status = ChatMessageStatus.Complete,
                        content = if (msg.content.isNotEmpty()) "${msg.content} (Stopped)" else "Stopped"
                    )
                }
            }
        }
    }

    fun retryMessage(messageId: String) {
        val message = _uiState.value.messages.find { it.id == messageId } ?: return
        if (message.role == ChatMessageRole.User) {
            sendMessage(message.content)
        } else {
            val previousUserMsg = _uiState.value.messages.takeWhile { it.id != messageId }.lastOrNull { it.role == ChatMessageRole.User }
            previousUserMsg?.let { sendMessage(it.content) }
        }
    }

    fun clearConversation() {
        streamingJob?.cancel()
        stopSpeaking()
        inMemoryTrace.clear()
        _uiState.update { it.copy(agentState = AgentState.Idle, isStreaming = false) }
        viewModelScope.launch(dispatchers.io) {
            chatRepository.clearConversation()
        }
    }

    fun addSampleMessages(count: Int = 300) {
        viewModelScope.launch(dispatchers.io) {
            chatRepository.addSampleMessages(count)
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceInputController.release()
        speechOutputController.release()
    }
}
