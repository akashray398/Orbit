package com.orbit.app.core.domain.model

enum class ReminderStatus {
    Scheduled,
    Fired,
    Cancelled
}

data class Reminder(
    val id: String,
    val title: String,
    val triggerAt: Long,
    val repeatRule: String? = null,
    val workRequestId: String? = null,
    val status: ReminderStatus = ReminderStatus.Scheduled,
    val createdAt: Long = System.currentTimeMillis(),
    val source: TaskSource = TaskSource.User
)
