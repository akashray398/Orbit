package com.orbit.app.core.domain.usecase

import com.orbit.app.core.domain.model.ToolResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToolResultSanitizer @Inject constructor() {

    fun sanitize(result: ToolResult, maxChars: Int = 4000): ToolResult {
        return when (result) {
            is ToolResult.Success -> {
                val cleanSummary = stripControlChars(result.summary).take(maxChars)
                result.copy(summary = cleanSummary)
            }
            is ToolResult.Failure -> {
                val cleanMessage = stripControlChars(result.message).take(maxChars)
                result.copy(message = cleanMessage)
            }
        }
    }

    private fun stripControlChars(input: String): String {
        return input.filter { it == '\n' || it == '\r' || it == '\t' || it.code in 32..126 || it.code > 159 }
    }
}
