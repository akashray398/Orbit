package com.orbit.app.core.data.database

import com.orbit.app.core.data.database.entities.ConversationEntity
import com.orbit.app.core.data.database.entities.NoteEntity
import com.orbit.app.core.data.database.entities.TaskEntity
import com.orbit.app.core.domain.model.NoteSource
import com.orbit.app.core.domain.model.TaskPriority
import com.orbit.app.core.domain.model.TaskSource
import com.orbit.app.core.domain.model.TaskStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseSeeder @Inject constructor(
    private val database: OrbitDatabase
) {
    suspend fun seedSampleData() {
        database.conversationDao().insertConversation(
            ConversationEntity("default_conversation", "Main Chat")
        )

        database.noteDao().insertNote(
            NoteEntity(
                id = "sample_note_1",
                title = "Orbit Architecture Ideas",
                body = "Multi-module Clean Architecture with Room, DataStore, and Hilt.",
                pinned = true,
                source = NoteSource.Agent,
                tags = listOf("architecture", "android")
            )
        )

        database.taskDao().insertTask(
            TaskEntity(
                id = "sample_task_1",
                title = "Review AI Tool-Calling Engine",
                notes = "Verify JSON schema and tool execution parameters.",
                priority = TaskPriority.High,
                status = TaskStatus.Open,
                source = TaskSource.User
            )
        )
    }
}
