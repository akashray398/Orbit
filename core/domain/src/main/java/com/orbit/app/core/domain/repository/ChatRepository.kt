package com.orbit.app.core.domain.repository

import com.orbit.app.core.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    val messages: Flow<List<ChatMessage>>
    suspend fun addMessage(message: ChatMessage)
    suspend fun updateMessage(id: String, transform: (ChatMessage) -> ChatMessage)
    suspend fun clearConversation()
    suspend fun addSampleMessages(count: Int = 300)
}
