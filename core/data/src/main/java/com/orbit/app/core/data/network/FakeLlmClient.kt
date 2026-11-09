package com.orbit.app.core.data.network

import com.orbit.app.core.domain.model.LlmEvent
import com.orbit.app.core.domain.model.LlmRequest
import com.orbit.app.core.domain.repository.LlmClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeLlmClient @Inject constructor() : LlmClient {

    override fun stream(request: LlmRequest): Flow<LlmEvent> = flow {
        val lastUserMsg = request.messages.lastOrNull { it.role == "user" }?.content?.firstOrNull()
        val text = (lastUserMsg as? com.orbit.app.core.domain.model.ContentPart.Text)?.text ?: ""

        if (text.contains("time", ignoreCase = true) && request.tools.any { it.name == "get_current_time" }) {
            emit(
                LlmEvent.ToolCallRequested(
                    id = "call_fake_101",
                    name = "get_current_time",
                    argumentsJson = "{}"
                )
            )
            emit(LlmEvent.Usage(12, 18))
            emit(LlmEvent.Done("tool_use"))
            return@flow
        }

        val fakeReply = "I am Orbit, connected to your LLM backend proxy! I received your prompt: \"$text\"."
        val words = fakeReply.split(" ")

        for ((index, word) in words.withIndex()) {
            val delta = if (index == 0) word else " $word"
            emit(LlmEvent.TextDelta(delta))
            delay(40)
        }

        emit(LlmEvent.Usage(inputTokens = 15, outputTokens = words.size * 2))
        emit(LlmEvent.Done("end_turn"))
    }
}
