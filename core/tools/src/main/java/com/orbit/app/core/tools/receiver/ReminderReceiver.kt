package com.orbit.app.core.tools.receiver

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
        val action = intent.action

        val scope = CoroutineScope(Dispatchers.IO)

        if (action == ACTION_MARK_DONE) {
            scope.launch {
                reminderRepository.markFired(reminderId)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(reminderId.hashCode())
            return
        }

        scope.launch {
            reminderRepository.markFired(reminderId)
        }

        showNotification(context, reminderId, title)
    }

    private fun showNotification(context: Context, reminderId: String, title: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "orbit_reminders_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Orbit Reminders & Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-priority interactive scheduled reminder notifications"
                enableLights(true)
                lightColor = Color.parseColor("#FF5500")
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 150, 250)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Open App Intent
        val openAppIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            reminderId.hashCode() + 1,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Mark Done Intent
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

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("⏰ Orbit Reminder: $title")
            .setContentText("It's time! Your scheduled reminder for '$title' is happening now.")
            .setStyle(NotificationCompat.BigTextStyle().bigText("It's time! Your scheduled reminder for '$title' is happening now."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setColor(Color.parseColor("#FF5500"))
            .setContentIntent(openPendingIntent)
            .setVibrate(longArrayOf(0, 250, 150, 250))
            .setAutoCancel(true)
            .addAction(android.R.drawable.checkbox_on_background, "Mark Done", donePendingIntent)
            .addAction(android.R.drawable.ic_menu_view, "Open Orbit", openPendingIntent)
            .build()

        notificationManager.notify(reminderId.hashCode(), notification)
    }

    companion object {
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_REMINDER_TITLE = "extra_reminder_title"
        const val ACTION_MARK_DONE = "com.orbit.app.ACTION_MARK_DONE"
    }
}
