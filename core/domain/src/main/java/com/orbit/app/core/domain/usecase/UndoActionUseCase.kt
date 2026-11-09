package com.orbit.app.core.domain.usecase

import com.orbit.app.core.domain.model.ToolResult
import com.orbit.app.core.domain.repository.ActionLogRepository
import com.orbit.app.core.domain.repository.ToolRegistry
import com.orbit.app.core.domain.result.Result
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

sealed interface UndoStatus {
    data class Undone(val message: String) : UndoStatus
    data class PartiallyUndone(val message: String) : UndoStatus
    data class Failed(val reason: String) : UndoStatus
}

@Singleton
class UndoActionUseCase @Inject constructor(
    private val actionLogRepository: ActionLogRepository,
    private val toolRegistry: ToolRegistry
) {

    suspend fun executeUndo(actionLogId: String): UndoStatus {
        val timeline = actionLogRepository.timeline.first()
        val targetLog = timeline.find { it.id == actionLogId }
            ?: return UndoStatus.Failed("Action log entry not found.")

        val payload = targetLog.undoPayloadJson
            ?: return UndoStatus.Failed("Action does not support undo.")

        val tool = toolRegistry.findTool(targetLog.toolName)
            ?: return UndoStatus.Failed("Tool '${targetLog.toolName}' no longer available.")

        val undoResult = tool.undo(payload)
            ?: return UndoStatus.Failed("Undo operation not supported by tool.")

        return when (undoResult) {
            is ToolResult.Success -> {
                actionLogRepository.markUndone(actionLogId)
                if (payload.contains("best_effort")) {
                    UndoStatus.PartiallyUndone(undoResult.summary)
                } else {
                    UndoStatus.Undone(undoResult.summary)
                }
            }
            is ToolResult.Failure -> UndoStatus.Failed(undoResult.message)
        }
    }
}
