package com.orbit.app.core.data.database.converters

import androidx.room.TypeConverter
import com.orbit.app.core.domain.model.ActionStatus
import com.orbit.app.core.domain.model.ChatMessageRole
import com.orbit.app.core.domain.model.ChatMessageStatus
import com.orbit.app.core.domain.model.NoteSource
import com.orbit.app.core.domain.model.RiskLevel
import com.orbit.app.core.domain.model.TaskPriority
import com.orbit.app.core.domain.model.TaskSource
import com.orbit.app.core.domain.model.TaskStatus

class OrbitTypeConverters {

    @TypeConverter
    fun fromChatMessageRole(role: ChatMessageRole): String = role.name

    @TypeConverter
    fun toChatMessageRole(value: String): ChatMessageRole = try {
        ChatMessageRole.valueOf(value)
    } catch (_: Exception) {
        ChatMessageRole.User
    }

    @TypeConverter
    fun fromChatMessageStatus(status: ChatMessageStatus): String = status.name

    @TypeConverter
    fun toChatMessageStatus(value: String): ChatMessageStatus = try {
        ChatMessageStatus.valueOf(value)
    } catch (_: Exception) {
        ChatMessageStatus.Complete
    }

    @TypeConverter
    fun fromNoteSource(source: NoteSource): String = source.name

    @TypeConverter
    fun toNoteSource(value: String): NoteSource = try {
        NoteSource.valueOf(value)
    } catch (_: Exception) {
        NoteSource.User
    }

    @TypeConverter
    fun fromTaskPriority(priority: TaskPriority): String = priority.name

    @TypeConverter
    fun toTaskPriority(value: String): TaskPriority = try {
        TaskPriority.valueOf(value)
    } catch (_: Exception) {
        TaskPriority.Medium
    }

    @TypeConverter
    fun fromTaskStatus(status: TaskStatus): String = status.name

    @TypeConverter
    fun toTaskStatus(value: String): TaskStatus = try {
        TaskStatus.valueOf(value)
    } catch (_: Exception) {
        TaskStatus.Open
    }

    @TypeConverter
    fun fromTaskSource(source: TaskSource): String = source.name

    @TypeConverter
    fun toTaskSource(value: String): TaskSource = try {
        TaskSource.valueOf(value)
    } catch (_: Exception) {
        TaskSource.User
    }

    @TypeConverter
    fun fromActionStatus(status: ActionStatus): String = status.name

    @TypeConverter
    fun toActionStatus(value: String): ActionStatus = try {
        ActionStatus.valueOf(value)
    } catch (_: Exception) {
        ActionStatus.Pending
    }

    @TypeConverter
    fun fromRiskLevel(level: RiskLevel): String = level.name

    @TypeConverter
    fun toRiskLevel(value: String): RiskLevel = try {
        RiskLevel.valueOf(value)
    } catch (_: Exception) {
        RiskLevel.Low
    }

    @TypeConverter
    fun fromStringList(list: List<String>?): String = list?.joinToString(",") ?: ""

    @TypeConverter
    fun toStringList(data: String?): List<String> = if (data.isNullOrEmpty()) emptyList() else data.split(",")
}
