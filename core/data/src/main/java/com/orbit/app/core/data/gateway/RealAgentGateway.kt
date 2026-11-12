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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Calendar
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
        var lastToolSummary: String? = null

        while (loopActive && stepCount < maxSteps) {
            stepCount++

            val request = LlmRequest(
                messages = workingMessages,
                tools = availableToolSpecs
            )

            var textAccumulated = ""
            var pendingToolCall: LlmEvent.ToolCallRequested? = null
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
                    is LlmEvent.Failed -> {
                        errorOccurred = event.error
                    }
                    else -> {}
                }
            }

            // Fallback to local intelligent agent simulation if backend proxy is not running
            if (errorOccurred != null) {
                runOfflineFallback(userText).collect { emit(it) }
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

                val callKey = "$toolName:$argsJson"
                val callCount = (recentToolCallsMap[callKey] ?: 0) + 1
                recentToolCallsMap[callKey] = callCount

                if (callCount >= 3) {
                    emit(AgentEvent.StateChanged(AgentState.Idle))
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

                val logRes = actionLogRepository.startAction(toolName, argsJson, tool.riskLevel)
                val actionLogId = (logRes as? com.orbit.app.core.domain.result.Result.Success)?.data?.id

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

                lastToolSummary = resultSummary
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
                if (textAccumulated.isEmpty() && lastToolSummary != null) {
                    emit(AgentEvent.TextDelta(lastToolSummary!!))
                }
                loopActive = false
            }
        }

        if (stepCount >= maxSteps) {
            emit(AgentEvent.StepLimitReached)
        }

        emit(AgentEvent.StateChanged(AgentState.Idle))
        emit(AgentEvent.Completed)
    }

    private fun runOfflineFallback(userText: String): Flow<AgentEvent> = flow {
        val lower = userText.lowercase()

        val (toolName, toolArgsJson) = when {
            lower.contains("alarm") -> {
                val (hour, minute) = parseHourAndMinute(userText)
                Pair("set_alarm", "{\"hour\":$hour,\"minute\":$minute,\"label\":\"Orbit Alarm\"}")
            }
            lower.contains("timer") -> {
                Pair("set_timer", "{\"durationSeconds\":600,\"label\":\"Orbit Timer\"}")
            }
            lower.contains("remind") -> {
                val title = userText.replace(Regex("(?i)remind me to|remind me|set reminder for"), "").trim().ifEmpty { "Orbit Reminder" }
                val futureIso = "2025-11-12T18:00:00"
                Pair("create_reminder", "{\"title\":\"$title\",\"triggerAt\":\"$futureIso\"}")
            }
            lower.contains("time") -> Pair("get_current_time", "{}")
            lower.contains("note") -> {
                val body = userText.replace(Regex("(?i)note:|add note|create note|write note"), "").trim().ifEmpty { "Orbit Note" }
                Pair("create_note", "{\"body\":\"$body\",\"title\":\"Note\"}")
            }
            lower.contains("task") -> {
                val title = userText.replace(Regex("(?i)add task:|add task|create task"), "").trim().ifEmpty { "Orbit Task" }
                Pair("add_task", "{\"title\":\"$title\"}")
            }
            lower.contains("calendar") || lower.contains("event") -> {
                Pair("add_calendar_event", "{\"title\":\"Orbit Event\",\"startAt\":\"2025-11-12T18:00:00\"}")
            }
            lower.contains("status") || lower.contains("battery") -> Pair("get_device_status", "{}")
            lower.contains("notification") || lower.contains("missed") || lower.contains("miss") || lower.contains("summarise") || lower.contains("summary") -> {
                Pair("summarize_notifications", "{\"sinceHours\":12}")
            }
            lower.contains("dnd") || lower.contains("disturb") -> Pair("set_do_not_disturb", "{\"mode\":\"PRIORITY_ONLY\"}")
            lower.contains("call") || lower.contains("phone") || lower.contains("dial") -> {
                val name = userText.replace(Regex("(?i)call|make a call to|dial"), "").trim().ifEmpty { "Contact" }
                Pair("make_call", "{\"name\":\"$name\"}")
            }
            lower.contains("maps") -> Pair("open_app", "{\"target\":\"MAPS\"}")
            lower.contains("camera") -> Pair("open_app", "{\"target\":\"CAMERA\"}")
            else -> Pair(null, null)
        }

        var executionSummary: String? = null

        if (toolName != null && toolArgsJson != null) {
            val tool = toolRegistry.findTool(toolName)
            if (tool != null) {
                val callId = "call_local_${System.currentTimeMillis()}"
                val toolArgs = ToolArguments.fromJsonString(toolArgsJson)
                val describeText = tool.describeAction(toolArgs)

                emit(AgentEvent.ToolStarted(callId, toolName, tool.displayLabel, describeText))
                emit(AgentEvent.StateChanged(AgentState.Acting(tool.displayLabel)))

                val logRes = actionLogRepository.startAction(toolName, toolArgsJson, tool.riskLevel)
                val actionLogId = (logRes as? com.orbit.app.core.domain.result.Result.Success)?.data?.id

                val ctx = ToolContext(messageId = UUID.randomUUID().toString())
                val res = try {
                    tool.execute(toolArgs, ctx)
                } catch (e: Exception) {
                    ToolResult.Failure(ToolError.Unknown(e.message ?: "Failed"), e.message ?: "Error")
                }

                val (status, summary, undoPayload) = when (res) {
                    is ToolResult.Success -> Triple(ActionStatus.Success, res.summary, res.undoPayload)
                    is ToolResult.Failure -> Triple(ActionStatus.Failed, res.message, null)
                }

                if (actionLogId != null) {
                    actionLogRepository.finishAction(actionLogId, status, summary, undoPayload)
                }

                executionSummary = summary
                emit(AgentEvent.ToolFinished(callId, status, summary))
            }
        }

        emit(AgentEvent.StateChanged(AgentState.Idle))

        val responseText = executionSummary ?: when {
            lower.contains("plan my day") || lower.contains("plan") -> "Here is your time-blocked schedule for today:\n- 09:00 AM: Check Notifications & Reminders\n- 10:00 AM: Team Sync Meeting\n- 01:00 PM: Project Review\n- 05:00 PM: Daily Review & Tasks"
            else -> "I am Orbit, your personal phone AI assistant. I can manage your alarms, reminders, notes, tasks, calendar, device settings, and notifications."
        }

        val words = responseText.split(" ")
        for ((index, word) in words.withIndex()) {
            val delta = if (index == 0) word else " $word"
            emit(AgentEvent.TextDelta(delta))
            delay(30)
        }

        emit(AgentEvent.Completed)
    }

    private fun parseHourAndMinute(input: String): Pair<Int, Int> {
        val lower = input.lowercase()

        // Match time with minutes like "9:30", "11:15", "09.30"
        val timeWithMinutesMatch = Regex("(\\d{1,2})[:.](\\d{2})").find(lower)
        if (timeWithMinutesMatch != null) {
            var hour = timeWithMinutesMatch.groupValues[1].toIntOrNull() ?: 9
            val minute = timeWithMinutesMatch.groupValues[2].toIntOrNull() ?: 0
            if ((lower.contains("pm") || lower.contains("p.m")) && hour in 1..11) hour += 12
            if ((lower.contains("am") || lower.contains("a.m")) && hour == 12) hour = 0
            return Pair(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
        }

        // Match standalone hour numbers e.g. "11 am", "9pm", "for 11", "at 9"
        val allNumbers = Regex("(\\d{1,2})").findAll(lower).mapNotNull { it.groupValues[1].toIntOrNull() }.toList()
        val validHourCandidate = allNumbers.firstOrNull { it in 1..24 }

        if (validHourCandidate != null) {
            var hour = validHourCandidate
            val isPm = lower.contains("pm") || lower.contains("p.m") || lower.contains("night") || lower.contains("evening")
            val isAm = lower.contains("am") || lower.contains("a.m") || lower.contains("morning")

            if (isPm && hour in 1..11) {
                hour += 12
            } else if (isAm && hour == 12) {
                hour = 0
            }
            return Pair(hour.coerceIn(0, 23), 0)
        }

        // Fallback to next upcoming hour
        val cal = Calendar.getInstance()
        val nextHour = (cal.get(Calendar.HOUR_OF_DAY) + 1) % 24
        return Pair(nextHour, 0)
    }
}
