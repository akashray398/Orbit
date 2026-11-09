package com.orbit.app.core.domain.model

import kotlinx.serialization.json.JsonObject

sealed interface ToolResult {
    data class Success(
        val summary: String,
        val data: JsonObject? = null,
        val undoPayload: String? = null
    ) : ToolResult

    data class Failure(
        val error: ToolError,
        val message: String
    ) : ToolResult
}
