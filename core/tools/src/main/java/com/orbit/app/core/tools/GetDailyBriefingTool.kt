package com.orbit.app.core.tools

import com.orbit.app.core.data.database.daos.NotificationDao
import com.orbit.app.core.data.database.daos.TaskDao
import com.orbit.app.core.domain.model.RiskLevel
import com.orbit.app.core.domain.model.Tool
import com.orbit.app.core.domain.model.ToolArguments
import com.orbit.app.core.domain.model.ToolContext
import com.orbit.app.core.domain.model.ToolResult
import com.orbit.app.core.domain.model.ToolSpec
import java.util.Calendar
import javax.inject.Inject

class GetDailyBriefingTool @Inject constructor(
    private val taskDao: TaskDao,
    private val notificationDao: NotificationDao
) : Tool {

    override val spec: ToolSpec = ToolSpec(
        name = "get_daily_briefing",
        description = "Generates a complete executive morning/evening briefing compiling calendar events, open tasks, and recent notifications.",
        parametersJsonSchema = """
            {
                "type": "object",
                "properties": {
                    "type": { "type": "string", "enum": ["Morning", "Evening"], "default": "Morning" }
                }
            }
        """.trimIndent()
    )

    override val riskLevel: RiskLevel = RiskLevel.Low
    override val displayLabel: String = "Generating Executive Daily Briefing..."
    override val isReadOnly: Boolean = true

    override suspend fun execute(args: ToolArguments, ctx: ToolContext): ToolResult {
        val type = args.getOptionalString("type") ?: "Morning"

        val openTasks = taskDao.getOpenTasks()
        val cutoff = System.currentTimeMillis() - 43200000L // last 12 hours
        val notifications = notificationDao.getRecentNotifications(cutoff, limit = 15)

        val sb = StringBuilder()
        if (type.equals("Evening", ignoreCase = true)) {
            sb.append("🌙 Executive Evening Review:\n\n")
            sb.append("✅ Open Tasks Pending (${openTasks.size}):\n")
            if (openTasks.isNotEmpty()) {
                openTasks.take(5).forEachIndexed { idx, t ->
                    sb.append("${idx + 1}. ${t.title}\n")
                }
            } else {
                sb.append("• All open tasks completed! Great work.\n")
            }

            sb.append("\n💬 Evening Notifications Summary (${notifications.size}):\n")
            if (notifications.isNotEmpty()) {
                notifications.take(5).forEach { n ->
                    sb.append("• [${n.appLabel}] ${n.title}: ${n.text}\n")
                }
            } else {
                sb.append("• No unread notifications.\n")
            }

            sb.append("\n🧘 Wind Down Tip: Rest well tonight to recharge for another high-impact day tomorrow!")
        } else {
            sb.append("🌅 Morning Executive Briefing:\n\n")
            sb.append("📋 Today's Top Open Tasks (${openTasks.size}):\n")
            if (openTasks.isNotEmpty()) {
                openTasks.take(5).forEachIndexed { idx, t ->
                    sb.append("${idx + 1}. ${t.title}\n")
                }
            } else {
                sb.append("• No open tasks for today.\n")
            }

            sb.append("\n🔔 Overnight Notifications (${notifications.size}):\n")
            if (notifications.isNotEmpty()) {
                notifications.take(5).forEach { n ->
                    sb.append("• [${n.appLabel}] ${n.title}: ${n.text}\n")
                }
            } else {
                sb.append("• Clean slate! No missed notifications.\n")
            }

            sb.append("\n🔥 AI Motivation: Start with your highest priority task first and make today count!")
        }

        return ToolResult.Success(summary = sb.toString())
    }

    override fun describeAction(args: ToolArguments): String {
        val type = args.getOptionalString("type") ?: "Morning"
        return "Generate $type Executive Briefing"
    }
}
