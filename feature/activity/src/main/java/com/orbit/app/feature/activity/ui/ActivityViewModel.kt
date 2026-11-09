package com.orbit.app.feature.activity.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orbit.app.core.common.coroutine.DispatcherProvider
import com.orbit.app.core.domain.model.ActionLog
import com.orbit.app.core.domain.repository.ActionLogRepository
import com.orbit.app.core.domain.usecase.UndoActionUseCase
import com.orbit.app.core.domain.usecase.UndoStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ActivityUiState(
    val actionLogs: List<ActionLog> = emptyList(),
    val selectedLog: ActionLog? = null,
    val messageText: String? = null
)

@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val actionLogRepository: ActionLogRepository,
    private val undoActionUseCase: UndoActionUseCase,
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityUiState())
    val uiState: StateFlow<ActivityUiState> = _uiState.asStateFlow()

    init {
        observeTimeline()
    }

    private fun observeTimeline() {
        viewModelScope.launch(dispatchers.io) {
            actionLogRepository.timeline.collect { logs ->
                _uiState.update { it.copy(actionLogs = logs) }
            }
        }
    }

    fun selectLog(log: ActionLog?) {
        _uiState.update { it.copy(selectedLog = log) }
    }

    fun undoAction(actionLogId: String) {
        viewModelScope.launch(dispatchers.io) {
            val status = undoActionUseCase.executeUndo(actionLogId)
            val text = when (status) {
                is UndoStatus.Undone -> status.message
                is UndoStatus.PartiallyUndone -> status.message
                is UndoStatus.Failed -> "Undo failed: ${status.reason}"
            }
            _uiState.update { it.copy(messageText = text) }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(messageText = null) }
    }
}
