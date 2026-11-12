package com.orbit.app.core.tools

import android.content.Intent
import android.net.Uri
import com.orbit.app.core.domain.model.RiskLevel
import com.orbit.app.core.domain.model.Tool
import com.orbit.app.core.domain.model.ToolArguments
import com.orbit.app.core.domain.model.ToolContext
import com.orbit.app.core.domain.model.ToolError
import com.orbit.app.core.domain.model.ToolResult
import com.orbit.app.core.domain.model.ToolSpec
import com.orbit.app.core.tools.helpers.ToolIntentLauncher
import javax.inject.Inject

class MakeCallTool @Inject constructor(
    private val intentLauncher: ToolIntentLauncher
) : Tool {

    override val spec: ToolSpec = ToolSpec(
        name = "make_call",
        description = "Opens dialer or initiates a phone call to a contact or phone number.",
        parametersJsonSchema = """
            {
                "type": "object",
                "properties": {
                    "phoneNumber": { "type": "string" },
                    "name": { "type": "string" }
                }
            }
        """.trimIndent()
    )

    override val riskLevel: RiskLevel = RiskLevel.Low
    override val displayLabel: String = "Making phone call..."

    override suspend fun execute(args: ToolArguments, ctx: ToolContext): ToolResult {
        val phoneNumber = args.getOptionalString("phoneNumber") ?: ""
        val name = args.getOptionalString("name") ?: "Contact"

        val cleanNumber = phoneNumber.filter { it.isDigit() || it == '+' }
        val uriStr = if (cleanNumber.isNotEmpty()) "tel:$cleanNumber" else "tel:"

        val intent = Intent(Intent.ACTION_DIAL, Uri.parse(uriStr))
        val summary = if (cleanNumber.isNotEmpty()) "Opening dialer for $cleanNumber ($name)..." else "Opening phone dialer..."

        return intentLauncher.launchIntent(intent, summary, undoPayload = null)
    }

    override fun describeAction(args: ToolArguments): String {
        val name = args.getOptionalString("name") ?: args.getOptionalString("phoneNumber") ?: "Contact"
        return "Call $name"
    }
}
