package com.orbit.app.core.tools

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.orbit.app.core.domain.model.RiskLevel
import com.orbit.app.core.domain.model.Tool
import com.orbit.app.core.domain.model.ToolArguments
import com.orbit.app.core.domain.model.ToolContext
import com.orbit.app.core.domain.model.ToolError
import com.orbit.app.core.domain.model.ToolResult
import com.orbit.app.core.domain.model.ToolSpec
import com.orbit.app.core.tools.helpers.ToolIntentLauncher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import javax.inject.Inject

class SetDoNotDisturbTool @Inject constructor(
    @ApplicationContext private val context: Context,
    private val intentLauncher: ToolIntentLauncher
) : Tool {

    override val spec: ToolSpec = ToolSpec(
        name = "set_do_not_disturb",
        description = "Changes Do Not Disturb mode (OFF, PRIORITY_ONLY, ALARMS_ONLY, TOTAL_SILENCE).",
        parametersJsonSchema = """
            {
                "type": "object",
                "properties": {
                    "mode": { "type": "string", "enum": ["OFF", "PRIORITY_ONLY", "ALARMS_ONLY", "TOTAL_SILENCE"] }
                },
                "required": ["mode"]
            }
        """.trimIndent()
    )

    override val riskLevel: RiskLevel = RiskLevel.Medium
    override val displayLabel: String = "Changing Do Not Disturb..."

    override suspend fun execute(args: ToolArguments, ctx: ToolContext): ToolResult {
        val modeStr = args.getString("mode")
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !notificationManager.isNotificationPolicyAccessGranted) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            return intentLauncher.launchIntent(intent, "Opened Notification Policy Access settings for DND permission.")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val filter = when (modeStr.uppercase()) {
                "OFF" -> NotificationManager.INTERRUPTION_FILTER_ALL
                "PRIORITY_ONLY" -> NotificationManager.INTERRUPTION_FILTER_PRIORITY
                "ALARMS_ONLY" -> NotificationManager.INTERRUPTION_FILTER_ALARMS
                "TOTAL_SILENCE" -> NotificationManager.INTERRUPTION_FILTER_NONE
                else -> NotificationManager.INTERRUPTION_FILTER_ALL
            }

            val previousFilter = notificationManager.currentInterruptionFilter
            notificationManager.setInterruptionFilter(filter)

            val undoPayload = buildJsonObject {
                put("previousFilter", previousFilter)
            }.toString()

            return ToolResult.Success(summary = "Do Not Disturb set to $modeStr.", undoPayload = undoPayload)
        }

        return ToolResult.Failure(ToolError.Unavailable, "DND filter change requires Android M+")
    }

    override fun describeAction(args: ToolArguments): String {
        val mode = args.getString("mode")
        return "Set Do Not Disturb to $mode"
    }

    override suspend fun undo(payload: String): ToolResult {
        return try {
            val obj = Json.parseToJsonElement(payload).jsonObject
            val prevFilter = obj["previousFilter"]?.jsonPrimitive?.content?.toIntOrNull()
                ?: return ToolResult.Failure(ToolError.InvalidArguments, "Invalid payload")

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                notificationManager.setInterruptionFilter(prevFilter)
                ToolResult.Success("Restored prior Do Not Disturb mode.")
            } else {
                ToolResult.Failure(ToolError.Unavailable, "Android M+ required")
            }
        } catch (e: Exception) {
            ToolResult.Failure(ToolError.Unknown(e.message ?: "Undo failed"), "Undo error.")
        }
    }
}
