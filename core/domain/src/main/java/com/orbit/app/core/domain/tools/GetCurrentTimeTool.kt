package com.orbit.app.core.domain.tools

import com.orbit.app.core.domain.model.RiskLevel
import com.orbit.app.core.domain.model.Tool
import com.orbit.app.core.domain.model.ToolArguments
import com.orbit.app.core.domain.model.ToolContext
import com.orbit.app.core.domain.model.ToolResult
import com.orbit.app.core.domain.model.ToolSpec
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class GetCurrentTimeTool @Inject constructor() : Tool {

    override val spec: ToolSpec = ToolSpec(
        name = "get_current_time",
        description = "Returns local date, time, day of the week, and time zone.",
        parametersJsonSchema = "{\"type\":\"object\",\"properties\":{}}"
    )

    override val riskLevel: RiskLevel = RiskLevel.Low
    override val displayLabel: String = "Checking time..."
    override val isReadOnly: Boolean = true

    override suspend fun execute(args: ToolArguments, ctx: ToolContext): ToolResult {
        val sdf = SimpleDateFormat("EEEE, MMMM d, yyyy 'at' hh:mm:ss a z", Locale.ENGLISH)
        val formatted = sdf.format(Date(ctx.currentTimeMillis))

        val data = buildJsonObject {
            put("formattedTime", formatted)
            put("timestamp", ctx.currentTimeMillis)
            put("timeZone", ctx.timeZoneId)
        }

        return ToolResult.Success(
            summary = "Current time: $formatted",
            data = data
        )
    }

    override fun describeAction(args: ToolArguments): String = "Check current time and date"
}
