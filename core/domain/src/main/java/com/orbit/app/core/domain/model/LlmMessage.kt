package com.orbit.app.core.domain.model

data class LlmMessage(
    val role: String,
    val content: List<ContentPart>
)
