package com.orbit.app.core.tools

import com.orbit.app.core.domain.model.RiskLevel
import com.orbit.app.core.domain.model.TaskPriority
import com.orbit.app.core.domain.model.Tool
import com.orbit.app.core.domain.model.ToolArguments
import com.orbit.app.core.domain.model.ToolContext
import com.orbit.app.core.domain.model.ToolResult
import com.orbit.app.core.domain.model.ToolSpec
import com.orbit.app.core.domain.repository.TaskRepository
import com.orbit.app.core.tools.helpers.DateTimeArgParser
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import javax.inject.Inject

class AddTaskTool @Inject constructor(
    private val taskRepository: TaskRepository
) : Tool {

    override val spec: ToolSpec = ToolSpec(
        name = "add_task",
        description = "Adds a to-do task. dueAt is optional ISO date-time string. priority is Low, Medium, or High.",
        parametersJsonSchema = """
            {
                "type": "object",
                "properties": {
                    "title": { "type": "string", "maxLength": 120 },
                    "notes": { "type": "string", "maxLength": 300 },
                    "dueAt": { "type": "string" },
                    "priority": { "type": "string", "enum": ["Low", "Medium", "High"], "default": "Medium" }
                },
                "required": ["title"]
            }
        """.trimIndent()
    )

    override val riskLevel: RiskLevel = RiskLevel.Low
    override val displayLabel: String = "Adding a task..."

    override suspend fun execute(args: ToolArguments, ctx: ToolContext): ToolResult {
        val title = args.getString("title")
        val notes = args.getOptionalString("notes")
        val dueIso = args.getOptionalString("dueAt")
        val prioStr = args.getOptionalString("priority") ?: "Medium"

        val priority = try { TaskPriority.valueOf(prioStr) } catch (_: Exception) { TaskPriority.Medium }
        val dueAt = dueIso?.let { DateTimeArgParser.parseIsoToEpochMillis(it, ctx.timeZoneId) }

        val res = taskRepository.createTask(title, notes, dueAt, priority)
        return when (res) {
            is com.orbit.app.core.domain.result.Result.Success -> {
                val task = res.data
                val undoPayload = buildJsonObject { put("taskId", task.id) }.toString()
                ToolResult.Success(summary = "Task '$title' created.", undoPayload = undoPayload)
            }
            else -> ToolResult.Failure(com.orbit.app.core.domain.model.ToolError.Unknown("Task create failed"), "Database error.")
        }
    }

    override fun describeAction(args: ToolArguments): String = "Add task '${args.getString("title")}'"

    override suspend fun undo(payload: String): ToolResult {
        return try {
            val obj = Json.parseToJsonElement(payload).jsonObject
            val taskId = obj["taskId"]?.jsonPrimitive?.content ?: return ToolResult.Failure(com.orbit.app.core.domain.model.ToolError.InvalidArguments, "Invalid payload")
            taskRepository.deleteTask(taskId)
            ToolResult.Success("Task deleted.")
        } catch (e: Exception) {
            ToolResult.Failure(com.orbit.app.core.domain.model.ToolError.Unknown(e.message ?: "Undo failed"), "Error: ${e.message}")
        }
    }
}
