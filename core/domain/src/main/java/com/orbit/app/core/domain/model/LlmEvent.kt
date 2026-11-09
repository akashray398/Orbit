package com.orbit.app.core.domain.model

sealed interface LlmEvent {
    data class TextDelta(val text: String) : LlmEvent
    data class ToolCallRequested(val id: String, val name: String, val argumentsJson: String) : LlmEvent
    data class Usage(val inputTokens: Int, val outputTokens: Int) : LlmEvent
    data class Done(val stopReason: String) : LlmEvent
    data class Failed(val error: AppError) : LlmEvent
}
