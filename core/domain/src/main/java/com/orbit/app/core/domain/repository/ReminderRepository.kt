package com.orbit.app.core.domain.repository

import com.orbit.app.core.domain.model.Reminder
import com.orbit.app.core.domain.result.Result
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    val upcomingReminders: Flow<List<Reminder>>
    suspend fun createReminder(title: String, triggerAt: Long, repeatRule: String?): Result<Reminder>
    suspend fun markFired(id: String): Result<Unit>
    suspend fun cancelReminder(id: String): Result<Unit>
    suspend fun findById(id: String): Result<Reminder?>
}
