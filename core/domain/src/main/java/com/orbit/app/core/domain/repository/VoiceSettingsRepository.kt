package com.orbit.app.core.domain.repository

import com.orbit.app.core.domain.model.VoiceLocale
import kotlinx.coroutines.flow.Flow

interface VoiceSettingsRepository {
    val voiceLocale: Flow<VoiceLocale>
    val spokenRepliesEnabled: Flow<Boolean>
    val autoSendVoice: Flow<Boolean>
    val keepScreenOn: Flow<Boolean>
    suspend fun setVoiceLocale(locale: VoiceLocale)
    suspend fun setSpokenRepliesEnabled(enabled: Boolean)
    suspend fun setAutoSendVoice(enabled: Boolean)
    suspend fun setKeepScreenOn(enabled: Boolean)
}
