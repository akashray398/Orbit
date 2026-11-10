package com.orbit.app.feature.routines.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orbit.app.core.common.coroutine.DispatcherProvider
import com.orbit.app.core.data.database.daos.RoutineDao
import com.orbit.app.core.data.database.entities.RoutineEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class RoutinesUiState(
    val routines: List<RoutineEntity> = emptyList(),
    val messageText: String? = null
)

@HiltViewModel
class RoutinesViewModel @Inject constructor(
    private val routineDao: RoutineDao,
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoutinesUiState())
    val uiState: StateFlow<RoutinesUiState> = _uiState.asStateFlow()

    init {
        observeRoutines()
    }

    private fun observeRoutines() {
        viewModelScope.launch(dispatchers.io) {
            routineDao.observeRoutines().collect { list ->
                _uiState.update { it.copy(routines = list) }
            }
        }
    }

    fun toggleRoutine(routine: RoutineEntity) {
        viewModelScope.launch(dispatchers.io) {
            val updated = routine.copy(enabled = !routine.enabled)
            routineDao.insertRoutine(updated)
        }
    }

    fun addPresetRoutine(name: String, type: String, time: String) {
        viewModelScope.launch(dispatchers.io) {
            val entity = RoutineEntity(
                id = UUID.randomUUID().toString(),
                name = name,
                type = type,
                timeOfDay = time,
                enabled = true
            )
            routineDao.insertRoutine(entity)
            _uiState.update { it.copy(messageText = "Routine '$name' added.") }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(messageText = null) }
    }
}
