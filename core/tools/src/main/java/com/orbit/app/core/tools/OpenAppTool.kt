package com.orbit.app.core.tools

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import com.orbit.app.core.domain.model.RiskLevel
import com.orbit.app.core.domain.model.Tool
import com.orbit.app.core.domain.model.ToolArguments
import com.orbit.app.core.domain.model.ToolContext
import com.orbit.app.core.domain.model.ToolError
import com.orbit.app.core.domain.model.ToolResult
import com.orbit.app.core.domain.model.ToolSpec
import com.orbit.app.core.tools.helpers.ToolIntentLauncher
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class OpenAppTool @Inject constructor(
    @ApplicationContext private val context: Context,
    private val intentLauncher: ToolIntentLauncher
) : Tool {

    override val spec: ToolSpec = ToolSpec(
        name = "open_app",
        description = "Opens a system app target or allowed application by target name (BROWSER, CAMERA, CALENDAR, CLOCK, MAPS, PHONE_DIALER, CONTACTS, SETTINGS, MESSAGES, FILES) with an optional query.",
        parametersJsonSchema = """
            {
                "type": "object",
                "properties": {
                    "target": { "type": "string", "enum": ["BROWSER", "CAMERA", "CALENDAR", "CLOCK", "MAPS", "PHONE_DIALER", "CONTACTS", "SETTINGS", "MESSAGES", "FILES"] },
                    "appName": { "type": "string" },
                    "query": { "type": "string" }
                }
            }
        """.trimIndent()
    )

    override val riskLevel: RiskLevel = RiskLevel.Low
    override val displayLabel: String = "Opening app..."

    private val blockedPackageKeywords = listOf("bank", "upi", "wallet", "paytm", "gpay", "pass", "vault", "keepass", "bitwarden")

    override suspend fun execute(args: ToolArguments, ctx: ToolContext): ToolResult {
        val target = args.getOptionalString("target")
        val appName = args.getOptionalString("appName")
        val query = args.getOptionalString("query") ?: ""

        val rawInput = "$target $appName $query".lowercase()
        if (blockedPackageKeywords.any { rawInput.contains(it) }) {
            return ToolResult.Failure(ToolError.Blocked("Financial, payment, and password vault applications are restricted for security."), "Access restricted.")
        }

        val intent: Intent? = when (target?.uppercase()) {
            "BROWSER" -> {
                val url = if (query.startsWith("http")) query else "https://www.google.com/search?q=${Uri.encode(query)}"
                Intent(Intent.ACTION_VIEW, Uri.parse(url))
            }
            "MAPS" -> {
                val geoUri = if (query.isNotEmpty()) Uri.parse("geo:0,0?q=${Uri.encode(query)}") else Uri.parse("geo:0,0")
                Intent(Intent.ACTION_VIEW, geoUri)
            }
            "CAMERA" -> Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
            "PHONE_DIALER" -> Intent(Intent.ACTION_DIAL)
            "CALENDAR" -> Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALENDAR)
            "MESSAGES" -> Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MESSAGING)
            "FILES" -> Intent(Intent.ACTION_GET_CONTENT).apply { type = "*/*" }
            "CONTACTS" -> Intent(Intent.ACTION_VIEW, Uri.parse("content://contacts/people"))
            "SETTINGS" -> Intent(android.provider.Settings.ACTION_SETTINGS)
            else -> null
        }

        if (intent != null) {
            return intentLauncher.launchIntent(intent, "Opened $target app successfully.")
        }

        if (appName != null) {
            val pm = context.packageManager
            val launchIntent = pm.getLaunchIntentForPackage(appName)
            if (launchIntent != null) {
                return intentLauncher.launchIntent(launchIntent, "Opened app '$appName'.")
            }
        }

        return ToolResult.Failure(ToolError.NotFound, "Target app target/name not found on device.")
    }

    override fun describeAction(args: ToolArguments): String {
        val target = args.getOptionalString("target") ?: args.getOptionalString("appName") ?: "App"
        return "Open $target"
    }
}
