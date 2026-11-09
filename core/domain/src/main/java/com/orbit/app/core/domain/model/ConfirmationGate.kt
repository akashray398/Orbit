package com.orbit.app.core.domain.model

data class PendingAction(
    val toolName: String,
    val riskLevel: RiskLevel,
    val describeActionText: String,
    val argumentsJson: String
)

enum class ConfirmationDecision {
    Approved,
    Denied,
    TimedOut
}

interface ConfirmationGate {
    suspend fun request(action: PendingAction): ConfirmationDecision
}
