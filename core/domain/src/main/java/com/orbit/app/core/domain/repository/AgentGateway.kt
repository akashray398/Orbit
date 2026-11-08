package com.orbit.app.core.domain.repository

import com.orbit.app.core.domain.model.AgentEvent
import kotlinx.coroutines.flow.Flow

interface AgentGateway {
    fun send(userText: String): Flow<AgentEvent>
}
