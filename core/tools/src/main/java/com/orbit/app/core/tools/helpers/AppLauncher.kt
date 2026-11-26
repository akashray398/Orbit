package com.orbit.app.core.tools.helpers

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import com.orbit.app.core.domain.model.ToolError
import com.orbit.app.core.domain.model.ToolResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLauncher @Inject constructor(
    @ApplicationContext private val context: Context,
    private val intentLauncher: ToolIntentLauncher
) {
    fun launchTarget(target: String, appName: String? = null, query: String = ""): ToolResult {
        val pm = context.packageManager
        val normTarget = target.uppercase().trim()

        val intent: Intent? = when (normTarget) {
            "CALENDAR" -> resolveCalendarIntent(pm)
            "CONTACTS" -> resolveContactsIntent(pm)
            "CAMERA" -> resolveCameraIntent(pm)
            "YOUTUBE" -> resolvePackageOrUri(pm, "com.google.android.youtube", "https://www.youtube.com/results?search_query=${Uri.encode(query)}")
            "SPOTIFY" -> resolvePackageOrUri(pm, "com.spotify.music", "https://open.spotify.com")
            "SETTINGS" -> Intent(Settings.ACTION_SETTINGS)
            "BLUETOOTH" -> Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            "WIFI" -> Intent(Settings.ACTION_WIFI_SETTINGS)
            "CALCULATOR" -> resolveCalculatorIntent(pm)
            "CHROME", "BROWSER" -> resolveBrowserIntent(pm, query)
            "CLOCK" -> resolveClockIntent(pm)
            "PHONE", "PHONE_DIALER" -> resolvePhoneIntent(pm, query)
            "MESSAGES" -> resolveMessagesIntent(pm)
            "FILES" -> Intent(Intent.ACTION_GET_CONTENT).apply { type = "*/*" }
            "MAPS" -> resolveMapsIntent(pm, query)
            "GALLERY", "PHOTOS" -> resolveGalleryIntent(pm)
            else -> null
        }

        if (intent != null) {
            return intentLauncher.launchIntent(intent, "Dispatched launch request for '$normTarget'.")
        }

        if (!appName.isNullOrBlank()) {
            val launchIntent = resolveCustomAppName(pm, appName)
            if (launchIntent != null) {
                return intentLauncher.launchIntent(launchIntent, "Launched application '$appName'.")
            }
        }

        return ToolResult.Failure(ToolError.NotFound, "Application '$normTarget' is not installed or available on this device.")
    }

    private fun resolveCalendarIntent(pm: PackageManager): Intent {
        val knownPackages = listOf(
            "com.google.android.calendar",
            "com.samsung.android.calendar",
            "com.miui.calendar",
            "com.coloros.calendar",
            "com.bbk.calendar"
        )
        for (pkg in knownPackages) {
            val launch = pm.getLaunchIntentForPackage(pkg)
            if (launch != null) return launch
        }
        return Intent(Intent.ACTION_VIEW).setData(Uri.parse("content://com.android.calendar/time"))
    }

    private fun resolveContactsIntent(pm: PackageManager): Intent {
        val launch = pm.getLaunchIntentForPackage("com.google.android.contacts")
            ?: pm.getLaunchIntentForPackage("com.samsung.android.app.contacts")
        return launch ?: Intent(Intent.ACTION_VIEW, Uri.parse("content://contacts/people"))
    }

    private fun resolveCameraIntent(pm: PackageManager): Intent {
        return Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
    }

    private fun resolveCalculatorIntent(pm: PackageManager): Intent {
        val known = listOf(
            "com.google.android.calculator",
            "com.sec.android.app.popupcalculator",
            "com.miui.calculator",
            "com.coloros.calculator"
        )
        for (pkg in known) {
            val launch = pm.getLaunchIntentForPackage(pkg)
            if (launch != null) return launch
        }
        return Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALCULATOR)
    }

    private fun resolveClockIntent(pm: PackageManager): Intent {
        val known = listOf(
            "com.google.android.deskclock",
            "com.sec.android.app.clockpackage",
            "com.miui.deskclock"
        )
        for (pkg in known) {
            val launch = pm.getLaunchIntentForPackage(pkg)
            if (launch != null) return launch
        }
        return Intent(AlarmClock.ACTION_SHOW_ALARMS)
    }

    private fun resolvePhoneIntent(pm: PackageManager, query: String): Intent {
        val clean = query.filter { it.isDigit() || it == '+' }
        return if (clean.isNotEmpty()) {
            Intent(Intent.ACTION_DIAL, Uri.parse("tel:$clean"))
        } else {
            Intent(Intent.ACTION_DIAL)
        }
    }

    private fun resolveMessagesIntent(pm: PackageManager): Intent {
        val launch = pm.getLaunchIntentForPackage("com.google.android.apps.messaging")
            ?: pm.getLaunchIntentForPackage("com.samsung.android.messaging")
        return launch ?: Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_MESSAGING)
    }

    private fun resolveBrowserIntent(pm: PackageManager, query: String): Intent {
        val url = if (query.startsWith("http")) query else if (query.isNotEmpty()) "https://www.google.com/search?q=${Uri.encode(query)}" else "https://www.google.com"
        val launch = pm.getLaunchIntentForPackage("com.android.chrome")
        return launch?.apply { data = Uri.parse(url) } ?: Intent(Intent.ACTION_VIEW, Uri.parse(url))
    }

    private fun resolveMapsIntent(pm: PackageManager, query: String): Intent {
        val uri = if (query.isNotEmpty()) Uri.parse("geo:0,0?q=${Uri.encode(query)}") else Uri.parse("geo:0,0")
        val launch = pm.getLaunchIntentForPackage("com.google.android.apps.maps")
        return launch?.apply { data = uri } ?: Intent(Intent.ACTION_VIEW, uri)
    }

    private fun resolveGalleryIntent(pm: PackageManager): Intent {
        val known = listOf(
            "com.google.android.apps.photos",
            "com.sec.android.gallery3d",
            "com.miui.gallery"
        )
        for (pkg in known) {
            val launch = pm.getLaunchIntentForPackage(pkg)
            if (launch != null) return launch
        }
        return Intent(Intent.ACTION_VIEW, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    }

    private fun resolvePackageOrUri(pm: PackageManager, pkg: String, fallbackUrl: String): Intent {
        val launch = pm.getLaunchIntentForPackage(pkg)
        return launch ?: Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl))
    }

    private fun resolveCustomAppName(pm: PackageManager, appName: String): Intent? {
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val queryLower = appName.lowercase()
        for (app in installedApps) {
            val label = pm.getApplicationLabel(app).toString().lowercase()
            if (label == queryLower || label.contains(queryLower)) {
                return pm.getLaunchIntentForPackage(app.packageName)
            }
        }
        return null
    }
}
