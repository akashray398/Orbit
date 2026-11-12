package com.orbit.app.core.tools

import com.orbit.app.core.data.database.daos.TaskDao
import com.orbit.app.core.domain.model.RiskLevel
import com.orbit.app.core.domain.model.Tool
import com.orbit.app.core.domain.model.ToolArguments
import com.orbit.app.core.domain.model.ToolContext
import com.orbit.app.core.domain.model.ToolResult
import com.orbit.app.core.domain.model.ToolSpec
import javax.inject.Inject

class PersonalCoachTool @Inject constructor(
    private val taskDao: TaskDao
) : Tool {

    override val spec: ToolSpec = ToolSpec(
        name = "get_personal_coaching",
        description = "Generates a personalized 24-hour study, motivation, focus, and rest coaching plan based on tasks and notes.",
        parametersJsonSchema = """
            {
                "type": "object",
                "properties": {
                    "focusGoal": { "type": "string", "default": "Study & Personal Growth" }
                }
            }
        """.trimIndent()
    )

    override val riskLevel: RiskLevel = RiskLevel.Low
    override val displayLabel: String = "Generating 24h Personal Coaching..."
    override val isReadOnly: Boolean = true

    override suspend fun execute(args: ToolArguments, ctx: ToolContext): ToolResult {
        val openTasks = taskDao.getOpenTasks()
        val openTaskTitles = openTasks.take(5).map { it.title }

        val sb = StringBuilder("🔥 Your Personal AI 24-Hour Growth & Focus Plan:\n\n")

        sb.append("⚡ **Personal Motivation**: You have incredible potential! Consistent daily action is what separates dreamers from achievers. Today is your day to push forward and conquer your goals.\n\n")

        sb.append("🎓 **Study & Deep Learning Focus**:\n")
        if (openTaskTitles.isNotEmpty()) {
            openTaskTitles.forEachIndexed { idx, title ->
                sb.append("${idx + 1}. $title (50-min deep work session)\n")
            }
        } else {
            sb.append("1. Practice Core Concepts (DSA / System Architecture)\n")
            sb.append("2. Build hands-on feature code for 90 minutes\n")
        }

        sb.append("\n🧠 **Focus & Energy Schedule**:\n")
        sb.append("• **Morning (09:00 - 12:00)**: Peak mental energy — Deep study & problem solving.\n")
        sb.append("• **Afternoon (02:00 - 05:00)**: Execution block — Tasks, project development & coding.\n")
        sb.append("• **Evening (07:00 - 09:00)**: Review & active recall learning.\n")

        sb.append("\n🧘 **Rest & Recovery Plan**:\n")
        sb.append("• Take 10-minute walk breaks after every 50 minutes of deep study.\n")
        sb.append("• Hydrate with 3L water throughout the day.\n")
        sb.append("• Unplug screens 30 minutes before sleep for full mental recovery.\n")

        return ToolResult.Success(summary = sb.toString())
    }

    override fun describeAction(args: ToolArguments): String = "Generate 24h Personal AI Coaching Plan"
}
