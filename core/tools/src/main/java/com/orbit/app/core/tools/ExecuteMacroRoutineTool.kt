package com.orbit.app.core.tools

import com.orbit.app.core.domain.model.ActionStatus
import com.orbit.app.core.domain.model.RiskLevel
import com.orbit.app.core.domain.model.Tool
import com.orbit.app.core.domain.model.ToolArguments
import com.orbit.app.core.domain.model.ToolContext
import com.orbit.app.core.domain.model.ToolError
import com.orbit.app.core.domain.model.ToolResult
import com.orbit.app.core.domain.model.ToolSpec
import com.orbit.app.core.domain.repository.ActionLogRepository
import com.orbit.app.core.domain.repository.ToolRegistry
import javax.inject.Inject
import javax.inject.Provider

class ExecuteMacroRoutineTool @Inject constructor(
    private val toolRegistryProvider: Provider<ToolRegistry>,
    private val actionLogRepository: ActionLogRepository
) : Tool {

    override val spec: ToolSpec = ToolSpec(
        name = "execute_macro_routine",
        description = "Executes a multi-step macro routine (Focus Mode, Workout Mode, Evening Wind-Down) sequentially.",
        parametersJsonSchema = """
            {
                "type": "object",
                "properties": {
                    "routineName": { "type": "string", "enum": ["Focus Mode", "Workout Mode", "Evening Wind Down"], "default": "Focus Mode" }
                }
            }
        """.trimIndent()
    )

    override val riskLevel: RiskLevel = RiskLevel.Low
    override val displayLabel: String = "Executing Macro Routine..."

    private data class MacroStep(
        val toolName: String,
        val argsJson: String,
        val description: String
    )

    override suspend fun execute(args: ToolArguments, ctx: ToolContext): ToolResult {
        val routineName = args.getOptionalString("routineName") ?: "Focus Mode"

        val steps = when (routineName.lowercase()) {
            "workout mode" -> listOf(
                MacroStep("set_timer", "{\"durationSeconds\":2700,\"label\":\"Workout Session\"}", "Setting 45-min workout timer"),
                MacroStep("set_do_not_disturb", "{\"mode\":\"PRIORITY_ONLY\"}", "Enabling Do Not Disturb"),
                MacroStep("create_note", "{\"title\":\"Workout Log\",\"body\":\"Daily workout session started.\"}", "Logging workout session note")
            )
            "evening wind down" -> listOf(
                MacroStep("set_do_not_disturb", "{\"mode\":\"PRIORITY_ONLY\"}", "Enabling Priority Do Not Disturb"),
                MacroStep("set_alarm", "{\"hour\":7,\"minute\":0,\"label\":\"Morning Wakeup\"}", "Setting 7:00 AM wakeup alarm"),
                MacroStep("create_note", "{\"title\":\"Evening Reflections\",\"body\":\"Nightly review logged.\"}", "Logging evening reflection note")
            )
            else -> listOf( // Default Focus Mode
                MacroStep("set_do_not_disturb", "{\"mode\":\"PRIORITY_ONLY\"}", "Enabling Priority Do Not Disturb"),
                MacroStep("set_timer", "{\"durationSeconds\":3000,\"label\":\"Pomodoro Focus\"}", "Setting 50-min study focus timer"),
                MacroStep("create_reminder", "{\"title\":\"Take 10-min study break\"}", "Scheduling 10-min break reminder"),
                MacroStep("create_note", "{\"title\":\"Focus Session Log\",\"body\":\"Study focus session started.\"}", "Logging study session note")
            )
        }

        val completedSteps = mutableListOf<String>()
        val toolRegistry = toolRegistryProvider.get()

        for (step in steps) {
            val tool = toolRegistry.findTool(step.toolName) ?: continue
            val toolArgs = ToolArguments.fromJsonString(step.argsJson)

            val logRes = actionLogRepository.startAction(step.toolName, step.argsJson, tool.riskLevel)
            val actionLogId = (logRes as? com.orbit.app.core.domain.result.Result.Success)?.data?.id

            val result = try {
                tool.execute(toolArgs, ctx)
            } catch (e: Exception) {
                ToolResult.Failure(ToolError.Unknown(e.message ?: "Step failed"), e.message ?: "Step execution error")
            }

            val (status, summary) = when (result) {
                is ToolResult.Success -> {
                    completedSteps.add("✔ ${step.description}: ${result.summary}")
                    Pair(ActionStatus.Success, result.summary)
                }
                is ToolResult.Failure -> {
                    completedSteps.add("✖ ${step.description}: ${result.message}")
                    Pair(ActionStatus.Failed, result.message)
                }
            }

            if (actionLogId != null) {
                actionLogRepository.finishAction(actionLogId, status, summary, null)
            }
        }

        val report = StringBuilder("⚡ Macro Routine '$routineName' Executed (${completedSteps.size} steps):\n\n")
        completedSteps.forEach { report.append("$it\n") }

        return ToolResult.Success(summary = report.toString())
    }

    override fun describeAction(args: ToolArguments): String {
        val routineName = args.getOptionalString("routineName") ?: "Focus Mode"
        return "Execute Macro Routine '$routineName'"
    }
}
