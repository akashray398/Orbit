package com.orbit.app.core.domain.repository

import com.orbit.app.core.domain.result.Result

interface DataMaintenanceRepository {
    suspend fun clearChatHistory(): Result<Unit>
    suspend fun deleteAllNotes(): Result<Unit>
    suspend fun deleteAllTasksAndReminders(): Result<Unit>
    suspend fun purgeActionLogsOlderThan(days: Int): Result<Int>
    suspend fun wipeAllData(): Result<Unit>
    suspend fun exportAllDataAsJson(): Result<String>
}
