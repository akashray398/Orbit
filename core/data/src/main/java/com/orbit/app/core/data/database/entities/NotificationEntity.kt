package com.orbit.app.core.data.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "captured_notifications",
    indices = [
        Index(value = ["postedAt"])
    ]
)
data class NotificationEntity(
    @PrimaryKey val id: String,
    val packageName: String,
    val appLabel: String,
    val title: String,
    val text: String,
    val postedAt: Long = System.currentTimeMillis(),
    val category: String = "general"
)
