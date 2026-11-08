package com.orbit.app.feature.onboarding.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

enum class PermissionStatus {
    NotAsked,
    Granted,
    Denied,
    Blocked
}

object PermissionRequester {

    const val KEY_NOTIFICATIONS = "notifications"
    const val KEY_MICROPHONE = "microphone"
    const val KEY_CALENDAR = "calendar"
    const val KEY_NOTIFICATION_ACCESS = "notification_access"

    fun getRuntimePermissions(key: String): Array<String> {
        return when (key) {
            KEY_NOTIFICATIONS -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    emptyArray()
                }
            }
            KEY_MICROPHONE -> arrayOf(Manifest.permission.RECORD_AUDIO)
            KEY_CALENDAR -> arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)
            else -> emptyArray()
        }
    }

    fun getPermissionStatus(
        context: Context,
        key: String,
        askedBefore: Boolean
    ): PermissionStatus {
        if (key == KEY_NOTIFICATION_ACCESS) {
            val enabledPackages = NotificationManagerCompat.getEnabledListenerPackages(context)
            val isGranted = enabledPackages.contains(context.packageName)
            return if (isGranted) PermissionStatus.Granted else if (askedBefore) PermissionStatus.Denied else PermissionStatus.NotAsked
        }

        val permissions = getRuntimePermissions(key)
        if (permissions.isEmpty()) {
            return PermissionStatus.Granted // e.g. Notifications pre-Android 13
        }

        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) return PermissionStatus.Granted

        val activity = context as? Activity
        if (activity != null) {
            val anyShouldShowRationale = permissions.any {
                activity.shouldShowRequestPermissionRationale(it)
            }

            return when {
                !askedBefore -> PermissionStatus.NotAsked
                anyShouldShowRationale -> PermissionStatus.Denied
                else -> PermissionStatus.Blocked // Permanently denied ("Don't ask again")
            }
        }

        return if (askedBefore) PermissionStatus.Denied else PermissionStatus.NotAsked
    }

    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun openNotificationAccessSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            openAppSettings(context)
        }
    }
}
