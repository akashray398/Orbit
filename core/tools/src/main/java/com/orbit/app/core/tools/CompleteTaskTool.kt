package com.orbit.app.core.tools

import com.orbit.app.core.domain.model.RiskLevel
import com.orbit.app.core.domain.model.Tool
import com.orbit.app.core.domain.model.ToolArguments
import com.orbit.app.core.domain.model.ToolContext
import com.orbit.app.core.domain.model.ToolResult
import com.orbit.app.core.domain.model.ToolSpec
import com.orbit.app.core.domain.repository.TaskRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import javax.inject.Inject

class CompleteTaskTool @Inject constructor(
    private val taskRepository: TaskRepository
) : Tool {

    override val spec: ToolSpec = ToolSpec(
        name = "complete_task",
        description = "Marks a task complete by taskId or titleMatch.",
        parametersJsonSchema = """
            {
                "type": "object",
                "properties": {
                    "taskId": { "type": "string" },
                    "titleMatch": { "type": "string" }
                }
            }
        """.trimIndent()
    )

    override val riskLevel: RiskLevel = RiskLevel.Low
    override val displayLabel: String = "Completing task..."

    override suspend fun execute(args: ToolArguments, ctx: ToolContext): ToolResult {
        val taskId = args.getOptionalString("taskId")
        val titleMatch = args.getOptionalString("titleMatch")

        val openTasks = taskRepository.openTasks.first()
        val target = when {
            taskId != null -> openTasks.find { it.id == taskId }
            titleMatch != null -> {
                val matches = openTasks.filter { it.title.contains(titleMatch, ignoreCase = true) }
                if (matches.size > 1) {
                    val titles = matches.joinToString(", ") { "'${it.title}'" }
                    return ToolResult.Failure(com.orbit.app.core.domain.model.ToolError.InvalidArguments, "Multiple tasks match: $titles. Please specify by taskId.")
                }
                matches.firstOrNull()
            }
            else -> null
        } ?: return ToolResult.Failure(com.orbit.app.core.domain.model.ToolError.NotFound, "Open task not found.")

        taskRepository.completeTask(target.id)
        val undoPayload = buildJsonObject { put("taskId", target.id) }.toString()
        return ToolResult.Success(summary = "Task '${target.title}' marked complete.", undoPayload = undoPayload)
    }

    override fun describeAction(args: ToolArguments): String = "Complete task"

    override suspend fun undo(payload: String): ToolResult {
        return try {
            val obj = Json.parseToJsonElement(payload).jsonObject
            val taskId = obj["taskId"]?.jsonPrimitive?.content ?: return ToolResult.Failure(com.orbit.app.core.domain.model.ToolError.InvalidArguments, "Invalid payload")
            taskRepository.reopenTask(taskId)
            ToolResult.Success("Task reopened.")
        } catch (e: Exception) {
            ToolResult.Failure(com.orbit.app.core.domain.model.ToolError.Unknown(e.message ?: "Undo failed"), "Error: ${e.message}")
        }
    }
}
