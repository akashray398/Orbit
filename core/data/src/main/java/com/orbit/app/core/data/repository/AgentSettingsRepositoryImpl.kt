package com.orbit.app.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.orbit.app.core.domain.model.AgentSettings
import com.orbit.app.core.domain.repository.AgentSettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.agentSettingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "orbit_agent_settings_prefs")

@Singleton
class AgentSettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AgentSettingsRepository {

    private object Keys {
        val MAX_STEPS = intPreferencesKey("max_agent_steps")
        val CONFIRM_MEDIUM_RISK = booleanPreferencesKey("confirm_medium_risk")
        val HISTORY_WINDOW = intPreferencesKey("history_window_size")
        val REDACT_TEXT = booleanPreferencesKey("send_redacted_text_only")
        val RETENTION_DAYS = intPreferencesKey("retention_days")
    }

    override val agentSettings: Flow<AgentSettings> = context.agentSettingsDataStore.data.map { prefs ->
        AgentSettings(
            maxAgentSteps = prefs[Keys.MAX_STEPS] ?: 8,
            confirmMediumRisk = prefs[Keys.CONFIRM_MEDIUM_RISK] ?: true,
            historyWindowSize = prefs[Keys.HISTORY_WINDOW] ?: 20,
            sendRedactedTextOnly = prefs[Keys.REDACT_TEXT] ?: true,
            retentionDays = prefs[Keys.RETENTION_DAYS] ?: 90
        )
    }

    override suspend fun setMaxAgentSteps(steps: Int) {
        context.agentSettingsDataStore.edit { prefs -> prefs[Keys.MAX_STEPS] = steps }
    }

    override suspend fun setConfirmMediumRisk(confirm: Boolean) {
        context.agentSettingsDataStore.edit { prefs -> prefs[Keys.CONFIRM_MEDIUM_RISK] = confirm }
    }

    override suspend fun setHistoryWindowSize(size: Int) {
        context.agentSettingsDataStore.edit { prefs -> prefs[Keys.HISTORY_WINDOW] = size }
    }

    override suspend fun setSendRedactedTextOnly(redact: Boolean) {
        context.agentSettingsDataStore.edit { prefs -> prefs[Keys.REDACT_TEXT] = redact }
    }

    override suspend fun setRetentionDays(days: Int) {
        context.agentSettingsDataStore.edit { prefs -> prefs[Keys.RETENTION_DAYS] = days }
    }
}
