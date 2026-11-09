package com.orbit.app.core.tools.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.orbit.app.core.data.database.daos.NotificationDao
import com.orbit.app.core.data.database.entities.NotificationEntity
import com.orbit.app.core.domain.model.Redactor
import com.orbit.app.core.domain.repository.AgentSettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OrbitNotificationListener : NotificationListenerService() {

    @Inject
    lateinit var notificationDao: NotificationDao

    @Inject
    lateinit var agentSettingsRepository: AgentSettingsRepository

    private val scope = CoroutineScope(Dispatchers.IO)
    private val sensitivePackageKeywords = listOf("bank", "upi", "wallet", "paytm", "gpay", "pass", "vault", "auth")

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return

        val packageName = sbn.packageName ?: return
        if (packageName == applicationContext.packageName) return
        if (sbn.isOngoing) return

        if (sensitivePackageKeywords.any { packageName.lowercase().contains(it) }) return

        scope.launch {
            val settings = agentSettingsRepository.agentSettings.first()
            val enabled = settings.confirmMediumRisk
            if (!enabled) return@launch

            val extras = sbn.notification?.extras ?: return@launch
            val rawTitle = extras.getCharSequence("android.title")?.toString() ?: ""
            val rawText = extras.getCharSequence("android.text")?.toString() ?: ""

            if (rawTitle.isEmpty() && rawText.isEmpty()) return@launch

            val cleanTitle = Redactor.redact(rawTitle).take(80)
            val cleanText = Redactor.redact(rawText).take(200)

            val entity = NotificationEntity(
                id = "${packageName}_${sbn.id}_${sbn.postTime}",
                packageName = packageName,
                appLabel = packageName.substringAfterLast('.'),
                title = cleanTitle,
                text = cleanText,
                postedAt = sbn.postTime
            )

            notificationDao.insertNotification(entity)
        }
    }
}
