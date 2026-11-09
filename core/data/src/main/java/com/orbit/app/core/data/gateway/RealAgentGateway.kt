package com.orbit.app.core.data.gateway

import com.orbit.app.core.domain.model.ActionStatus
import com.orbit.app.core.domain.model.AgentEvent
import com.orbit.app.core.domain.model.AgentState
import com.orbit.app.core.domain.model.AppError
import com.orbit.app.core.domain.model.ConfirmationDecision
import com.orbit.app.core.domain.model.ConfirmationGate
import com.orbit.app.core.domain.model.ContentPart
import com.orbit.app.core.domain.model.LlmEvent
import com.orbit.app.core.domain.model.LlmMessage
import com.orbit.app.core.domain.model.LlmRequest
import com.orbit.app.core.domain.model.PendingAction
import com.orbit.app.core.domain.model.RiskLevel
import com.orbit.app.core.domain.model.ToolArguments
import com.orbit.app.core.domain.model.ToolContext
import com.orbit.app.core.domain.model.ToolError
import com.orbit.app.core.domain.model.ToolResult
import com.orbit.app.core.domain.repository.ActionLogRepository
import com.orbit.app.core.domain.repository.AgentGateway
import com.orbit.app.core.domain.repository.AgentSettingsRepository
import com.orbit.app.core.domain.repository.ChatRepository
import com.orbit.app.core.domain.repository.LlmClient
import com.orbit.app.core.domain.repository.ToolRegistry
import com.orbit.app.core.domain.usecase.ConversationContextBuilder
import com.orbit.app.core.domain.usecase.ToolResultSanitizer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeoutOrNull
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealAgentGateway @Inject constructor(
    private val llmClient: LlmClient,
    private val toolRegistry: ToolRegistry,
    private val chatRepository: ChatRepository,
    private val actionLogRepository: ActionLogRepository,
    private val agentSettingsRepository: AgentSettingsRepository,
    private val confirmationGate: ConfirmationGate,
    private val contextBuilder: ConversationContextBuilder,
    private val sanitizer: ToolResultSanitizer
) : AgentGateway {

    private val blocklistKeywords = listOf("bank", "upi", "wallet", "password", "pin", "credential", "paytm", "gpay")

    override fun send(userText: String): Flow<AgentEvent> = flow {
        emit(AgentEvent.StateChanged(AgentState.Thinking))

        val settings = agentSettingsRepository.agentSettings.first()
        val maxSteps = settings.maxAgentSteps
        val availableToolSpecs = toolRegistry.getAvailableToolSpecs()

        val conversationMessages = chatRepository.messages.first()
        val workingMessages = contextBuilder.build(
            messages = conversationMessages,
            historyWindowSize = settings.historyWindowSize,
            sendRedactedTextOnly = settings.sendRedactedTextOnly
        ).toMutableList()

        val recentToolCallsMap = mutableMapOf<String, Int>()
        var stepCount = 0
        var loopActive = true

        while (loopActive && stepCount < maxSteps) {
            stepCount++

            val request = LlmRequest(
                messages = workingMessages,
                tools = availableToolSpecs
            )

            var textAccumulated = ""
            var pendingToolCall: LlmEvent.ToolCallRequested? = null
            var stopReason: String? = null
            var errorOccurred: AppError? = null

            llmClient.stream(request).collect { event ->
                when (event) {
                    is LlmEvent.TextDelta -> {
                        textAccumulated += event.text
                        emit(AgentEvent.TextDelta(event.text))
                    }
                    is LlmEvent.ToolCallRequested -> {
                        pendingToolCall = event
                    }
                    is LlmEvent.Done -> {
                        stopReason = event.stopReason
                    }
                    is LlmEvent.Failed -> {
                        errorOccurred = event.error
                    }
                    else -> {}
                }
            }

            if (errorOccurred != null) {
                emit(AgentEvent.Failed(errorOccurred!!))
                return@flow
            }

            if (textAccumulated.isNotEmpty()) {
                workingMessages.add(
                    LlmMessage(
                        role = "assistant",
                        content = listOf(ContentPart.Text(textAccumulated))
                    )
                )
            }

            val toolCall = pendingToolCall
            if (toolCall != null) {
                val callId = toolCall.id
                val toolName = toolCall.name
                val argsJson = toolCall.argumentsJson

                // Loop detection
                val callKey = "$toolName:$argsJson"
                val callCount = (recentToolCallsMap[callKey] ?: 0) + 1
                recentToolCallsMap[callKey] = callCount

                if (callCount >= 3) {
                    emit(AgentEvent.Failed(AppError.Unknown("Blocked repeated tool call loop")))
                    return@flow
                }

                val tool = toolRegistry.findTool(toolName)
                if (tool == null) {
                    workingMessages.add(
                        LlmMessage(
                            role = "tool",
                            content = listOf(ContentPart.ToolResult(callId, "Tool '$toolName' not found", isError = true))
                        )
                    )
                    continue
                }

                val toolArgs = ToolArguments.fromJsonString(argsJson)
                val describeText = tool.describeAction(toolArgs)

                emit(AgentEvent.ToolStarted(callId, toolName, tool.displayLabel, describeText))
                emit(AgentEvent.StateChanged(AgentState.Acting(tool.displayLabel)))

                // Blocklist check
                val isBlocked = blocklistKeywords.any { kw -> argsJson.contains(kw, ignoreCase = true) }
                if (isBlocked) {
                    val logRes = actionLogRepository.startAction(toolName, argsJson, tool.riskLevel)
                    if (logRes is com.orbit.app.core.domain.result.Result.Success) {
                        actionLogRepository.finishAction(logRes.data.id, ActionStatus.Denied, "Blocked security policy", null)
                    }
                    emit(AgentEvent.ToolFinished(callId, ActionStatus.Denied, "Blocked by safety policy"))
                    workingMessages.add(
                        LlmMessage(
                            role = "tool",
                            content = listOf(ContentPart.ToolResult(callId, "Action blocked by safety policy", isError = true))
                        )
                    )
                    continue
                }

                // Confirmation Gate Check
                val decision = confirmationGate.request(
                    PendingAction(toolName, tool.riskLevel, describeText, argsJson)
                )

                if (decision != ConfirmationDecision.Approved) {
                    val logRes = actionLogRepository.startAction(toolName, argsJson, tool.riskLevel)
                    if (logRes is com.orbit.app.core.domain.result.Result.Success) {
                        actionLogRepository.finishAction(logRes.data.id, ActionStatus.Denied, "User declined action", null)
                    }
                    emit(AgentEvent.ToolFinished(callId, ActionStatus.Denied, "User declined"))
                    workingMessages.add(
                        LlmMessage(
                            role = "tool",
                            content = listOf(ContentPart.ToolResult(callId, "User declined action", isError = true))
                        )
                    )
                    continue
                }

                // Log Start
                val logRes = actionLogRepository.startAction(toolName, argsJson, tool.riskLevel)
                val actionLogId = (logRes as? com.orbit.app.core.domain.result.Result.Success)?.data?.id

                // Execute with 15s timeout
                val ctx = ToolContext(messageId = UUID.randomUUID().toString())
                val rawResult = withTimeoutOrNull(15000L) {
                    try {
                        tool.execute(toolArgs, ctx)
                    } catch (e: Exception) {
                        ToolResult.Failure(ToolError.Unknown(e.message ?: "Execution failed"), e.message ?: "Execution error")
                    }
                } ?: ToolResult.Failure(ToolError.Timeout, "Tool execution timed out")

                val sanitizedResult = sanitizer.sanitize(rawResult)

                val (actionStatus, resultSummary, undoPayload) = when (sanitizedResult) {
                    is ToolResult.Success -> Triple(ActionStatus.Success, sanitizedResult.summary, sanitizedResult.undoPayload)
                    is ToolResult.Failure -> Triple(ActionStatus.Failed, sanitizedResult.message, null)
                }

                if (actionLogId != null) {
                    actionLogRepository.finishAction(actionLogId, actionStatus, resultSummary, undoPayload)
                }

                emit(AgentEvent.ToolFinished(callId, actionStatus, resultSummary))

                workingMessages.add(
                    LlmMessage(
                        role = "tool",
                        content = listOf(
                            ContentPart.ToolResult(
                                toolCallId = callId,
                                content = resultSummary,
                                isError = sanitizedResult is ToolResult.Failure
                            )
                        )
                    )
                )
            } else {
                loopActive = false
            }
        }

        if (stepCount >= maxSteps) {
            emit(AgentEvent.StepLimitReached)
        }

        emit(AgentEvent.StateChanged(AgentState.Idle))
        emit(AgentEvent.Completed)
    }
}
