package com.orbit.app.feature.today.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orbit.app.core.common.coroutine.DispatcherProvider
import com.orbit.app.core.data.database.daos.NotificationDao
import com.orbit.app.core.domain.model.Task
import com.orbit.app.core.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TodayUiState(
    val openTasksCount: Int = 0,
    val completedTasksCount: Int = 0,
    val openTasks: List<Task> = emptyList(),
    val notificationsCount: Int = 0,
    val streakDays: Int = 3
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val notificationDao: NotificationDao,
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch(dispatchers.io) {
            taskRepository.openTasks.collect { open ->
                _uiState.update { it.copy(openTasks = open, openTasksCount = open.size) }
            }
        }
        viewModelScope.launch(dispatchers.io) {
            taskRepository.completedTasks.collect { completed ->
                _uiState.update { it.copy(completedTasksCount = completed.size) }
            }
        }
        viewModelScope.launch(dispatchers.io) {
            notificationDao.observeCount().collect { count ->
                _uiState.update { it.copy(notificationsCount = count) }
            }
        }
    }

    fun completeTask(id: String) {
        viewModelScope.launch(dispatchers.io) {
            taskRepository.completeTask(id)
        }
    }
}
