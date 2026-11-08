package com.orbit.app.core.domain.repository

import com.orbit.app.core.domain.model.AppSettings
import com.orbit.app.core.domain.model.ReducedMotionOverride
import com.orbit.app.core.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface AppSettingsRepository {
    val appSettings: Flow<AppSettings>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setDynamicColorEnabled(enabled: Boolean)
    suspend fun setReducedMotionOverride(override: ReducedMotionOverride)
    suspend fun setHapticsEnabled(enabled: Boolean)
    suspend fun setLastActiveConversationId(id: String)
    suspend fun setShowDebugTools(show: Boolean)
}
