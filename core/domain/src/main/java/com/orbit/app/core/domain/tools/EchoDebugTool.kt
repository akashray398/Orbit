package com.orbit.app.core.domain.tools

import com.orbit.app.core.domain.model.RiskLevel
import com.orbit.app.core.domain.model.Tool
import com.orbit.app.core.domain.model.ToolArguments
import com.orbit.app.core.domain.model.ToolContext
import com.orbit.app.core.domain.model.ToolResult
import com.orbit.app.core.domain.model.ToolSpec
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

class EchoDebugTool @Inject constructor() : Tool {

    override val spec: ToolSpec = ToolSpec(
        name = "echo_debug",
        description = "Echoes back debug input for multi-step agent testing.",
        parametersJsonSchema = "{\"type\":\"object\",\"properties\":{\"message\":{\"type\":\"string\"}},\"required\":[\"message\"]}"
    )

    override val riskLevel: RiskLevel = RiskLevel.Low
    override val displayLabel: String = "Echoing debug message..."
    override val isReadOnly: Boolean = true

    override suspend fun execute(args: ToolArguments, ctx: ToolContext): ToolResult {
        val msg = try {
            args.getString("message")
        } catch (e: Exception) {
            "No message provided"
        }

        val data = buildJsonObject {
            put("echo", msg)
        }

        return ToolResult.Success(
            summary = "Echoed: $msg",
            data = data
        )
    }

    override fun describeAction(args: ToolArguments): String = "Echo debug input"
}
