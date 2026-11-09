package com.orbit.app.core.tools

import com.orbit.app.core.domain.model.RiskLevel
import com.orbit.app.core.domain.model.Tool
import com.orbit.app.core.domain.model.ToolArguments
import com.orbit.app.core.domain.model.ToolContext
import com.orbit.app.core.domain.model.ToolResult
import com.orbit.app.core.domain.model.ToolSpec
import com.orbit.app.core.domain.repository.NoteRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AppendToNoteTool @Inject constructor(
    private val noteRepository: NoteRepository
) : Tool {

    override val spec: ToolSpec = ToolSpec(
        name = "append_to_note",
        description = "Appends text to an existing note by noteId or titleMatch.",
        parametersJsonSchema = """
            {
                "type": "object",
                "properties": {
                    "noteId": { "type": "string" },
                    "titleMatch": { "type": "string" },
                    "text": { "type": "string", "maxLength": 2000 }
                },
                "required": ["text"]
            }
        """.trimIndent()
    )

    override val riskLevel: RiskLevel = RiskLevel.Low
    override val displayLabel: String = "Appending to note..."

    override suspend fun execute(args: ToolArguments, ctx: ToolContext): ToolResult {
        val textToAppend = args.getString("text")
        val noteId = args.getOptionalString("noteId")
        val titleMatch = args.getOptionalString("titleMatch")

        val allNotes = noteRepository.notes.first()
        val targetNote = when {
            noteId != null -> allNotes.find { it.id == noteId }
            titleMatch != null -> {
                val matches = allNotes.filter { it.title.contains(titleMatch, ignoreCase = true) }
                if (matches.size > 1) {
                    val titles = matches.joinToString(", ") { "'${it.title}'" }
                    return ToolResult.Failure(com.orbit.app.core.domain.model.ToolError.InvalidArguments, "Multiple notes match: $titles. Please specify by noteId.")
                }
                matches.firstOrNull()
            }
            else -> null
        } ?: return ToolResult.Failure(com.orbit.app.core.domain.model.ToolError.NotFound, "Note not found.")

        val updatedNote = targetNote.copy(
            body = "${targetNote.body}\n$textToAppend",
            updatedAt = System.currentTimeMillis()
        )

        noteRepository.updateNote(updatedNote)
        return ToolResult.Success(summary = "Appended text to note '${targetNote.title}'.")
    }

    override fun describeAction(args: ToolArguments): String = "Append text to note"
}
