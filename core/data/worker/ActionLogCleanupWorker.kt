package com.orbit.app.core.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.orbit.app.core.domain.repository.ActionLogRepository
import com.orbit.app.core.domain.repository.AgentSettingsRepository
import kotlinx.coroutines.flow.first

class ActionLogCleanupWorker(
    context: Context,
    params: WorkerParameters,
    private val actionLogRepository: ActionLogRepository,
    private val agentSettingsRepository: AgentSettingsRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val settings = agentSettingsRepository.agentSettings.first()
            val retentionDays = settings.retentionDays
            actionLogRepository.purgeOlderThan(retentionDays)
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
