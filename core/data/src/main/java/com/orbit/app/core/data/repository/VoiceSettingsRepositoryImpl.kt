package com.orbit.app.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.orbit.app.core.domain.model.VoiceLocale
import com.orbit.app.core.domain.repository.VoiceSettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.voiceDataStore: DataStore<Preferences> by preferencesDataStore(name = "orbit_voice_prefs")

@Singleton
class VoiceSettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : VoiceSettingsRepository {

    private object Keys {
        val VOICE_LOCALE = stringPreferencesKey("voice_locale")
        val SPOKEN_REPLIES = booleanPreferencesKey("spoken_replies_enabled")
        val AUTO_SEND_VOICE = booleanPreferencesKey("auto_send_voice")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
    }

    override val voiceLocale: Flow<VoiceLocale> = context.voiceDataStore.data.map { prefs ->
        val tag = prefs[Keys.VOICE_LOCALE] ?: VoiceLocale.ENGLISH.bcp47Tag
        VoiceLocale.fromBcp47(tag)
    }

    override val spokenRepliesEnabled: Flow<Boolean> = context.voiceDataStore.data.map { prefs ->
        prefs[Keys.SPOKEN_REPLIES] ?: false
    }

    override val autoSendVoice: Flow<Boolean> = context.voiceDataStore.data.map { prefs ->
        prefs[Keys.AUTO_SEND_VOICE] ?: true
    }

    override val keepScreenOn: Flow<Boolean> = context.voiceDataStore.data.map { prefs ->
        prefs[Keys.KEEP_SCREEN_ON] ?: true
    }

    override suspend fun setVoiceLocale(locale: VoiceLocale) {
        context.voiceDataStore.edit { prefs ->
            prefs[Keys.VOICE_LOCALE] = locale.bcp47Tag
        }
    }

    override suspend fun setSpokenRepliesEnabled(enabled: Boolean) {
        context.voiceDataStore.edit { prefs ->
            prefs[Keys.SPOKEN_REPLIES] = enabled
        }
    }

    override suspend fun setAutoSendVoice(enabled: Boolean) {
        context.voiceDataStore.edit { prefs ->
            prefs[Keys.AUTO_SEND_VOICE] = enabled
        }
    }

    override suspend fun setKeepScreenOn(enabled: Boolean) {
        context.voiceDataStore.edit { prefs ->
            prefs[Keys.KEEP_SCREEN_ON] = enabled
        }
    }
}
