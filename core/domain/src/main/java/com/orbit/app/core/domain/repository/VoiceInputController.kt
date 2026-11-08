package com.orbit.app.core.domain.repository

import com.orbit.app.core.domain.model.VoiceInputEvent
import com.orbit.app.core.domain.model.VoiceInputState
import com.orbit.app.core.domain.model.VoiceLocale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface VoiceInputController {
    val state: StateFlow<VoiceInputState>
    val events: Flow<VoiceInputEvent>
    fun start(locale: VoiceLocale)
    fun stop()
    fun cancel()
    fun release()
}
