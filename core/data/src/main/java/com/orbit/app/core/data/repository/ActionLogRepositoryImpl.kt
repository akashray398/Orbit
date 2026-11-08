package com.orbit.app.core.data.repository

import com.orbit.app.core.common.result.safeCall
import com.orbit.app.core.data.database.daos.ActionLogDao
import com.orbit.app.core.data.mapper.toDomain
import com.orbit.app.core.data.mapper.toEntity
import com.orbit.app.core.domain.model.ActionLog
import com.orbit.app.core.domain.model.ActionStatus
import com.orbit.app.core.domain.model.RiskLevel
import com.orbit.app.core.domain.repository.ActionLogRepository
import com.orbit.app.core.domain.result.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActionLogRepositoryImpl @Inject constructor(
    private val actionLogDao: ActionLogDao
) : ActionLogRepository {

    override val timeline: Flow<List<ActionLog>> = actionLogDao.observeTimeline().map { list -> list.map { it.toDomain() } }

    override fun observeById(id: String): Flow<ActionLog?> = actionLogDao.observeById(id).map { it?.toDomain() }

    override suspend fun startAction(toolName: String, argumentsJson: String, riskLevel: RiskLevel): Result<ActionLog> = safeCall {
        val actionLog = ActionLog(
            id = UUID.randomUUID().toString(),
            toolName = toolName,
            argumentsJson = argumentsJson,
            resultSummary = "Pending...",
            status = ActionStatus.Pending,
            riskLevel = riskLevel
        )
        actionLogDao.insertActionLog(actionLog.toEntity())
        actionLog
    }

    override suspend fun finishAction(id: String, status: ActionStatus, summary: String, undoPayloadJson: String?): Result<Unit> = safeCall {
        actionLogDao.finishAction(id, status, summary, System.currentTimeMillis(), undoPayloadJson)
    }

    override suspend fun markUndone(id: String): Result<Unit> = safeCall {
        actionLogDao.markUndone(id)
    }

    override suspend fun getPendingCount(): Result<Int> = safeCall {
        actionLogDao.getPendingCount()
    }

    override suspend fun purgeOlderThan(days: Int): Result<Int> = safeCall {
        val cutoff = System.currentTimeMillis() - (days * 86400000L)
        actionLogDao.purgeOlderThan(cutoff)
    }
}
