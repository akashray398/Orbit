package com.orbit.app.core.data.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.orbit.app.core.domain.model.ActionStatus
import com.orbit.app.core.domain.model.RiskLevel

@Entity(
    tableName = "action_logs",
    indices = [
        Index(value = ["startedAt"])
    ]
)
data class ActionLogEntity(
    @PrimaryKey val id: String,
    val toolName: String,
    val argumentsJson: String,
    val resultSummary: String,
    val status: ActionStatus = ActionStatus.Pending,
    val riskLevel: RiskLevel = RiskLevel.Low,
    val startedAt: Long = System.currentTimeMillis(),
    val finishedAt: Long? = null,
    val conversationId: String? = null,
    val messageId: String? = null,
    val undoPayloadJson: String? = null,
    val undoneAt: Long? = null
)
