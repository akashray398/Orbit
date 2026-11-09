package com.orbit.app.core.tools

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Environment
import android.os.PowerManager
import android.os.StatFs
import com.orbit.app.core.domain.model.RiskLevel
import com.orbit.app.core.domain.model.Tool
import com.orbit.app.core.domain.model.ToolArguments
import com.orbit.app.core.domain.model.ToolContext
import com.orbit.app.core.domain.model.ToolResult
import com.orbit.app.core.domain.model.ToolSpec
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

class GetDeviceStatusTool @Inject constructor(
    @ApplicationContext private val context: Context
) : Tool {

    override val spec: ToolSpec = ToolSpec(
        name = "get_device_status",
        description = "Returns current device battery level, charging state, power saver state, and free storage.",
        parametersJsonSchema = "{\"type\":\"object\",\"properties\":{}}"
    )

    override val riskLevel: RiskLevel = RiskLevel.Low
    override val displayLabel: String = "Checking device status..."
    override val isReadOnly: Boolean = true

    override suspend fun execute(args: ToolArguments, ctx: ToolContext): ToolResult {
        val batteryStatus: Intent? = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level != -1 && scale != -1) (level * 100 / scale.toFloat()).toInt() else -1

        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isPowerSave = powerManager.isPowerSaveMode

        val stat = StatFs(Environment.getDataDirectory().path)
        val freeBytes = stat.availableBlocksLong * stat.blockSizeLong
        val totalBytes = stat.blockCountLong * stat.blockSizeLong

        val freeGb = String.format("%.1f GB", freeBytes / (1024f * 1024f * 1024f))
        val totalGb = String.format("%.1f GB", totalBytes / (1024f * 1024f * 1024f))

        val summary = "Battery: $batteryPct% (${if (isCharging) "Charging" else "Discharging"}), Power Saver: ${if (isPowerSave) "ON" else "OFF"}, Storage: $freeGb free of $totalGb."

        val data = buildJsonObject {
            put("batteryPercent", batteryPct)
            put("isCharging", isCharging)
            put("isPowerSave", isPowerSave)
            put("freeStorageGb", freeGb)
            put("totalStorageGb", totalGb)
        }

        return ToolResult.Success(summary = summary, data = data)
    }

    override fun describeAction(args: ToolArguments): String = "Check device status and battery"
}
