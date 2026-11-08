package com.orbit.app.core.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.orbit.app.core.domain.model.ReminderStatus
import com.orbit.app.core.domain.model.TaskSource

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey val id: String,
    val title: String,
    val triggerAt: Long,
    val repeatRule: String? = null,
    val workRequestId: String? = null,
    val status: ReminderStatus = ReminderStatus.Scheduled,
    val createdAt: Long = System.currentTimeMillis(),
    val source: TaskSource = TaskSource.User
)
