package com.orbit.app.core.domain.model

sealed interface AgentEvent {
    data class StateChanged(val state: AgentState) : AgentEvent
    data class TextDelta(val delta: String) : AgentEvent
    data class ToolStarted(
        val callId: String,
        val toolName: String,
        val label: String,
        val describeText: String
    ) : AgentEvent
    data class ToolFinished(
        val callId: String,
        val status: ActionStatus,
        val summary: String
    ) : AgentEvent
    data object StepLimitReached : AgentEvent
    data object Completed : AgentEvent
    data class Failed(val error: AppError) : AgentEvent
}
