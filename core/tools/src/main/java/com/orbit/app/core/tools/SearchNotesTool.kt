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

class SearchNotesTool @Inject constructor(
    private val noteRepository: NoteRepository
) : Tool {

    override val spec: ToolSpec = ToolSpec(
        name = "search_notes",
        description = "Searches notes by full-text query. Returns note titles and short snippets.",
        parametersJsonSchema = """
            {
                "type": "object",
                "properties": {
                    "query": { "type": "string", "maxLength": 100 },
                    "limit": { "type": "integer", "default": 5 }
                },
                "required": ["query"]
            }
        """.trimIndent()
    )

    override val riskLevel: RiskLevel = RiskLevel.Low
    override val displayLabel: String = "Searching notes..."
    override val isReadOnly: Boolean = true
    override val returnsUntrustedContent: Boolean = true

    override suspend fun execute(args: ToolArguments, ctx: ToolContext): ToolResult {
        val query = args.getString("query")
        val limit = args.getInt("limit")

        val results = noteRepository.searchNotes(query).first().take(limit)
        if (results.isEmpty()) {
            return ToolResult.Success(summary = "No notes matched query '$query'.")
        }

        val summaryList = results.joinToString("\n") { note ->
            "- [ID: ${note.id}] ${note.title}: ${note.body.take(150)}..."
        }

        return ToolResult.Success(summary = "Found ${results.size} notes:\n$summaryList")
    }

    override fun describeAction(args: ToolArguments): String = "Search notes for '${args.getString("query")}'"
}
