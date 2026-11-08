package com.orbit.app.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.orbit.app.core.domain.model.AppSettings
import com.orbit.app.core.domain.model.ReducedMotionOverride
import com.orbit.app.core.domain.model.ThemeMode
import com.orbit.app.core.domain.repository.AppSettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.appSettingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "orbit_app_settings_prefs")

@Singleton
class AppSettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AppSettingsRepository {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color_enabled")
        val REDUCED_MOTION = stringPreferencesKey("reduced_motion_override")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val LAST_CONVERSATION_ID = stringPreferencesKey("last_active_conversation_id")
        val SHOW_DEBUG_TOOLS = booleanPreferencesKey("show_debug_tools")
    }

    override val appSettings: Flow<AppSettings> = context.appSettingsDataStore.data.map { prefs ->
        AppSettings(
            themeMode = try { ThemeMode.valueOf(prefs[Keys.THEME_MODE] ?: ThemeMode.System.name) } catch (_: Exception) { ThemeMode.System },
            dynamicColorEnabled = prefs[Keys.DYNAMIC_COLOR] ?: false,
            reducedMotionOverride = try { ReducedMotionOverride.valueOf(prefs[Keys.REDUCED_MOTION] ?: ReducedMotionOverride.System.name) } catch (_: Exception) { ReducedMotionOverride.System },
            hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true,
            lastActiveConversationId = prefs[Keys.LAST_CONVERSATION_ID] ?: "default_conversation",
            showDebugTools = prefs[Keys.SHOW_DEBUG_TOOLS] ?: true
        )
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        context.appSettingsDataStore.edit { prefs -> prefs[Keys.THEME_MODE] = mode.name }
    }

    override suspend fun setDynamicColorEnabled(enabled: Boolean) {
        context.appSettingsDataStore.edit { prefs -> prefs[Keys.DYNAMIC_COLOR] = enabled }
    }

    override suspend fun setReducedMotionOverride(override: ReducedMotionOverride) {
        context.appSettingsDataStore.edit { prefs -> prefs[Keys.REDUCED_MOTION] = override.name }
    }

    override suspend fun setHapticsEnabled(enabled: Boolean) {
        context.appSettingsDataStore.edit { prefs -> prefs[Keys.HAPTICS_ENABLED] = enabled }
    }

    override suspend fun setLastActiveConversationId(id: String) {
        context.appSettingsDataStore.edit { prefs -> prefs[Keys.LAST_CONVERSATION_ID] = id }
    }

    override suspend fun setShowDebugTools(show: Boolean) {
        context.appSettingsDataStore.edit { prefs -> prefs[Keys.SHOW_DEBUG_TOOLS] = show }
    }
}
