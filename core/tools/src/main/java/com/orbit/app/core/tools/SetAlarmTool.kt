package com.orbit.app.core.tools

import android.content.Intent
import android.provider.AlarmClock
import com.orbit.app.core.domain.model.RiskLevel
import com.orbit.app.core.domain.model.Tool
import com.orbit.app.core.domain.model.ToolArguments
import com.orbit.app.core.domain.model.ToolContext
import com.orbit.app.core.domain.model.ToolResult
import com.orbit.app.core.domain.model.ToolSpec
import com.orbit.app.core.tools.helpers.ToolIntentLauncher
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

class SetAlarmTool @Inject constructor(
    private val intentLauncher: ToolIntentLauncher
) : Tool {

    override val spec: ToolSpec = ToolSpec(
        name = "set_alarm",
        description = "Sets an alarm in the system clock app. hour must be 0-23, minute 0-59.",
        parametersJsonSchema = """
            {
                "type": "object",
                "properties": {
                    "hour": { "type": "integer", "minimum": 0, "maximum": 23 },
                    "minute": { "type": "integer", "minimum": 0, "maximum": 59 },
                    "label": { "type": "string", "maxLength": 60 },
                    "skipUi": { "type": "boolean", "default": true }
                },
                "required": ["hour", "minute"]
            }
        """.trimIndent()
    )

    override val riskLevel: RiskLevel = RiskLevel.Low
    override val displayLabel: String = "Setting an alarm..."

    override suspend fun execute(args: ToolArguments, ctx: ToolContext): ToolResult {
        val hour = args.getInt("hour", default = 9)
        val minute = args.getInt("minute", default = 0)
        val label = args.getOptionalString("label") ?: "Orbit Alarm"
        val skipUi = args.getBoolean("skipUi", default = true)

        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            putExtra(AlarmClock.EXTRA_MESSAGE, label)
            putExtra(AlarmClock.EXTRA_SKIP_UI, skipUi)
        }

        val formattedTime = String.format("%02d:%02d", hour, minute)
        val summary = "Alarm request for $formattedTime sent to clock app."
        val undoPayload = buildJsonObject {
            put("type", "best_effort")
            put("hour", hour)
            put("minute", minute)
            put("label", label)
        }.toString()

        return intentLauncher.launchIntent(intent, summary, undoPayload)
    }

    override fun describeAction(args: ToolArguments): String {
        val hour = args.getInt("hour", default = 9)
        val minute = args.getInt("minute", default = 0)
        val label = args.getOptionalString("label") ?: "Alarm"
        return "Set alarm for ${String.format("%02d:%02d", hour, minute)} ($label)"
    }
}
