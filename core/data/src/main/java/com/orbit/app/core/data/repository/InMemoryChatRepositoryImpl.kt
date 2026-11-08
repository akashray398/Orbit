package com.orbit.app.core.data.repository

import com.orbit.app.core.domain.model.ChatMessage
import com.orbit.app.core.domain.model.ChatMessageRole
import com.orbit.app.core.domain.model.ChatMessageStatus
import com.orbit.app.core.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryChatRepositoryImpl @Inject constructor() : ChatRepository {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    override val messages: Flow<List<ChatMessage>> = _messages.asStateFlow()

    override suspend fun addMessage(message: ChatMessage) {
        _messages.update { current -> current + message }
    }

    override suspend fun updateMessage(id: String, transform: (ChatMessage) -> ChatMessage) {
        _messages.update { current ->
            current.map { msg ->
                if (msg.id == id) transform(msg) else msg
            }
        }
    }

    override suspend fun clearConversation() {
        _messages.value = emptyList()
    }

    override suspend fun addSampleMessages(count: Int) {
        val sampleList = mutableListOf<ChatMessage>()
        val now = System.currentTimeMillis()
        for (i in 1..count) {
            val role = if (i % 2 == 0) ChatMessageRole.Agent else ChatMessageRole.User
            val content = if (role == ChatMessageRole.User) {
                "Sample user request #$i"
            } else {
                "Sample agent response #$i with action details."
            }
            sampleList.add(
                ChatMessage(
                    id = "sample_$i",
                    role = role,
                    content = content,
                    timestamp = now - (count - i) * 60000L,
                    status = ChatMessageStatus.Complete
                )
            )
        }
        _messages.value = sampleList
    }
}
