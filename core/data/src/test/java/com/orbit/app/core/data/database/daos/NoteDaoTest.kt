package com.orbit.app.core.data.database.daos

import com.orbit.app.core.data.database.entities.NoteEntity
import com.orbit.app.core.domain.model.NoteSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class NoteDaoTest {

    @Test
    fun noteEntity_creation_maps_correctly() {
        val note = NoteEntity(
            id = "note_101",
            title = "DSA Practice Notes",
            body = "Solve binary tree traversals and dynamic programming problems.",
            source = NoteSource.User,
            tags = listOf("dsa", "interview")
        )

        assertEquals("note_101", note.id)
        assertEquals("DSA Practice Notes", note.title)
        assertEquals(2, note.tags.size)
        assertNotNull(note.createdAt)
    }
}
