package com.orbit.app.core.data.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.orbit.app.core.domain.model.VoiceError
import com.orbit.app.core.domain.model.VoiceInputEvent
import com.orbit.app.core.domain.model.VoiceInputState
import com.orbit.app.core.domain.model.VoiceLocale
import com.orbit.app.core.domain.repository.VoiceInputController
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidVoiceInputController @Inject constructor(
    @ApplicationContext private val context: Context
) : VoiceInputController {

    private val _state = MutableStateFlow<VoiceInputState>(VoiceInputState.Idle)
    override val state: StateFlow<VoiceInputState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<VoiceInputEvent>(extraBufferCapacity = 64)
    override val events: Flow<VoiceInputEvent> = _events.asSharedFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var currentAmplitude = 0f
    private var retryCount = 0

    override fun start(locale: VoiceLocale) {
        mainHandler.post {
            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                _state.value = VoiceInputState.Error(VoiceError.RecognizerUnavailable)
                return@post
            }

            if (speechRecognizer != null) {
                try {
                    speechRecognizer?.cancel()
                    speechRecognizer?.destroy()
                } catch (_: Exception) {}
            }

            val recognizer = try {
                SpeechRecognizer.createSpeechRecognizer(context)
            } catch (_: Exception) {
                _state.value = VoiceInputState.Error(VoiceError.RecognizerUnavailable)
                return@post
            }

            speechRecognizer = recognizer

            val langTag = try {
                locale.bcp47Tag
            } catch (_: Exception) {
                Locale.getDefault().toLanguageTag()
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, langTag)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, langTag)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            }

            recognizer.setRecognitionListener(createListener(locale))
            _state.value = VoiceInputState.Preparing

            try {
                recognizer.startListening(intent)
            } catch (_: Exception) {
                _state.value = VoiceInputState.Error(VoiceError.Unknown("Speech start error"))
            }
        }
    }

    override fun stop() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
                _state.value = VoiceInputState.Processing
            } catch (_: Exception) {}
        }
    }

    override fun cancel() {
        mainHandler.post {
            try {
                speechRecognizer?.cancel()
                _state.value = VoiceInputState.Idle
                mainHandler.post {
                    _events.tryEmit(VoiceInputEvent.Cancelled)
                }
            } catch (_: Exception) {}
        }
    }

    override fun release() {
        mainHandler.post {
            try {
                speechRecognizer?.destroy()
                speechRecognizer = null
                _state.value = VoiceInputState.Idle
            } catch (_: Exception) {}
        }
    }

    private fun createListener(locale: VoiceLocale): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _state.value = VoiceInputState.Listening(0f, "")
                retryCount = 0
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {
                val raw = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                currentAmplitude = if (raw > currentAmplitude) {
                    raw
                } else {
                    currentAmplitude * 0.7f + raw * 0.3f
                }
                val currentPartial = (_state.value as? VoiceInputState.Listening)?.partialText ?: ""
                _state.value = VoiceInputState.Listening(currentAmplitude, currentPartial)
            }

            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                _state.value = VoiceInputState.Processing
            }

            override fun onError(error: Int) {
                if ((error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT || error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) && retryCount < 2) {
                    retryCount++
                    mainHandler.postDelayed({ start(locale) }, 250)
                    return
                }

                val voiceError = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> VoiceError.NoMatch
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> VoiceError.NoSpeech
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> VoiceError.Busy
                    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> VoiceError.Network
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> VoiceError.PermissionDenied
                    SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED -> VoiceError.LanguageUnavailable
                    else -> VoiceError.Unknown("Error code: $error")
                }
                _state.value = VoiceInputState.Error(voiceError)
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: ""
                if (text.isNotBlank()) {
                    _events.tryEmit(VoiceInputEvent.FinalResult(text))
                    _state.value = VoiceInputState.Idle
                } else if (retryCount < 2) {
                    retryCount++
                    mainHandler.postDelayed({ start(locale) }, 250)
                } else {
                    _state.value = VoiceInputState.Error(VoiceError.NoMatch)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val partial = matches?.firstOrNull() ?: ""
                _state.value = VoiceInputState.Listening(currentAmplitude, partial)
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }
}
