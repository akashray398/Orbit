package com.orbit.app.core.domain.model

data class SuggestionChipModel(
    val id: String,
    val label: String,
    val promptText: String = label
)
