package com.orbit.app.core.domain.model

data class LlmRequest(
    val messages: List<LlmMessage>,
    val tools: List<ToolSpec> = emptyList(),
    val promptProfile: String = "default",
    val maxOutputTokens: Int = 1024,
    val temperature: Float = 0.3f
)
