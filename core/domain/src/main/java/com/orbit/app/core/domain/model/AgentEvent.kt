package com.orbit.app.core.domain.model

sealed interface AgentEvent {
    data class StateChanged(val state: AgentState) : AgentEvent
    data class TextDelta(val delta: String) : AgentEvent
    data object Completed : AgentEvent
    data class Failed(val error: AppError) : AgentEvent
}
