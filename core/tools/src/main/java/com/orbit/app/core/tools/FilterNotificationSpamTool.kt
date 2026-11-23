package com.orbit.app.core.tools

import com.orbit.app.core.data.database.daos.NotificationDao
import com.orbit.app.core.domain.model.RiskLevel
import com.orbit.app.core.domain.model.Tool
import com.orbit.app.core.domain.model.ToolArguments
import com.orbit.app.core.domain.model.ToolContext
import com.orbit.app.core.domain.model.ToolResult
import com.orbit.app.core.domain.model.ToolSpec
import javax.inject.Inject

class FilterNotificationSpamTool @Inject constructor(
    private val notificationDao: NotificationDao
) : Tool {

    override val spec: ToolSpec = ToolSpec(
        name = "filter_notification_spam",
        description = "Analyzes captured notifications over past 24 hours to separate urgent/delivery alerts from promotional spam.",
        parametersJsonSchema = """
            {
                "type": "object",
                "properties": {
                    "sinceHours": { "type": "integer", "default": 24 }
                }
            }
        """.trimIndent()
    )

    override val riskLevel: RiskLevel = RiskLevel.Low
    override val displayLabel: String = "Filtering Spam & Urgent Alerts..."
    override val isReadOnly: Boolean = true

    override suspend fun execute(args: ToolArguments, ctx: ToolContext): ToolResult {
        val hours = args.getInt("sinceHours", default = 24)
        val cutoff = System.currentTimeMillis() - (hours * 3600000L)

        val notifications = notificationDao.getRecentNotifications(cutoff, limit = 50)
        if (notifications.isEmpty()) {
            return ToolResult.Success("No notifications captured in the last $hours hours.")
        }

        val urgentKeywords = listOf("delivery", "delivered", "out for delivery", "arriving", "flight", "order", "zomato", "swiggy", "amazon", "flipkart", "uber", "ola", "booking", "otp", "code", "bank", "alert", "calendar", "meeting")
        val spamKeywords = listOf("offer", "discount", "sale", "% off", "cashback", "promo", "deal", "win", "claim", "free", "subscribe", "limited time")

        val urgentList = mutableListOf<String>()
        val spamList = mutableListOf<String>()
        val normalList = mutableListOf<String>()

        notifications.forEach { item ->
            val textLower = "${item.title} ${item.text}".lowercase()
            val entry = "- [${item.appLabel}] ${item.title}: ${item.text}"

            when {
                urgentKeywords.any { textLower.contains(it) } -> urgentList.add(entry)
                spamKeywords.any { textLower.contains(it) } -> spamList.add(entry)
                else -> normalList.add(entry)
            }
        }

        val sb = StringBuilder("🛡️ Notification Guard Report (Last $hours Hours):\n\n")

        if (urgentList.isNotEmpty()) {
            sb.append("🚨 Urgent & High-Priority Alerts (${urgentList.size}):\n")
            urgentList.take(8).forEach { sb.append("$it\n") }
        } else {
            sb.append("🚨 Urgent Alerts: None (No critical delivery or schedule alerts).\n")
        }

        if (spamList.isNotEmpty()) {
            sb.append("\n🚫 Filtered Promotional Spam (${spamList.size}):\n")
            spamList.take(8).forEach { sb.append("$it\n") }
        } else {
            sb.append("\n🚫 Filtered Spam: 0 promotional notifications.\n")
        }

        if (normalList.isNotEmpty()) {
            sb.append("\n💬 Regular App Updates (${normalList.size}):\n")
            normalList.take(5).forEach { sb.append("$it\n") }
        }

        return ToolResult.Success(summary = sb.toString())
    }

    override fun describeAction(args: ToolArguments): String = "Filter Notification Spam & Urgent Alerts"
}
