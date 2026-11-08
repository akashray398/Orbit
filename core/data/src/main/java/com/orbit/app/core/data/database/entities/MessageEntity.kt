package com.orbit.app.core.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.orbit.app.core.domain.model.ChatMessageRole
import com.orbit.app.core.domain.model.ChatMessageStatus

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["conversationId", "timestamp"])
    ]
)
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String = "default_conversation",
    val role: ChatMessageRole,
    val content: String,
    val status: ChatMessageStatus,
    val timestamp: Long = System.currentTimeMillis(),
    val inputSource: String = "Typed",
    val errorMessage: String? = null,
    val toolCallsJson: String? = null,
    val actionToolLabel: String? = null
)
