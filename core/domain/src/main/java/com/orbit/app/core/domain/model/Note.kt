package com.orbit.app.core.domain.model

enum class NoteSource {
    User,
    Agent
}

data class Note(
    val id: String,
    val title: String,
    val body: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val pinned: Boolean = false,
    val source: NoteSource = NoteSource.User,
    val tags: List<String> = emptyList()
)
