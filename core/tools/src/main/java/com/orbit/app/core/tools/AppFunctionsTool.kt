package com.orbit.app.core.tools

import android.app.SearchManager
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import com.orbit.app.core.domain.model.RiskLevel
import com.orbit.app.core.domain.model.Tool
import com.orbit.app.core.domain.model.ToolArguments
import com.orbit.app.core.domain.model.ToolContext
import com.orbit.app.core.domain.model.ToolResult
import com.orbit.app.core.domain.model.ToolSpec
import com.orbit.app.core.tools.helpers.ToolIntentLauncher
import javax.inject.Inject

class AppFunctionsTool @Inject constructor(
    private val intentLauncher: ToolIntentLauncher
) : Tool {

    override val spec: ToolSpec = ToolSpec(
        name = "execute_app_function",
        description = "Executes direct in-app workflows like Maps location search, camera photo capture, web search, or system settings.",
        parametersJsonSchema = """
            {
                "type": "object",
                "properties": {
                    "actionType": { "type": "string", "enum": ["MAPS_SEARCH", "CAMERA", "WEB_SEARCH", "SETTINGS"], "default": "MAPS_SEARCH" },
                    "query": { "type": "string" }
                }
            }
        """.trimIndent()
    )

    override val riskLevel: RiskLevel = RiskLevel.Low
    override val displayLabel: String = "Executing in-app workflow..."

    override suspend fun execute(args: ToolArguments, ctx: ToolContext): ToolResult {
        val actionType = args.getOptionalString("actionType") ?: "MAPS_SEARCH"
        val query = args.getOptionalString("query") ?: "petrol pumps near me"

        return when (actionType.uppercase()) {
            "CAMERA" -> {
                val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
                intentLauncher.launchIntent(intent, "Opening Camera to capture photo...")
            }
            "WEB_SEARCH" -> {
                val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                    putExtra(SearchManager.QUERY, query)
                }
                intentLauncher.launchIntent(intent, "Searching web for '$query'...")
            }
            "SETTINGS" -> {
                val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                intentLauncher.launchIntent(intent, "Opening System Settings...")
            }
            else -> { // MAPS_SEARCH
                val encodedQuery = Uri.encode(query)
                val uri = Uri.parse("geo:0,0?q=$encodedQuery")
                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                    setPackage("com.google.android.apps.maps")
                }
                intentLauncher.launchIntent(intent, "Opening Maps and searching for '$query'...")
            }
        }
    }

    override fun describeAction(args: ToolArguments): String {
        val query = args.getOptionalString("query") ?: "in-app action"
        return "Execute App Function '$query'"
    }
}
