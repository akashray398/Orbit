package com.orbit.app.core.tools.helpers

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateTimeArgParser {

    fun parseIsoToEpochMillis(isoString: String, timeZoneId: String = "UTC"): Long? {
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd"
        )

        for (pattern in formats) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.US)
                sdf.timeZone = TimeZone.getTimeZone(timeZoneId)
                val date = sdf.parse(isoString)
                if (date != null) return date.time
            } catch (_: Exception) {}
        }
        return null
    }

    fun formatEpochToIso(epochMillis: Long, timeZoneId: String = "UTC"): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone(timeZoneId)
        return sdf.format(Date(epochMillis))
    }
}
