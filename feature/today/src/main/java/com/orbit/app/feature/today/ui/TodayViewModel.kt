package com.orbit.app.feature.today.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orbit.app.core.common.coroutine.DispatcherProvider
import com.orbit.app.core.data.database.daos.NotificationDao
import com.orbit.app.core.data.planner.DailyPlanReport
import com.orbit.app.core.data.planner.DailyPlannerEngine
import com.orbit.app.core.data.planner.PlannerItem
import com.orbit.app.core.domain.model.Task
import com.orbit.app.core.domain.model.ToolArguments
import com.orbit.app.core.domain.model.ToolContext
import com.orbit.app.core.domain.repository.TaskRepository
import com.orbit.app.core.tools.AddCalendarEventTool
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class TodayUiState(
    val openTasksCount: Int = 0,
    val completedTasksCount: Int = 0,
    val openTasks: List<Task> = emptyList(),
    val notificationsCount: Int = 0,
    val streakDays: Int = 5,
    val planReport: DailyPlanReport? = null,
    val calendarAddedNotice: String? = null
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val notificationDao: NotificationDao,
    private val plannerEngine: DailyPlannerEngine,
    private val addCalendarEventTool: AddCalendarEventTool,
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    init {
        observeData()
        generateDailyPlan()
    }

    fun generateDailyPlan(incompleteSubject: String? = null, isTiredMode: Boolean = false) {
        val report = plannerEngine.generatePlan(incompleteSubject, isTiredMode)
        _uiState.update { it.copy(planReport = report) }
    }

    fun toggleItemCompleted(itemId: String) {
        val currentReport = _uiState.value.planReport ?: return
        val updatedItems = currentReport.items.map { item ->
            if (item.id == itemId) {
                item.copy(isCompleted = !item.isCompleted)
            } else {
                item
            }
        }
        _uiState.update {
            it.copy(planReport = currentReport.copy(items = updatedItems))
        }
    }

    fun addPlanToCalendar() {
        val report = _uiState.value.planReport ?: return
        viewModelScope.launch(dispatchers.io) {
            val studyItems = report.items.filter { it.category == "STUDY" || it.category == "PROJECT" }
            var count = 0

            studyItems.forEach { item ->
                val args = ToolArguments.fromJsonString("{\"title\":\"${item.title}\"}")
                val ctx = ToolContext(messageId = UUID.randomUUID().toString())
                try {
                    addCalendarEventTool.execute(args, ctx)
                    count++
                } catch (_: Exception) {}
            }

            _uiState.update {
                it.copy(calendarAddedNotice = "$count study sessions added to your calendar!")
            }
        }
    }

    fun clearNotice() {
        _uiState.update { it.copy(calendarAddedNotice = null) }
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
