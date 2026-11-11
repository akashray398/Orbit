package com.orbit.app.core.tools

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.orbit.app.core.domain.model.RiskLevel
import com.orbit.app.core.domain.model.Tool
import com.orbit.app.core.domain.model.ToolArguments
import com.orbit.app.core.domain.model.ToolContext
import com.orbit.app.core.domain.model.ToolError
import com.orbit.app.core.domain.model.ToolResult
import com.orbit.app.core.domain.model.ToolSpec
import com.orbit.app.core.domain.repository.ReminderRepository
import com.orbit.app.core.tools.helpers.DateTimeArgParser
import com.orbit.app.core.tools.receiver.ReminderReceiver
import com.orbit.app.core.tools.worker.ReminderWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CreateReminderTool @Inject constructor(
    @ApplicationContext private val context: Context,
    private val reminderRepository: ReminderRepository
) : Tool {

    override val spec: ToolSpec = ToolSpec(
        name = "create_reminder",
        description = "Creates a scheduled reminder notification. triggerAt is an ISO-8601 date-time string in local timezone (e.g. 2025-11-12T18:00:00).",
        parametersJsonSchema = """
            {
                "type": "object",
                "properties": {
                    "title": { "type": "string", "maxLength": 120 },
                    "triggerAt": { "type": "string" },
                    "notes": { "type": "string", "maxLength": 300 },
                    "repeat": { "type": "string", "enum": ["None", "Daily", "Weekly"], "default": "None" }
                },
                "required": ["title"]
            }
        """.trimIndent()
    )

    override val riskLevel: RiskLevel = RiskLevel.Low
    override val displayLabel: String = "Creating a reminder..."

    override suspend fun execute(args: ToolArguments, ctx: ToolContext): ToolResult {
        val title = args.getOptionalString("title")?.ifBlank { null } ?: "Drink water"
        val triggerIso = args.getOptionalString("triggerAt")
        val repeatRule = args.getOptionalString("repeat") ?: "None"

        val triggerTime = if (triggerIso != null) {
            DateTimeArgParser.parseIsoToEpochMillis(triggerIso, ctx.timeZoneId)
                ?: (System.currentTimeMillis() + 1800000L) // Default 30 min in future if parse fails
        } else {
            System.currentTimeMillis() + 1800000L // Default 30 min in future if triggerAt not provided
        }

        val createRes = reminderRepository.createReminder(title, triggerTime, repeatRule)
        if (createRes !is com.orbit.app.core.domain.result.Result.Success) {
            return ToolResult.Failure(ToolError.Unknown("Failed to save reminder"), "Database error.")
        }

        val reminder = createRes.data
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else true

        if (canExact) {
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra(ReminderReceiver.EXTRA_REMINDER_ID, reminder.id)
                putExtra(ReminderReceiver.EXTRA_REMINDER_TITLE, reminder.title)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                reminder.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } else {
            val delay = (triggerTime - System.currentTimeMillis()).coerceAtLeast(0L)
            val workReq = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(workDataOf("reminder_id" to reminder.id, "reminder_title" to reminder.title))
                .build()

            WorkManager.getInstance(context).enqueue(workReq)
        }

        val undoPayload = buildJsonObject {
            put("reminderId", reminder.id)
        }.toString()

        val formattedTimeStr = DateTimeArgParser.formatEpochToIso(triggerTime, ctx.timeZoneId)
        val summary = "Reminder '$title' created for $formattedTimeStr."
        return ToolResult.Success(summary = summary, undoPayload = undoPayload)
    }

    override fun describeAction(args: ToolArguments): String {
        val title = args.getOptionalString("title") ?: "Reminder"
        val triggerAt = args.getOptionalString("triggerAt") ?: "in 30 min"
        return "Create reminder '$title' ($triggerAt)"
    }

    override suspend fun undo(payload: String): ToolResult {
        return try {
            val obj = Json.parseToJsonElement(payload).jsonObject
            val reminderId = obj["reminderId"]?.jsonPrimitive?.content ?: return ToolResult.Failure(ToolError.InvalidArguments, "Invalid undo payload")

            val intent = Intent(context, ReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, reminderId.hashCode(), intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
            if (pendingIntent != null) {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }

            reminderRepository.cancelReminder(reminderId)
            ToolResult.Success(summary = "Reminder cancelled successfully.")
        } catch (e: Exception) {
            ToolResult.Failure(ToolError.Unknown(e.message ?: "Undo failed"), "Undo error.")
        }
    }
}
