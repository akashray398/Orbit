package com.orbit.app.core.tools.helpers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.orbit.app.core.data.planner.DailyPlanReport
import com.orbit.app.core.domain.repository.ReminderRepository
import com.orbit.app.core.tools.receiver.ReminderReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val reminderRepository: ReminderRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun scheduleDailyPlanReminders(report: DailyPlanReport) {
        scope.launch {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())

            report.items.forEach { item ->
                if (!item.isCompleted) {
                    val triggerTime = parseTimeStringToEpoch(item.timeStr) ?: (System.currentTimeMillis() + 1800000L)
                    if (triggerTime > System.currentTimeMillis()) {
                        val createRes = reminderRepository.createReminder(item.title, triggerTime, "None")
                        if (createRes is com.orbit.app.core.domain.result.Result.Success) {
                            val reminder = createRes.data
                            scheduleAlarm(alarmManager, reminder.id, reminder.title, item.category, triggerTime)
                        }
                    }
                }
            }
        }
    }

    fun snoozeReminder(reminderId: String, title: String, category: String = "GENERAL") {
        scope.launch {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val snoozeTime = System.currentTimeMillis() + (10 * 60 * 1000L) // 10 minutes
            scheduleAlarm(alarmManager, reminderId, title, category, snoozeTime)
        }
    }

    private fun scheduleAlarm(
        alarmManager: AlarmManager,
        reminderId: String,
        title: String,
        category: String,
        triggerTime: Long
    ) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_REMINDER_ID, reminderId)
            putExtra(ReminderReceiver.EXTRA_REMINDER_TITLE, title)
            putExtra(ReminderReceiver.EXTRA_REMINDER_CATEGORY, category)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        } catch (_: Exception) {}
    }

    private fun parseTimeStringToEpoch(timeStr: String): Long? {
        return try {
            val cal = Calendar.getInstance()
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val date = sdf.parse(timeStr) ?: return null

            val timeCal = Calendar.getInstance().apply { time = date }
            cal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
            cal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
            cal.set(Calendar.SECOND, 0)
            cal.timeInMillis
        } catch (_: Exception) {
            null
        }
    }
}
