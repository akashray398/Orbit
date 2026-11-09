package com.orbit.app.core.tools

import com.orbit.app.core.domain.model.RiskLevel
import com.orbit.app.core.domain.model.Tool
import com.orbit.app.core.domain.model.ToolArguments
import com.orbit.app.core.domain.model.ToolContext
import com.orbit.app.core.domain.model.ToolResult
import com.orbit.app.core.domain.model.ToolSpec
import com.orbit.app.core.domain.repository.TaskRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ListTasksTool @Inject constructor(
    private val taskRepository: TaskRepository
) : Tool {

    override val spec: ToolSpec = ToolSpec(
        name = "list_tasks",
        description = "Lists user tasks filtered by OPEN, DONE, or DUE_TODAY.",
        parametersJsonSchema = """
            {
                "type": "object",
                "properties": {
                    "filter": { "type": "string", "enum": ["OPEN", "DONE", "DUE_TODAY"], "default": "OPEN" },
                    "limit": { "type": "integer", "default": 10 }
                }
            }
        """.trimIndent()
    )

    override val riskLevel: RiskLevel = RiskLevel.Low
    override val displayLabel: String = "Listing tasks..."
    override val isReadOnly: Boolean = true

    override suspend fun execute(args: ToolArguments, ctx: ToolContext): ToolResult {
        val filter = args.getOptionalString("filter") ?: "OPEN"
        val limit = args.getInt("limit")

        val tasks = when (filter.uppercase()) {
            "DONE" -> taskRepository.completedTasks.first().take(limit)
            "DUE_TODAY" -> taskRepository.dueTodayTasks.first().take(limit)
            else -> taskRepository.openTasks.first().take(limit)
        }

        if (tasks.isEmpty()) {
            return ToolResult.Success("No $filter tasks found.")
        }

        val taskLines = tasks.joinToString("\n") { t ->
            "- [ID: ${t.id}] ${t.title} (${t.priority} priority)"
        }

        return ToolResult.Success("Tasks ($filter):\n$taskLines")
    }

    override fun describeAction(args: ToolArguments): String = "List tasks"
}
