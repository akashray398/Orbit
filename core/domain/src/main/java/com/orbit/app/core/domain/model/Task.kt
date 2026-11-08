package com.orbit.app.core.domain.model

enum class TaskPriority {
    Low,
    Medium,
    High
}

enum class TaskStatus {
    Open,
    Done
}

enum class TaskSource {
    User,
    Agent
}

data class Task(
    val id: String,
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
