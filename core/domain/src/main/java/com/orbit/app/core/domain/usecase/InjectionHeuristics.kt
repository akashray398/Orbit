package com.orbit.app.core.domain.usecase

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InjectionHeuristics @Inject constructor() {

    private val injectionPatterns = listOf(
        "ignore previous instructions",
        "system prompt",
        "you must now",
        "delete all",
        "send money",
        "transfer to"
    )

    fun containsInjectionPattern(text: String): Boolean {
        val lower = text.lowercase()
        return injectionPatterns.any { lower.contains(it) }
    }
}
