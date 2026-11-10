package com.orbit.app.core.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routine_runs")
data class RoutineRunEntity(
    @PrimaryKey val id: String,
    val routineId: String,
    val startedAt: Long = System.currentTimeMillis(),
    val finishedAt: Long? = null,
    val status: String = "Success",
    val resultText: String,
    val usedAi: Boolean = true,
    val tokenCount: Int? = null
)
