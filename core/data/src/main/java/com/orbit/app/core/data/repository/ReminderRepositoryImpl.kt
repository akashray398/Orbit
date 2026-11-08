package com.orbit.app.core.data.repository

import com.orbit.app.core.common.result.safeCall
import com.orbit.app.core.data.database.daos.ReminderDao
import com.orbit.app.core.data.mapper.toDomain
import com.orbit.app.core.data.mapper.toEntity
import com.orbit.app.core.domain.model.Reminder
import com.orbit.app.core.domain.model.ReminderStatus
import com.orbit.app.core.domain.repository.ReminderRepository
import com.orbit.app.core.domain.result.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepositoryImpl @Inject constructor(
    private val reminderDao: ReminderDao
) : ReminderRepository {

    override val upcomingReminders: Flow<List<Reminder>> = reminderDao.observeUpcomingReminders().map { list -> list.map { it.toDomain() } }

    override suspend fun createReminder(title: String, triggerAt: Long, repeatRule: String?): Result<Reminder> = safeCall {
        val reminder = Reminder(
            id = UUID.randomUUID().toString(),
            title = title,
            triggerAt = triggerAt,
            repeatRule = repeatRule
        )
        reminderDao.insertReminder(reminder.toEntity())
        reminder
    }

    override suspend fun markFired(id: String): Result<Unit> = safeCall {
        reminderDao.updateStatus(id, ReminderStatus.Fired)
    }

    override suspend fun cancelReminder(id: String): Result<Unit> = safeCall {
        reminderDao.updateStatus(id, ReminderStatus.Cancelled)
    }

    override suspend fun findById(id: String): Result<Reminder?> = safeCall {
        reminderDao.getReminderById(id)?.toDomain()
    }
}
