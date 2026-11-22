package com.orbit.app.core.tools

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import com.orbit.app.core.domain.model.RiskLevel
import com.orbit.app.core.domain.model.Tool
import com.orbit.app.core.domain.model.ToolArguments
import com.orbit.app.core.domain.model.ToolContext
import com.orbit.app.core.domain.model.ToolError
import com.orbit.app.core.domain.model.ToolResult
import com.orbit.app.core.domain.model.ToolSpec
import com.orbit.app.core.tools.helpers.DateTimeArgParser
import com.orbit.app.core.tools.helpers.PermissionChecker
import com.orbit.app.core.tools.helpers.ToolIntentLauncher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.util.Calendar
import java.util.TimeZone
import javax.inject.Inject

class AddCalendarEventTool @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissionChecker: PermissionChecker,
    private val intentLauncher: ToolIntentLauncher
) : Tool {

    override val spec: ToolSpec = ToolSpec(
        name = "add_calendar_event",
        description = "Adds an event to the user's calendar. startAt and endAt are ISO-8601 strings in local timezone.",
        parametersJsonSchema = """
            {
                "type": "object",
                "properties": {
                    "title": { "type": "string", "maxLength": 120 },
                    "startAt": { "type": "string" },
                    "endAt": { "type": "string" },
                    "allDay": { "type": "boolean", "default": false },
                    "location": { "type": "string", "maxLength": 120 },
                    "description": { "type": "string", "maxLength": 500 },
                    "reminderMinutesBefore": { "type": "integer", "default": 10 }
                },
                "required": ["title"]
            }
        """.trimIndent()
    )

    override val riskLevel: RiskLevel = RiskLevel.Medium
    override val displayLabel: String = "Adding to calendar..."
    override val returnsUntrustedContent: Boolean = true

    override suspend fun execute(args: ToolArguments, ctx: ToolContext): ToolResult {
        val title = args.getOptionalString("title")?.ifBlank { null } ?: "Calendar Event"
        val startIso = args.getOptionalString("startAt")
        val endIso = args.getOptionalString("endAt")
        val allDay = args.getBoolean("allDay", false)
        val location = args.getOptionalString("location") ?: ""
        val description = args.getOptionalString("description") ?: ""
        val reminderMinutes = args.getInt("reminderMinutesBefore", default = 10)

        val startTime = if (startIso != null) {
            DateTimeArgParser.parseIsoToEpochMillis(startIso, ctx.timeZoneId)
                ?: (System.currentTimeMillis() + 3600000L)
        } else {
            val cal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 10)
                set(Calendar.MINUTE, 0)
            }
            cal.timeInMillis
        }

        val endTime = if (endIso != null) {
            DateTimeArgParser.parseIsoToEpochMillis(endIso, ctx.timeZoneId) ?: (startTime + 3600000L)
        } else {
            startTime + 3600000L
        }

        val hasCalendarPerms = permissionChecker.isPermissionGranted(android.Manifest.permission.READ_CALENDAR) &&
                permissionChecker.isPermissionGranted(android.Manifest.permission.WRITE_CALENDAR)

        if (!hasCalendarPerms) {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, title)
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime)
                putExtra(CalendarContract.Events.ALL_DAY, allDay)
                putExtra(CalendarContract.Events.EVENT_LOCATION, location)
                putExtra(CalendarContract.Events.DESCRIPTION, description)
            }
            return intentLauncher.launchIntent(intent, "Opened calendar event '$title' for user confirmation.")
        }

        return try {
            val calendarId = getPrimaryCalendarId(context) ?: 1L

            val values = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, startTime)
                put(CalendarContract.Events.DTEND, endTime)
                put(CalendarContract.Events.TITLE, title)
                put(CalendarContract.Events.DESCRIPTION, description)
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                put(CalendarContract.Events.ALL_DAY, if (allDay) 1 else 0)
                put(CalendarContract.Events.EVENT_LOCATION, location)
            }

            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            val eventId = uri?.lastPathSegment?.toLongOrNull()
                ?: return ToolResult.Failure(ToolError.Unknown("Failed to insert calendar row"), "Database insert failed")

            if (reminderMinutes > 0) {
                val remValues = ContentValues().apply {
                    put(CalendarContract.Reminders.EVENT_ID, eventId)
                    put(CalendarContract.Reminders.MINUTES, reminderMinutes)
                    put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
                }
                context.contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, remValues)
            }

            val undoPayload = buildJsonObject {
                put("eventId", eventId)
                put("calendarId", calendarId)
            }.toString()

            val formattedStartStr = DateTimeArgParser.formatEpochToIso(startTime, ctx.timeZoneId)
            ToolResult.Success(summary = "Event '$title' added to calendar for $formattedStartStr.", undoPayload = undoPayload)
        } catch (e: Exception) {
            ToolResult.Failure(ToolError.Unknown(e.message ?: "Calendar write failed"), "Error: ${e.message}")
        }
    }

    override fun describeAction(args: ToolArguments): String {
        val title = args.getOptionalString("title") ?: "Calendar Event"
        val startAt = args.getOptionalString("startAt") ?: "Tomorrow at 10 AM"
        return "Add event '$title' on $startAt"
    }

    override suspend fun undo(payload: String): ToolResult {
        return try {
            val obj = Json.parseToJsonElement(payload).jsonObject
            val eventId = obj["eventId"]?.jsonPrimitive?.content?.toLongOrNull()
                ?: return ToolResult.Failure(ToolError.InvalidArguments, "Invalid undo payload")

            val deleteUri = CalendarContract.Events.CONTENT_URI.buildUpon().appendPath(eventId.toString()).build()
            val deletedRows = context.contentResolver.delete(deleteUri, null, null)

            if (deletedRows > 0) {
                ToolResult.Success(summary = "Calendar event removed successfully.")
            } else {
                ToolResult.Failure(ToolError.NotFound, "Calendar event not found for deletion.")
            }
        } catch (e: Exception) {
            ToolResult.Failure(ToolError.Unknown(e.message ?: "Calendar undo failed"), "Error: ${e.message}")
        }
    }

    private fun getPrimaryCalendarId(context: Context): Long? {
        val projection = arrayOf(CalendarContract.Calendars._ID, CalendarContract.Calendars.IS_PRIMARY)
        context.contentResolver.query(CalendarContract.Calendars.CONTENT_URI, projection, null, null, null)?.use { cursor ->
            val idIdx = cursor.getColumnIndex(CalendarContract.Calendars._ID)
            val primaryIdx = cursor.getColumnIndex(CalendarContract.Calendars.IS_PRIMARY)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIdx)
                val isPrimary = if (primaryIdx != -1) cursor.getInt(primaryIdx) == 1 else false
                if (isPrimary) return id
            }
        }
        return 1L
    }
}
