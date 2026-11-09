package com.orbit.app.core.tools

import com.orbit.app.core.domain.model.RiskLevel
import com.orbit.app.core.domain.model.Tool
import com.orbit.app.core.domain.model.ToolArguments
import com.orbit.app.core.domain.model.ToolContext
import com.orbit.app.core.domain.model.ToolError
import com.orbit.app.core.domain.model.ToolResult
import com.orbit.app.core.domain.model.ToolSpec
import com.orbit.app.core.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CancelReminderTool @Inject constructor(
    private val reminderRepository: ReminderRepository
) : Tool {

    override val spec: ToolSpec = ToolSpec(
        name = "cancel_reminder",
        description = "Cancels a scheduled reminder by id or matching title query.",
        parametersJsonSchema = """
            {
                "type": "object",
                "properties": {
                    "reminderId": { "type": "string" },
                    "query": { "type": "string" }
                }
            }
        """.trimIndent()
    )

    override val riskLevel: RiskLevel = RiskLevel.Low
    override val displayLabel: String = "Cancelling reminder..."

    override suspend fun execute(args: ToolArguments, ctx: ToolContext): ToolResult {
        val reminderId = args.getOptionalString("reminderId")
        val query = args.getOptionalString("query")

        if (reminderId != null) {
            reminderRepository.cancelReminder(reminderId)
            return ToolResult.Success("Reminder '$reminderId' cancelled.")
        }

        if (query != null) {
            val upcoming = reminderRepository.upcomingReminders.first()
            val matches = upcoming.filter { it.title.contains(query, ignoreCase = true) }
            if (matches.isEmpty()) {
                return ToolResult.Failure(ToolError.NotFound, "No matching reminder found for '$query'.")
            }
            if (matches.size == 1) {
                reminderRepository.cancelReminder(matches.first().id)
                return ToolResult.Success("Reminder '${matches.first().title}' cancelled.")
            }
            val titles = matches.joinToString(", ") { "'${it.title}'" }
            return ToolResult.Failure(ToolError.InvalidArguments, "Multiple matching reminders found: $titles. Please specify by id.")
        }

        return ToolResult.Failure(ToolError.InvalidArguments, "Please provide reminderId or query string.")
    }

    override fun describeAction(args: ToolArguments): String = "Cancel reminder"
}
