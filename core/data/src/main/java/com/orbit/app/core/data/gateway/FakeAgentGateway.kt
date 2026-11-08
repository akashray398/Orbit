package com.orbit.app.core.data.gateway

import com.orbit.app.core.domain.model.AgentEvent
import com.orbit.app.core.domain.model.AgentState
import com.orbit.app.core.domain.model.AppError
import com.orbit.app.core.domain.repository.AgentGateway
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Temporary Fake Agent Gateway for Prompt 6.
 * Emits AgentState transitions (Thinking, Acting) and streams canned text deltas word-by-word.
 * Replaced in Prompt 10 with the real LLM tool-calling loop.
 */
@Singleton
class FakeAgentGateway @Inject constructor() : AgentGateway {

    override fun send(userText: String): Flow<AgentEvent> = flow {
        val lowercaseInput = userText.lowercase()

        if (lowercaseInput.contains("fail")) {
            emit(AgentEvent.StateChanged(AgentState.Thinking))
            delay(800)
            emit(AgentEvent.Failed(AppError.Unknown("Simulated agent connection failure")))
            return@flow
        }

        emit(AgentEvent.StateChanged(AgentState.Thinking))
        delay(700)

        val toolAction = when {
            lowercaseInput.contains("alarm") || lowercaseInput.contains("remind") -> "Setting alarm..."
            lowercaseInput.contains("calendar") || lowercaseInput.contains("plan") -> "Checking calendar..."
            lowercaseInput.contains("open") || lowercaseInput.contains("app") -> "Opening application..."
            else -> null
        }

        if (toolAction != null) {
            emit(AgentEvent.StateChanged(AgentState.Acting(toolAction)))
            delay(900)
        }

        emit(AgentEvent.StateChanged(AgentState.Idle))

        val responseText = when {
            lowercaseInput.contains("alarm") -> "I've set an alarm for 6:00 AM tomorrow."
            lowercaseInput.contains("plan") -> "Here is your plan for today:\n- 10:00 AM: Team Sync\n- 1:00 PM: Project Review\n- 5:00 PM: Focus Time"
            lowercaseInput.contains("calendar") -> "Your calendar is clear after 2:00 PM today."
            lowercaseInput.contains("remind") -> "Reminder created: **Drink water**."
            else -> "I'm Orbit, your personal AI agent. I can manage your alarms, schedule events, summarize notifications, and launch apps for you."
        }

        val words = responseText.split(" ")
        for ((index, word) in words.withIndex()) {
            val delta = if (index == 0) word else " $word"
            emit(AgentEvent.TextDelta(delta))
            delay(50)
        }

        emit(AgentEvent.Completed)
    }
}
