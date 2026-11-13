package com.orbit.app.core.tools.service

import android.content.ComponentName
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.orbit.app.core.data.database.daos.NotificationDao
import com.orbit.app.core.data.database.entities.NotificationEntity
import com.orbit.app.core.domain.model.Redactor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OrbitNotificationListener : NotificationListenerService() {

    @Inject
    lateinit var notificationDao: NotificationDao

    private val scope = CoroutineScope(Dispatchers.IO)
    private val sensitivePackageKeywords = listOf("bank", "upi", "wallet", "paytm", "gpay", "pass", "vault", "auth")

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        try {
            requestRebind(ComponentName(this, OrbitNotificationListener::class.java))
        } catch (_: Exception) {}
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return

        val packageName = sbn.packageName ?: return
        if (packageName == applicationContext.packageName) return

        if (sensitivePackageKeywords.any { packageName.lowercase().contains(it) }) return

        scope.launch {
            val extras = sbn.notification?.extras ?: return@launch
            val rawTitle = extras.getCharSequence("android.title")?.toString()
                ?: extras.getCharSequence("android.titleBig")?.toString()
                ?: extras.getCharSequence("android.conversationTitle")?.toString()
                ?: ""

            val rawText = extras.getCharSequence("android.text")?.toString()
                ?: extras.getCharSequence("android.bigText")?.toString()
                ?: extras.getCharSequence("android.subText")?.toString()
                ?: (extras.getCharSequenceArray("android.textLines")?.joinToString("; ") ?: "")

            if (rawTitle.isEmpty() && rawText.isEmpty()) return@launch

            val cleanTitle = Redactor.redact(rawTitle).take(100)
            val cleanText = Redactor.redact(rawText).take(300)
            val appLabelStr = packageName.substringAfterLast('.').replaceFirstChar { it.uppercase() }

            val entity = NotificationEntity(
                id = "${packageName}_${sbn.id}_${sbn.postTime}",
                packageName = packageName,
                appLabel = appLabelStr,
                title = cleanTitle,
                text = cleanText,
                postedAt = sbn.postTime
            )

            notificationDao.insertNotification(entity)
        }
    }
}
