package com.orbit.app.core.domain.repository

import com.orbit.app.core.domain.model.VoiceLocale
import kotlinx.coroutines.flow.StateFlow

interface SpeechOutputController {
    val isSpeaking: StateFlow<Boolean>
    fun speak(text: String, locale: VoiceLocale)
    fun stop()
    fun isLanguageAvailable(locale: VoiceLocale): Boolean
    fun release()
}
