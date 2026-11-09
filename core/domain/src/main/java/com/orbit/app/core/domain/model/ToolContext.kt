package com.orbit.app.core.domain.model

data class ToolContext(
    val conversationId: String = "default_conversation",
    val messageId: String,
    val currentTimeMillis: Long = System.currentTimeMillis(),
    val timeZoneId: String = "UTC",
    val locale: String = "en-IN"
)
