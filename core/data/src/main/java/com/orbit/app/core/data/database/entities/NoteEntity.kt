package com.orbit.app.core.data.database.entities

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey
import com.orbit.app.core.domain.model.NoteSource

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val body: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val pinned: Boolean = false,
    val source: NoteSource = NoteSource.User,
    val tags: List<String> = emptyList()
)

@Fts4(contentEntity = NoteEntity::class)
@Entity(tableName = "notes_fts")
data class NoteFtsEntity(
    val title: String,
    val body: String
)
