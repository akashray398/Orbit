package com.orbit.app.feature.onboarding.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orbit.app.core.common.coroutine.DispatcherProvider
import com.orbit.app.core.domain.model.OnboardingState
import com.orbit.app.core.domain.usecase.CompleteOnboardingUseCase
import com.orbit.app.core.domain.usecase.ObserveOnboardingStateUseCase
import com.orbit.app.core.domain.usecase.SavePermissionsStepUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val onboardingState: OnboardingState = OnboardingState(),
    val isPermissionsStep: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val observeOnboardingStateUseCase: ObserveOnboardingStateUseCase,
    private val completeOnboardingUseCase: CompleteOnboardingUseCase,
    private val savePermissionsStepUseCase: SavePermissionsStepUseCase,
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        observeState()
    }

    private fun observeState() {
        viewModelScope.launch(dispatchers.io) {
            observeOnboardingStateUseCase().collect { state ->
                _uiState.value = _uiState.value.copy(
                    onboardingState = state,
                    isPermissionsStep = state.permissionStepSeen
                )
            }
        }
    }

    fun onPageChanged(page: Int) {
        viewModelScope.launch(dispatchers.io) {
            savePermissionsStepUseCase.setPage(page)
        }
    }

    fun onShowPermissionsStep() {
        viewModelScope.launch(dispatchers.io) {
            savePermissionsStepUseCase.setPermissionStepSeen(true)
        }
    }

    fun onPermissionAsked(permissionKey: String) {
        viewModelScope.launch(dispatchers.io) {
            savePermissionsStepUseCase.setPermissionAsked(permissionKey, true)
        }
    }

    fun completeOnboarding(onComplete: () -> Unit) {
        viewModelScope.launch(dispatchers.io) {
            completeOnboardingUseCase()
            viewModelScope.launch(dispatchers.main) {
                onComplete()
            }
        }
    }
}
