package com.orbit.app.core.data.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.orbit.app.core.domain.model.TaskPriority
import com.orbit.app.core.domain.model.TaskSource
import com.orbit.app.core.domain.model.TaskStatus

@Entity(
    tableName = "tasks",
    indices = [
        Index(value = ["status", "dueAt"])
    ]
)
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val notes: String? = null,
    val dueAt: Long? = null,
    val priority: TaskPriority = TaskPriority.Medium,
    val status: TaskStatus = TaskStatus.Open,
    val completedAt: Long? = null,
    val reminderId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val source: TaskSource = TaskSource.User
)
