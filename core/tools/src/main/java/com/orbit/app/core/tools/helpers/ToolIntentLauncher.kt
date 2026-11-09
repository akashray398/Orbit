package com.orbit.app.core.tools.helpers

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import com.orbit.app.core.domain.model.ToolError
import com.orbit.app.core.domain.model.ToolResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToolIntentLauncher @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun launchIntent(intent: Intent, successSummary: String, undoPayload: String? = null): ToolResult {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            ToolResult.Success(summary = successSummary, undoPayload = undoPayload)
        } catch (e: ActivityNotFoundException) {
            ToolResult.Failure(ToolError.Unavailable, "Required app or activity not found on device.")
        } catch (e: SecurityException) {
            ToolResult.Failure(ToolError.PermissionMissing("Permission denied launching activity: ${e.message}"), "Permission denied.")
        } catch (e: Exception) {
            ToolResult.Failure(ToolError.Unknown(e.message ?: "Failed to start activity"), e.message ?: "Launch failed.")
        }
    }
}
