package com.orbit.app.core.domain.model

enum class ChatMessageRole {
    User,
    Agent,
    System
}

enum class ChatMessageStatus {
    Sending,
    Streaming,
    Complete,
    Failed,
    Interrupted
}

data class ChatMessage(
    val id: String,
    val role: ChatMessageRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: ChatMessageStatus = ChatMessageStatus.Complete,
    val actionToolLabel: String? = null
)
