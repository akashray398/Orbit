package com.orbit.app.core.domain.model

sealed interface AgentState {
    data object Idle : AgentState
    data object Listening : AgentState
    data object Thinking : AgentState
    data class Acting(val toolLabel: String) : AgentState
    data class Error(val message: String = "") : AgentState
}
