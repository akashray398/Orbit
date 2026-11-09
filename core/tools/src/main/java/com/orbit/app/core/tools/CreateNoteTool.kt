package com.orbit.app.core.tools

import com.orbit.app.core.domain.model.RiskLevel
import com.orbit.app.core.domain.model.Tool
import com.orbit.app.core.domain.model.ToolArguments
import com.orbit.app.core.domain.model.ToolContext
import com.orbit.app.core.domain.model.ToolResult
import com.orbit.app.core.domain.model.ToolSpec
import com.orbit.app.core.domain.repository.NoteRepository
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import javax.inject.Inject

class CreateNoteTool @Inject constructor(
    private val noteRepository: NoteRepository
) : Tool {

    override val spec: ToolSpec = ToolSpec(
        name = "create_note",
        description = "Creates a persistent note. body max 4000 chars, optional title and tags list.",
        parametersJsonSchema = """
            {
                "type": "object",
                "properties": {
                    "title": { "type": "string", "maxLength": 120 },
                    "body": { "type": "string", "maxLength": 4000 },
                    "pinned": { "type": "boolean", "default": false },
                    "tags": { "type": "array", "items": { "type": "string" } }
                },
                "required": ["body"]
            }
        """.trimIndent()
    )

    override val riskLevel: RiskLevel = RiskLevel.Low
    override val displayLabel: String = "Saving a note..."

    override suspend fun execute(args: ToolArguments, ctx: ToolContext): ToolResult {
        val body = args.getString("body")
        val title = args.getOptionalString("title") ?: body.take(30)
        val tagsStr = args.getOptionalString("tags") ?: ""
        val tagsList = tagsStr.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }

        val res = noteRepository.createNote(title, body, tagsList)
        return when (res) {
            is com.orbit.app.core.domain.result.Result.Success -> {
                val note = res.data
                val undoPayload = buildJsonObject {
                    put("noteId", note.id)
                }.toString()
                ToolResult.Success(summary = "Note '$title' created.", undoPayload = undoPayload)
            }
            is com.orbit.app.core.domain.result.Result.Error -> {
                ToolResult.Failure(com.orbit.app.core.domain.model.ToolError.Unknown(res.appError.message ?: "Failed"), "Save note error.")
            }
            else -> ToolResult.Failure(com.orbit.app.core.domain.model.ToolError.Unknown("Failed"), "Unknown error.")
        }
    }

    override fun describeAction(args: ToolArguments): String = "Create note '${args.getOptionalString("title") ?: "Note"}'"

    override suspend fun undo(payload: String): ToolResult {
        return try {
            val obj = Json.parseToJsonElement(payload).jsonObject
            val noteId = obj["noteId"]?.jsonPrimitive?.content ?: return ToolResult.Failure(com.orbit.app.core.domain.model.ToolError.InvalidArguments, "Invalid payload")
            noteRepository.deleteNote(noteId)
            ToolResult.Success("Note deleted.")
        } catch (e: Exception) {
            ToolResult.Failure(com.orbit.app.core.domain.model.ToolError.Unknown(e.message ?: "Undo failed"), "Error: ${e.message}")
        }
    }
}
