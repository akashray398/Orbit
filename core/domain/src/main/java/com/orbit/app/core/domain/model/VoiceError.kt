package com.orbit.app.core.domain.model

sealed interface VoiceError {
    data object NoSpeech : VoiceError
    data object NoMatch : VoiceError
    data object Busy : VoiceError
    data object Network : VoiceError
    data object PermissionDenied : VoiceError
    data object LanguageUnavailable : VoiceError
    data object RecognizerUnavailable : VoiceError
    data class Unknown(val message: String = "") : VoiceError
}
