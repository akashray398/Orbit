package com.orbit.app.core.domain.model

data class AgentSettings(
    val maxAgentSteps: Int = 8,
    val confirmMediumRisk: Boolean = true,
    val historyWindowSize: Int = 20,
    val sendRedactedTextOnly: Boolean = true,
    val retentionDays: Int = 90
)
