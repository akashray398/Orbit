package com.orbit.app.core.domain.model

sealed interface ContentPart {
    data class Text(val text: String) : ContentPart
    data class ToolCall(val id: String, val name: String, val argumentsJson: String) : ContentPart
    data class ToolResult(val toolCallId: String, val content: String, val isError: Boolean = false) : ContentPart
}
