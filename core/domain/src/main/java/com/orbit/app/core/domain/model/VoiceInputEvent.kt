package com.orbit.app.core.domain.model

sealed interface VoiceInputEvent {
    data class FinalResult(
        val text: String,
        val confidence: Float = 1.0f
    ) : VoiceInputEvent
    data object Cancelled : VoiceInputEvent
}
