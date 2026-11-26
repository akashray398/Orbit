package com.orbit.app.core.tools

import com.orbit.app.core.domain.model.RiskLevel
import com.orbit.app.core.domain.model.Tool
import com.orbit.app.core.domain.model.ToolArguments
import com.orbit.app.core.domain.model.ToolContext
import com.orbit.app.core.domain.model.ToolError
import com.orbit.app.core.domain.model.ToolResult
import com.orbit.app.core.domain.model.ToolSpec
import com.orbit.app.core.tools.helpers.AppLauncher
import javax.inject.Inject

class OpenAppTool @Inject constructor(
    private val appLauncher: AppLauncher
) : Tool {

    override val spec: ToolSpec = ToolSpec(
        name = "open_app",
        description = "Launches an installed application or system app target (CALENDAR, CONTACTS, CAMERA, YOUTUBE, SPOTIFY, SETTINGS, CALCULATOR, CHROME, BROWSER, MESSAGES, PHONE_DIALER, FILES, MAPS, GALLERY, CLOCK, BLUETOOTH, WIFI) by target or app name.",
        parametersJsonSchema = """
            {
                "type": "object",
                "properties": {
                    "target": { "type": "string", "enum": ["CALENDAR", "CONTACTS", "CAMERA", "YOUTUBE", "SPOTIFY", "SETTINGS", "CALCULATOR", "CHROME", "BROWSER", "MESSAGES", "PHONE_DIALER", "FILES", "MAPS", "GALLERY", "CLOCK", "BLUETOOTH", "WIFI"] },
                    "appName": { "type": "string" },
                    "query": { "type": "string" }
                }
            }
        """.trimIndent()
    )

    override val riskLevel: RiskLevel = RiskLevel.Low
    override val displayLabel: String = "Launching app..."

    private val blockedPackageKeywords = listOf("bank", "upi", "wallet", "paytm", "gpay", "pass", "vault", "keepass", "bitwarden")

    override suspend fun execute(args: ToolArguments, ctx: ToolContext): ToolResult {
        val target = args.getOptionalString("target") ?: args.getOptionalString("appName") ?: "CALENDAR"
        val appName = args.getOptionalString("appName")
        val query = args.getOptionalString("query") ?: ""

        val rawInput = "$target $appName $query".lowercase()
        if (blockedPackageKeywords.any { rawInput.contains(it) }) {
            return ToolResult.Failure(ToolError.Blocked("Financial, payment, and password vault applications are restricted for security."), "Access restricted.")
        }

        return appLauncher.launchTarget(target = target, appName = appName, query = query)
    }

    override fun describeAction(args: ToolArguments): String {
        val target = args.getOptionalString("target") ?: args.getOptionalString("appName") ?: "App"
        return "Open $target"
    }
}
