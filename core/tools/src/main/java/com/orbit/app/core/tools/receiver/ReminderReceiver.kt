package com.orbit.app.core.tools.receiver

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.orbit.app.core.domain.repository.ReminderRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var reminderRepository: ReminderRepository

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getStringExtra(EXTRA_REMINDER_ID) ?: return
        val title = intent.getStringExtra(EXTRA_REMINDER_TITLE) ?: "Orbit Reminder"
        val category = intent.getStringExtra(EXTRA_REMINDER_CATEGORY) ?: "GENERAL"
        val action = intent.action

        val scope = CoroutineScope(Dispatchers.IO)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        when (action) {
            ACTION_MARK_DONE -> {
                scope.launch {
                    reminderRepository.markFired(reminderId)
                }
                notificationManager.cancel(reminderId.hashCode())
                return
            }
            ACTION_SNOOZE_10M -> {
                notificationManager.cancel(reminderId.hashCode())
                snoozeAlarm(context, reminderId, title, category)
                return
            }
            else -> {
                scope.launch {
                    reminderRepository.markFired(reminderId)
                }
                showNotification(context, reminderId, title, category)
            }
        }
    }

    private fun snoozeAlarm(context: Context, reminderId: String, title: String, category: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val snoozeTime = System.currentTimeMillis() + (10 * 60 * 1000L) // 10 minutes

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_REMINDER_TITLE, title)
            putExtra(EXTRA_REMINDER_CATEGORY, category)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeTime, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, snoozeTime, pendingIntent)
            }
        } catch (_: Exception) {}
    }

    private fun showNotification(context: Context, reminderId: String, title: String, category: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "orbit_smart_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Orbit Smart Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Interactive notifications for your daily routine, study sessions, and projects"
                enableLights(true)
                lightColor = Color.parseColor("#FF5500")
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 150, 250)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Contextual Message per category
        val contextualMessage = when (category.uppercase()) {
            "HEALTH" -> "Good morning! Time for your wellness routine. Hydrate with water and stay active today."
            "STUDY" -> "Study session time! Focus on mastering your core technical concepts today."
            "PROJECT" -> "Orbit AI Project Development block is here! Build awesome hands-on Android code."
            "AI_LEARNING" -> "Your GenAI insight session is here! Explore something new and expand your knowledge."
            "REVIEW" -> "Evening reflection time! Celebrate your progress today and prepare for a restful sleep."
            else -> "It's time! Your scheduled routine for '$title' is happening now."
        }

        val openAppIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            reminderId.hashCode() + 1,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Done Action
        val doneIntent = Intent(context, ReminderReceiver::class.java).apply {
            this.action = ACTION_MARK_DONE
            putExtra(EXTRA_REMINDER_ID, reminderId)
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode(),
            doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze 10m Action
        val snoozeIntent = Intent(context, ReminderReceiver::class.java).apply {
            this.action = ACTION_SNOOZE_10M
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_REMINDER_TITLE, title)
            putExtra(EXTRA_REMINDER_CATEGORY, category)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode() + 2,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("⏰ Orbit — $title")
            .setContentText(contextualMessage)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contextualMessage))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setColor(Color.parseColor("#FF5500"))
            .setContentIntent(openPendingIntent)
            .setVibrate(longArrayOf(0, 250, 150, 250))
            .setAutoCancel(true)
            .addAction(android.R.drawable.checkbox_on_background, "Done", donePendingIntent)
            .addAction(android.R.drawable.ic_popup_sync, "Snooze 10 min", snoozePendingIntent)
            .addAction(android.R.drawable.ic_menu_view, "Open Orbit", openPendingIntent)
            .build()

        notificationManager.notify(reminderId.hashCode(), notification)
    }

    companion object {
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_REMINDER_TITLE = "extra_reminder_title"
        const val EXTRA_REMINDER_CATEGORY = "extra_reminder_category"
        const val ACTION_MARK_DONE = "com.orbit.app.ACTION_MARK_DONE"
        const val ACTION_SNOOZE_10M = "com.orbit.app.ACTION_SNOOZE_10M"
    }
}
