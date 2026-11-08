package com.orbit.app.core.domain.repository

import com.orbit.app.core.domain.model.ActionLog
import com.orbit.app.core.domain.model.ActionStatus
import com.orbit.app.core.domain.model.RiskLevel
import com.orbit.app.core.domain.result.Result
import kotlinx.coroutines.flow.Flow

interface ActionLogRepository {
    val timeline: Flow<List<ActionLog>>
    fun observeById(id: String): Flow<ActionLog?>
    suspend fun startAction(toolName: String, argumentsJson: String, riskLevel: RiskLevel): Result<ActionLog>
    suspend fun finishAction(id: String, status: ActionStatus, summary: String, undoPayloadJson: String?): Result<Unit>
    suspend fun markUndone(id: String): Result<Unit>
    suspend fun getPendingCount(): Result<Int>
    suspend fun purgeOlderThan(days: Int): Result<Int>
}
