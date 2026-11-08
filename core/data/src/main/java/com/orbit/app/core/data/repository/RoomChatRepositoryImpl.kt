package com.orbit.app.core.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.orbit.app.core.data.database.daos.ConversationDao
import com.orbit.app.core.data.database.daos.MessageDao
import com.orbit.app.core.data.database.entities.ConversationEntity
import com.orbit.app.core.data.mapper.toDomain
import com.orbit.app.core.data.mapper.toEntity
import com.orbit.app.core.domain.model.ChatMessage
import com.orbit.app.core.domain.model.ChatMessageStatus
import com.orbit.app.core.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomChatRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao
) : ChatRepository {

    private var lastStreamWriteTime = 0L

    override val messages: Flow<List<ChatMessage>> = messageDao.observeMessages("default_conversation").map { list ->
        list.map { it.toDomain() }
    }

    suspend fun getOrCreateDefaultConversation(): String {
        val existing = conversationDao.getConversationById("default_conversation")
        if (existing == null) {
            conversationDao.insertConversation(ConversationEntity(id = "default_conversation", title = "Main Chat"))
        }
        return "default_conversation"
    }

    fun pagedMessages(conversationId: String = "default_conversation"): Flow<PagingData<ChatMessage>> {
        return Pager(
            config = PagingConfig(pageSize = 30, enablePlaceholders = false)
        ) {
            messageDao.pagedMessages(conversationId)
        }.flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }

    override suspend fun addMessage(message: ChatMessage) {
        getOrCreateDefaultConversation()
        messageDao.insertMessage(message.toEntity("default_conversation"))
    }

    override suspend fun updateMessage(id: String, transform: (ChatMessage) -> ChatMessage) {
        val now = System.currentTimeMillis()
        val isThrottled = (now - lastStreamWriteTime) < 400L

        val existingList = messageDao.getRecentMessages("default_conversation", 50)
        val target = existingList.find { it.id == id } ?: return
        val updatedDomain = transform(target.toDomain())

        val isFinal = updatedDomain.status == ChatMessageStatus.Complete || updatedDomain.status == ChatMessageStatus.Failed

        if (!isThrottled || isFinal) {
            lastStreamWriteTime = now
            messageDao.updateStreamingContent(
                id = id,
                content = updatedDomain.content,
                status = updatedDomain.status,
                actionToolLabel = updatedDomain.actionToolLabel
            )
        }
    }

    suspend fun recoverInterruptedStreams(): Int {
        return messageDao.recoverInterruptedStreams()
    }

    override suspend fun clearConversation() {
        messageDao.clearAllMessages()
    }

    override suspend fun addSampleMessages(count: Int) {
        getOrCreateDefaultConversation()
        val sampleList = mutableListOf<ChatMessage>()
        val now = System.currentTimeMillis()
        for (i in 1..count) {
            val role = if (i % 2 == 0) com.orbit.app.core.domain.model.ChatMessageRole.Agent else com.orbit.app.core.domain.model.ChatMessageRole.User
            val content = if (role == com.orbit.app.core.domain.model.ChatMessageRole.User) {
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
        messageDao.insertMessages(sampleList.map { it.toEntity("default_conversation") })
    }
}
