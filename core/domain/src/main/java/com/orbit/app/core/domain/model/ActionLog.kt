package com.orbit.app.core.domain.model

enum class ActionStatus {
    Pending,
    Success,
    Failed,
    Denied,
    Undone
}

enum class RiskLevel {
    Low,
    Medium,
    High
}

data class ActionLog(
    val id: String,
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
