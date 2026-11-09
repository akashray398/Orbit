package com.orbit.app.core.domain.repository

import com.orbit.app.core.domain.model.LlmEvent
import com.orbit.app.core.domain.model.LlmRequest
import kotlinx.coroutines.flow.Flow

interface LlmClient {
    fun stream(request: LlmRequest): Flow<LlmEvent>
}
