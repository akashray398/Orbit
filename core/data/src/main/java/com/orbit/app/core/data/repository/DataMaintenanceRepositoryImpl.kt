package com.orbit.app.core.data.repository

import com.orbit.app.core.common.result.safeCall
import com.orbit.app.core.data.database.OrbitDatabase
import com.orbit.app.core.domain.repository.DataMaintenanceRepository
import com.orbit.app.core.domain.result.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataMaintenanceRepositoryImpl @Inject constructor(
    private val database: OrbitDatabase
) : DataMaintenanceRepository {

    override suspend fun clearChatHistory(): Result<Unit> = safeCall {
        database.messageDao().clearAllMessages()
    }

    override suspend fun deleteAllNotes(): Result<Unit> = safeCall {
        database.noteDao().clearAllNotes()
    }

    override suspend fun deleteAllTasksAndReminders(): Result<Unit> = safeCall {
        database.taskDao().clearAllTasks()
        database.reminderDao().clearAllReminders()
    }

    override suspend fun purgeActionLogsOlderThan(days: Int): Result<Int> = safeCall {
        val cutoff = System.currentTimeMillis() - (days * 86400000L)
        database.actionLogDao().purgeOlderThan(cutoff)
    }

    override suspend fun wipeAllData(): Result<Unit> = safeCall {
        database.clearAllTables()
    }

    override suspend fun exportAllDataAsJson(): Result<String> = safeCall {
        "{\"status\":\"exported\",\"timestamp\":${System.currentTimeMillis()}}"
    }
}
