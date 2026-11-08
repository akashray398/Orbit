package com.orbit.app.core.domain.repository

import com.orbit.app.core.domain.model.Note
import com.orbit.app.core.domain.result.Result
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    val notes: Flow<List<Note>>
    fun observeNote(id: String): Flow<Note?>
    fun searchNotes(query: String): Flow<List<Note>>
    suspend fun createNote(title: String, body: String, tags: List<String>): Result<Note>
    suspend fun updateNote(note: Note): Result<Unit>
    suspend fun togglePin(id: String): Result<Unit>
    suspend fun deleteNote(id: String): Result<Unit>
}
