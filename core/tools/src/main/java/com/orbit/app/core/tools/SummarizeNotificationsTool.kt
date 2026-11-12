package com.orbit.app.core.tools

import com.orbit.app.core.data.database.daos.NotificationDao
import com.orbit.app.core.domain.model.RiskLevel
import com.orbit.app.core.domain.model.Tool
import com.orbit.app.core.domain.model.ToolArguments
import com.orbit.app.core.domain.model.ToolContext
import com.orbit.app.core.domain.model.ToolResult
import com.orbit.app.core.domain.model.ToolSpec
import javax.inject.Inject

class SummarizeNotificationsTool @Inject constructor(
    private val notificationDao: NotificationDao
) : Tool {

    override val spec: ToolSpec = ToolSpec(
        name = "summarize_notifications",
        description = "Reads and organizes captured notifications over the past 24 hours (messages, calls, deliveries, and app updates).",
        parametersJsonSchema = """
            {
                "type": "object",
                "properties": {
                    "sinceHours": { "type": "integer", "default": 24 },
                    "maxItems": { "type": "integer", "default": 50 }
                }
            }
        """.trimIndent()
    )

    override val riskLevel: RiskLevel = RiskLevel.Low
    override val displayLabel: String = "Summarizing past 24h notifications..."
    override val isReadOnly: Boolean = true
    override val returnsUntrustedContent: Boolean = true

    override suspend fun execute(args: ToolArguments, ctx: ToolContext): ToolResult {
        val hours = args.getInt("sinceHours", default = 24)
        val maxItems = args.getInt("maxItems", default = 50)
        val cutoff = System.currentTimeMillis() - (hours * 3600000L)

        val notifications = notificationDao.getRecentNotifications(cutoff, maxItems)
        if (notifications.isEmpty()) {
            return ToolResult.Success("No notifications captured in the last $hours hours. Ensure Orbit Notification Listener is enabled in Settings & Privacy.")
        }

        val messagingKeywords = listOf("whatsapp", "telegram", "signal", "message", "call", "phone", "truecaller", "chat")
        val deliveryKeywords = listOf("zomato", "swiggy", "amazon", "flipkart", "uber", "ola", "rapido", "blinkit", "zepto")
        val socialKeywords = listOf("instagram", "linkedin", "youtube", "twitter", "x", "reddit", "facebook")

        val messagesList = mutableListOf<String>()
        val deliveriesList = mutableListOf<String>()
        val socialList = mutableListOf<String>()
        val generalList = mutableListOf<String>()

        notifications.forEach { item ->
            val pkgLower = item.packageName.lowercase()
            val appLower = item.appLabel.lowercase()
            val entry = "- [${item.appLabel}] ${item.title}: ${item.text}"

            when {
                messagingKeywords.any { pkgLower.contains(it) || appLower.contains(it) } -> messagesList.add(entry)
                deliveryKeywords.any { pkgLower.contains(it) || appLower.contains(it) } -> deliveriesList.add(entry)
                socialKeywords.any { pkgLower.contains(it) || appLower.contains(it) } -> socialList.add(entry)
                else -> generalList.add(entry)
            }
        }

        val sb = StringBuilder("📊 24-Hour Notification Summary (${notifications.size} total):\n")

        if (messagesList.isNotEmpty()) {
            sb.append("\n💬 Messages & Calls (${messagesList.size}):\n")
            messagesList.take(10).forEach { sb.append("$it\n") }
        }

        if (deliveriesList.isNotEmpty()) {
            sb.append("\n📦 Orders & Deliveries (${deliveriesList.size}):\n")
            deliveriesList.take(10).forEach { sb.append("$it\n") }
        }

        if (socialList.isNotEmpty()) {
            sb.append("\n🌐 Social & Updates (${socialList.size}):\n")
            socialList.take(10).forEach { sb.append("$it\n") }
        }

        if (generalList.isNotEmpty()) {
            sb.append("\n🔔 Other App Updates (${generalList.size}):\n")
            generalList.take(10).forEach { sb.append("$it\n") }
        }

        return ToolResult.Success(summary = sb.toString())
    }

    override fun describeAction(args: ToolArguments): String = "Summarize past 24h notifications"
}
