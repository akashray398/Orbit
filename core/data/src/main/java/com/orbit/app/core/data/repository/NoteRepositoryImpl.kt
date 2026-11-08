package com.orbit.app.core.data.repository

import com.orbit.app.core.common.result.safeCall
import com.orbit.app.core.data.database.daos.NoteDao
import com.orbit.app.core.data.mapper.toDomain
import com.orbit.app.core.data.mapper.toEntity
import com.orbit.app.core.domain.model.Note
import com.orbit.app.core.domain.repository.NoteRepository
import com.orbit.app.core.domain.result.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao
) : NoteRepository {

    override val notes: Flow<List<Note>> = noteDao.observeNotes().map { list ->
        list.map { it.toDomain() }
    }

    override fun observeNote(id: String): Flow<Note?> = noteDao.observeNote(id).map { it?.toDomain() }

    override fun searchNotes(query: String): Flow<List<Note>> = noteDao.searchNotes(query).map { list ->
        list.map { it.toDomain() }
    }

    override suspend fun createNote(title: String, body: String, tags: List<String>): Result<Note> = safeCall {
        val note = Note(
            id = UUID.randomUUID().toString(),
            title = title,
            body = body,
            tags = tags
        )
        noteDao.insertNote(note.toEntity())
        note
    }

    override suspend fun updateNote(note: Note): Result<Unit> = safeCall {
        noteDao.updateNote(note.toEntity())
    }

    override suspend fun togglePin(id: String): Result<Unit> = safeCall {
        noteDao.togglePin(id)
    }

    override suspend fun deleteNote(id: String): Result<Unit> = safeCall {
        noteDao.deleteNote(id)
    }
}
