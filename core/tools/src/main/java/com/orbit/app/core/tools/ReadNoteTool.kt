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

class ReadNoteTool @Inject constructor(
    private val noteRepository: NoteRepository
) : Tool {

    override val spec: ToolSpec = ToolSpec(
        name = "read_note",
        description = "Reads full body of a note by noteId or titleMatch.",
        parametersJsonSchema = """
            {
                "type": "object",
                "properties": {
                    "noteId": { "type": "string" },
                    "titleMatch": { "type": "string" }
                }
            }
        """.trimIndent()
    )

    override val riskLevel: RiskLevel = RiskLevel.Low
    override val displayLabel: String = "Reading note..."
    override val isReadOnly: Boolean = true
    override val returnsUntrustedContent: Boolean = true

    override suspend fun execute(args: ToolArguments, ctx: ToolContext): ToolResult {
        val noteId = args.getOptionalString("noteId")
        val titleMatch = args.getOptionalString("titleMatch")

        val allNotes = noteRepository.notes.first()
        val targetNote = when {
            noteId != null -> allNotes.find { it.id == noteId }
            titleMatch != null -> allNotes.find { it.title.contains(titleMatch, ignoreCase = true) }
            else -> null
        } ?: return ToolResult.Failure(com.orbit.app.core.domain.model.ToolError.NotFound, "Note not found.")

        return ToolResult.Success(summary = "Title: ${targetNote.title}\nBody: ${targetNote.body}")
    }

    override fun describeAction(args: ToolArguments): String = "Read note content"
}
