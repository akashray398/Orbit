package com.orbit.app.core.domain.usecase

import com.orbit.app.core.domain.model.ChatMessage
import com.orbit.app.core.domain.model.ChatMessageRole
import com.orbit.app.core.domain.model.ContentPart
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConversationContextBuilderTest {

    private val builder = ConversationContextBuilder()

    @Test
    fun contextBuilder_respects_history_window_size() {
        val messages = (1..30).map { i ->
            ChatMessage(
                id = "msg_$i",
                role = if (i % 2 == 1) ChatMessageRole.User else ChatMessageRole.Agent,
                content = "Message $i"
            )
        }

        val result = builder.build(messages, historyWindowSize = 10)
        assertEquals(10, result.size)
        assertEquals("Message 21", (result.first().content.first() as ContentPart.Text).text)
    }

    @Test
    fun contextBuilder_applies_redaction_to_user_messages() {
        val messages = listOf(
            ChatMessage(
                id = "msg_1",
                role = ChatMessageRole.User,
                content = "Send email to test@orbit.app"
            )
        )

        val result = builder.build(messages, historyWindowSize = 10, sendRedactedTextOnly = true)
        val text = (result.first().content.first() as ContentPart.Text).text
        assertTrue(text.contains("[REDACTED_EMAIL]"))
    }
}
