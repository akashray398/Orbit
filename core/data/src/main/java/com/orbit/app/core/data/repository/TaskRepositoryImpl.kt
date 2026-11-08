package com.orbit.app.core.data.repository

import com.orbit.app.core.common.result.safeCall
import com.orbit.app.core.data.database.daos.ReminderDao
import com.orbit.app.core.data.database.daos.TaskDao
import com.orbit.app.core.data.mapper.toDomain
import com.orbit.app.core.data.mapper.toEntity
import com.orbit.app.core.domain.model.Reminder
import com.orbit.app.core.domain.model.Task
import com.orbit.app.core.domain.model.TaskPriority
import com.orbit.app.core.domain.repository.TaskRepository
import com.orbit.app.core.domain.result.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val reminderDao: ReminderDao
) : TaskRepository {

    override val openTasks: Flow<List<Task>> = taskDao.observeOpenTasks().map { list -> list.map { it.toDomain() } }
    override val completedTasks: Flow<List<Task>> = taskDao.observeCompletedTasks().map { list -> list.map { it.toDomain() } }
    override val dueTodayTasks: Flow<List<Task>> = taskDao.observeDueToday(System.currentTimeMillis() + 86400000L).map { list -> list.map { it.toDomain() } }

    override suspend fun createTask(title: String, notes: String?, dueAt: Long?, priority: TaskPriority): Result<Task> = safeCall {
        val task = Task(
            id = UUID.randomUUID().toString(),
            title = title,
            notes = notes,
            dueAt = dueAt,
            priority = priority
        )
        taskDao.insertTask(task.toEntity())
        task
    }

    override suspend fun updateTask(task: Task): Result<Unit> = safeCall {
        taskDao.updateTask(task.toEntity())
    }

    override suspend fun completeTask(id: String): Result<Unit> = safeCall {
        taskDao.completeTask(id)
    }

    override suspend fun reopenTask(id: String): Result<Unit> = safeCall {
        taskDao.reopenTask(id)
    }

    override suspend fun deleteTask(id: String): Result<Unit> = safeCall {
        taskDao.deleteTask(id)
    }

    override suspend fun createWithReminder(taskTitle: String, notes: String?, triggerAt: Long): Result<Pair<Task, Reminder>> = safeCall {
        val reminderId = UUID.randomUUID().toString()
        val taskId = UUID.randomUUID().toString()

        val reminder = Reminder(
            id = reminderId,
            title = taskTitle,
            triggerAt = triggerAt
        )
        val task = Task(
            id = taskId,
            title = taskTitle,
            notes = notes,
            dueAt = triggerAt,
            reminderId = reminderId
        )

        taskDao.insertTaskWithReminder(task.toEntity(), reminder.toEntity(), reminderDao)
        Pair(task, reminder)
    }
}
