package com.orbit.app.core.domain.model

sealed interface VoiceInputState {
    data object Idle : VoiceInputState
    data object Preparing : VoiceInputState
    data class Listening(
        val amplitude: Float = 0f,
        val partialText: String = ""
    ) : VoiceInputState
    data object Processing : VoiceInputState
    data class Error(val error: VoiceError) : VoiceInputState
}
