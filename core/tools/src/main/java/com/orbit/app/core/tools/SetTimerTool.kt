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
import javax.inject.Inject

class SetTimerTool @Inject constructor(
    private val intentLauncher: ToolIntentLauncher
) : Tool {

    override val spec: ToolSpec = ToolSpec(
        name = "set_timer",
        description = "Sets a countdown timer in the system clock app. durationSeconds must be in seconds (1 to 86400).",
        parametersJsonSchema = """
            {
                "type": "object",
                "properties": {
                    "durationSeconds": { "type": "integer", "minimum": 1, "maximum": 86400 },
                    "label": { "type": "string", "maxLength": 60 },
                    "skipUi": { "type": "boolean", "default": true }
                },
                "required": ["durationSeconds"]
            }
        """.trimIndent()
    )

    override val riskLevel: RiskLevel = RiskLevel.Low
    override val displayLabel: String = "Setting a timer..."

    override suspend fun execute(args: ToolArguments, ctx: ToolContext): ToolResult {
        val durationSeconds = args.getInt("durationSeconds")
        val label = args.getOptionalString("label") ?: "Orbit Timer"
        val skipUi = args.getBoolean("skipUi", default = true)

        val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
            putExtra(AlarmClock.EXTRA_LENGTH, durationSeconds)
            putExtra(AlarmClock.EXTRA_MESSAGE, label)
            putExtra(AlarmClock.EXTRA_SKIP_UI, skipUi)
        }

        val summary = "Timer for ${durationSeconds / 60} min ($label) requested in clock app."
        return intentLauncher.launchIntent(intent, summary, undoPayload = null)
    }

    override fun describeAction(args: ToolArguments): String {
        val sec = args.getInt("durationSeconds")
        val label = args.getOptionalString("label") ?: "Timer"
        return "Set timer for ${sec / 60}m ${sec % 60}s ($label)"
    }
}
