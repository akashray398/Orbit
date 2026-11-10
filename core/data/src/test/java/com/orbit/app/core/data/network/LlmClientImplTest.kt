package com.orbit.app.core.data.network

import com.orbit.app.core.domain.model.AppError
import com.orbit.app.core.domain.model.LlmEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LlmClientImplTest {

    @Test
    fun llmEvent_types_map_correctly() {
        val textEvent: LlmEvent = LlmEvent.TextDelta("Hello world")
        val toolEvent: LlmEvent = LlmEvent.ToolCallRequested("call_1", "get_current_time", "{}")
        val usageEvent: LlmEvent = LlmEvent.Usage(10, 20)
        val doneEvent: LlmEvent = LlmEvent.Done("end_turn")
        val rateLimitError: LlmEvent = LlmEvent.Failed(AppError.RateLimited(60))

        assertTrue(textEvent is LlmEvent.TextDelta)
        assertEquals("Hello world", (textEvent as LlmEvent.TextDelta).text)
        assertEquals("get_current_time", (toolEvent as LlmEvent.ToolCallRequested).name)
        assertEquals(10, (usageEvent as LlmEvent.Usage).inputTokens)
        assertEquals("end_turn", (doneEvent as LlmEvent.Done).stopReason)
        assertEquals(60, ((rateLimitError as LlmEvent.Failed).error as AppError.RateLimited).retryAfterSeconds)
    }
}
