package com.orbit.app.core.domain.model

data class Conversation(
    val id: String = "default_conversation",
    val title: String = "Main Chat",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val archived: Boolean = false
)
