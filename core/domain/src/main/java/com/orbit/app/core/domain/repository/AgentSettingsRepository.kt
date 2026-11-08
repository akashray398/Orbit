package com.orbit.app.core.domain.repository

import com.orbit.app.core.domain.model.AgentSettings
import kotlinx.coroutines.flow.Flow

interface AgentSettingsRepository {
    val agentSettings: Flow<AgentSettings>
    suspend fun setMaxAgentSteps(steps: Int)
    suspend fun setConfirmMediumRisk(confirm: Boolean)
    suspend fun setHistoryWindowSize(size: Int)
    suspend fun setSendRedactedTextOnly(redact: Boolean)
    suspend fun setRetentionDays(days: Int)
}
