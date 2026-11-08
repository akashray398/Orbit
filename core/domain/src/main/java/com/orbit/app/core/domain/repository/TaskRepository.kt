package com.orbit.app.core.domain.repository

import com.orbit.app.core.domain.model.Reminder
import com.orbit.app.core.domain.model.Task
import com.orbit.app.core.domain.model.TaskPriority
import com.orbit.app.core.domain.result.Result
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    val openTasks: Flow<List<Task>>
    val completedTasks: Flow<List<Task>>
    val dueTodayTasks: Flow<List<Task>>
    suspend fun createTask(title: String, notes: String?, dueAt: Long?, priority: TaskPriority): Result<Task>
    suspend fun updateTask(task: Task): Result<Unit>
    suspend fun completeTask(id: String): Result<Unit>
    suspend fun reopenTask(id: String): Result<Unit>
    suspend fun deleteTask(id: String): Result<Unit>
    suspend fun createWithReminder(taskTitle: String, notes: String?, triggerAt: Long): Result<Pair<Task, Reminder>>
}
