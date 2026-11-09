package com.orbit.app.core.domain.usecase

import com.orbit.app.core.domain.model.ChatMessage
import com.orbit.app.core.domain.model.ChatMessageRole
import com.orbit.app.core.domain.model.ContentPart
import com.orbit.app.core.domain.model.LlmMessage
import com.orbit.app.core.domain.model.Redactor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationContextBuilder @Inject constructor() {

    fun build(
        messages: List<ChatMessage>,
        historyWindowSize: Int = 20,
        sendRedactedTextOnly: Boolean = true
    ): List<LlmMessage> {
        val window = messages.takeLast(historyWindowSize)

        return window.map { msg ->
            val roleStr = when (msg.role) {
                ChatMessageRole.User -> "user"
                ChatMessageRole.Agent -> "assistant"
                ChatMessageRole.System -> "user"
            }

            val contentText = if (sendRedactedTextOnly) Redactor.redact(msg.content) else msg.content

            LlmMessage(
                role = roleStr,
                content = listOf(ContentPart.Text(contentText))
            )
        }
    }
}
