package com.orbit.app.core.data.database.daos

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.orbit.app.core.data.database.entities.MessageEntity
import com.orbit.app.core.domain.model.ChatMessageStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun observeMessages(conversationId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp DESC")
    fun pagedMessages(conversationId: String): PagingSource<Int, MessageEntity>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessages(conversationId: String, limit: Int): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Query("UPDATE messages SET content = :content, status = :status, actionToolLabel = :actionToolLabel WHERE id = :id")
    suspend fun updateStreamingContent(id: String, content: String, status: ChatMessageStatus, actionToolLabel: String?)

    @Query("UPDATE messages SET status = :status, errorMessage = :errorMessage WHERE id = :id")
    suspend fun markFailed(id: String, status: ChatMessageStatus, errorMessage: String?)

    @Query("UPDATE messages SET status = 'Interrupted' WHERE status IN ('Streaming', 'Sending')")
    suspend fun recoverInterruptedStreams(): Int

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessage(id: String)

    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun deleteConversationMessages(conversationId: String)

    @Query("DELETE FROM messages")
    suspend fun clearAllMessages()
}
