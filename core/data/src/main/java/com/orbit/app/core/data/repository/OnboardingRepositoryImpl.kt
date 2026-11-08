package com.orbit.app.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.orbit.app.core.domain.model.OnboardingState
import com.orbit.app.core.domain.repository.OnboardingRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "orbit_onboarding_prefs")

@Singleton
class OnboardingRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : OnboardingRepository {

    private object PreferenceKeys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val LAST_COMPLETED_PAGE = intPreferencesKey("last_completed_page")
        val PERMISSION_STEP_SEEN = booleanPreferencesKey("permission_step_seen")
        val ASKED_PERMISSIONS = stringSetPreferencesKey("asked_permissions")
    }

    override val onboardingState: Flow<OnboardingState> = context.dataStore.data.map { preferences ->
        OnboardingState(
            onboardingCompleted = preferences[PreferenceKeys.ONBOARDING_COMPLETED] ?: false,
            lastCompletedPage = preferences[PreferenceKeys.LAST_COMPLETED_PAGE] ?: 0,
            permissionStepSeen = preferences[PreferenceKeys.PERMISSION_STEP_SEEN] ?: false,
            askedPermissions = preferences[PreferenceKeys.ASKED_PERMISSIONS] ?: emptySet()
        )
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    override suspend fun saveLastCompletedPage(page: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.LAST_COMPLETED_PAGE] = page
        }
    }

    override suspend fun savePermissionStepSeen(seen: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.PERMISSION_STEP_SEEN] = seen
        }
    }

    override suspend fun savePermissionAsked(permissionKey: String, asked: Boolean) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferenceKeys.ASKED_PERMISSIONS] ?: emptySet()
            val updated = if (asked) current + permissionKey else current - permissionKey
            preferences[PreferenceKeys.ASKED_PERMISSIONS] = updated
        }
    }

    override suspend fun resetOnboarding() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
