package com.orbit.app.core.data.database.daos

import com.orbit.app.core.data.database.entities.MessageEntity
import com.orbit.app.core.domain.model.ChatMessageRole
import com.orbit.app.core.domain.model.ChatMessageStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class MessageDaoTest {

    @Test
    fun messageEntity_maps_streaming_and_interrupted_statuses() {
        val msg = MessageEntity(
            id = "msg_streaming_1",
            conversationId = "default_conversation",
            role = ChatMessageRole.Agent,
            content = "Streaming response token...",
            status = ChatMessageStatus.Streaming
        )

        assertEquals("msg_streaming_1", msg.id)
        assertEquals(ChatMessageStatus.Streaming, msg.status)

        val updated = msg.copy(status = ChatMessageStatus.Interrupted)
        assertEquals(ChatMessageStatus.Interrupted, updated.status)
    }
}
