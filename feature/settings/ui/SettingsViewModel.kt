package com.orbit.app.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orbit.app.core.common.coroutine.DispatcherProvider
import com.orbit.app.core.data.database.daos.NotificationDao
import com.orbit.app.core.domain.model.AgentSettings
import com.orbit.app.core.domain.model.AppSettings
import com.orbit.app.core.domain.model.ReducedMotionOverride
import com.orbit.app.core.domain.model.ThemeMode
import com.orbit.app.core.domain.model.ToolSpec
import com.orbit.app.core.domain.model.VoiceLocale
import com.orbit.app.core.domain.repository.AgentSettingsRepository
import com.orbit.app.core.domain.repository.AppSettingsRepository
import com.orbit.app.core.domain.repository.DataMaintenanceRepository
import com.orbit.app.core.domain.repository.OnboardingRepository
import com.orbit.app.core.domain.repository.ToolRegistry
import com.orbit.app.core.domain.repository.VoiceSettingsRepository
import com.orbit.app.core.tools.helpers.PermissionChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val appSettings: AppSettings = AppSettings(),
    val agentSettings: AgentSettings = AgentSettings(),
    val voiceLocale: VoiceLocale = VoiceLocale.ENGLISH,
    val spokenRepliesEnabled: Boolean = false,
    val tools: List<ToolSpec> = emptyList(),
    val capturedNotificationsCount: Int = 0,
    val hasCalendarPermission: Boolean = false,
    val messageText: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository,
    private val agentSettingsRepository: AgentSettingsRepository,
    private val voiceSettingsRepository: VoiceSettingsRepository,
    private val dataMaintenanceRepository: DataMaintenanceRepository,
    private val onboardingRepository: OnboardingRepository,
    private val toolRegistry: ToolRegistry,
    private val permissionChecker: PermissionChecker,
    private val notificationDao: NotificationDao,
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch(dispatchers.io) {
            appSettingsRepository.appSettings.collect { settings ->
                _uiState.update { it.copy(appSettings = settings) }
            }
        }
        viewModelScope.launch(dispatchers.io) {
            agentSettingsRepository.agentSettings.collect { settings ->
                _uiState.update { it.copy(agentSettings = settings) }
            }
        }
        viewModelScope.launch(dispatchers.io) {
            voiceSettingsRepository.voiceLocale.collect { locale ->
                _uiState.update { it.copy(voiceLocale = locale) }
            }
        }
        viewModelScope.launch(dispatchers.io) {
            voiceSettingsRepository.spokenRepliesEnabled.collect { enabled ->
                _uiState.update { it.copy(spokenRepliesEnabled = enabled) }
            }
        }
        viewModelScope.launch(dispatchers.io) {
            notificationDao.observeCount().collect { count ->
                _uiState.update { it.copy(capturedNotificationsCount = count) }
            }
        }

        val hasCal = permissionChecker.isPermissionGranted(android.Manifest.permission.READ_CALENDAR)
        val registeredTools = toolRegistry.getAvailableToolSpecs()
        _uiState.update { it.copy(hasCalendarPermission = hasCal, tools = registeredTools) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch(dispatchers.io) { appSettingsRepository.setThemeMode(mode) }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch(dispatchers.io) { appSettingsRepository.setDynamicColorEnabled(enabled) }
    }

    fun setReducedMotion(override: ReducedMotionOverride) {
        viewModelScope.launch(dispatchers.io) { appSettingsRepository.setReducedMotionOverride(override) }
    }

    fun setVoiceLocale(locale: VoiceLocale) {
        viewModelScope.launch(dispatchers.io) { voiceSettingsRepository.setVoiceLocale(locale) }
    }

    fun setSpokenReplies(enabled: Boolean) {
        viewModelScope.launch(dispatchers.io) { voiceSettingsRepository.setSpokenRepliesEnabled(enabled) }
    }

    fun setConfirmMediumRisk(confirm: Boolean) {
        viewModelScope.launch(dispatchers.io) { agentSettingsRepository.setConfirmMediumRisk(confirm) }
    }

    fun setSendRedactedText(redact: Boolean) {
        viewModelScope.launch(dispatchers.io) { agentSettingsRepository.setSendRedactedTextOnly(redact) }
    }

    fun clearChatHistory() {
        viewModelScope.launch(dispatchers.io) {
            dataMaintenanceRepository.clearChatHistory()
            _uiState.update { it.copy(messageText = "Chat history cleared.") }
        }
    }

    fun deleteAllNotes() {
        viewModelScope.launch(dispatchers.io) {
            dataMaintenanceRepository.deleteAllNotes()
            _uiState.update { it.copy(messageText = "All notes deleted.") }
        }
    }

    fun deleteAllTasksAndReminders() {
        viewModelScope.launch(dispatchers.io) {
            dataMaintenanceRepository.deleteAllTasksAndReminders()
            _uiState.update { it.copy(messageText = "All tasks and reminders deleted.") }
        }
    }

    fun wipeAllData() {
        viewModelScope.launch(dispatchers.io) {
            dataMaintenanceRepository.wipeAllData()
            _uiState.update { it.copy(messageText = "All local data wiped.") }
        }
    }

    fun resetOnboarding() {
        viewModelScope.launch(dispatchers.io) {
            onboardingRepository.resetOnboarding()
            _uiState.update { it.copy(messageText = "Onboarding status reset.") }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(messageText = null) }
    }
}
