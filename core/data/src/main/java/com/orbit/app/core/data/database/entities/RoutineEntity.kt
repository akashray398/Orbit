package com.orbit.app.core.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String = "MORNING_BRIEF",
    val timeOfDay: String = "08:00",
    val daysOfWeek: Int = 127, // All days bitmask
    val enabled: Boolean = false,
    val customPrompt: String? = null,
    val lastRunAt: Long? = null,
    val lastStatus: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
