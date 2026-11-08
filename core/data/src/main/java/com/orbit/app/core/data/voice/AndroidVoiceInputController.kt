package com.orbit.app.core.data.voice

import android.content.Context
import android.content.Intent
import android.os.Build
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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidVoiceInputController @Inject constructor(
    @ApplicationContext private val context: Context
) : VoiceInputController {

    private val _state = MutableStateFlow<VoiceInputState>(VoiceInputState.Idle)
    override val state: StateFlow<VoiceInputState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<VoiceInputEvent>()
    override val events: Flow<VoiceInputEvent> = _events.asSharedFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var currentAmplitude = 0f
    private var busyRetryCount = 0

    override fun start(locale: VoiceLocale) {
        mainHandler.post {
            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                _state.value = VoiceInputState.Error(VoiceError.RecognizerUnavailable)
                return@post
            }

            if (speechRecognizer != null) {
                cancel()
            }

            val recognizer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                SpeechRecognizer.isOnDeviceRecognitionAvailable(context)
            ) {
                SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
            } else {
                SpeechRecognizer.createSpeechRecognizer(context)
            }

            speechRecognizer = recognizer

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.bcp47Tag)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, locale.bcp47Tag)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            }

            recognizer.setRecognitionListener(createListener(locale))
            _state.value = VoiceInputState.Preparing
            recognizer.startListening(intent)
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
                busyRetryCount = 0
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {
                // Normalize dB (-2 .. 10) to 0.0 .. 1.0 with smoothing
                val raw = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                currentAmplitude = if (raw > currentAmplitude) {
                    raw // Fast attack
                } else {
                    currentAmplitude * 0.7f + raw * 0.3f // Slow decay
                }
                val currentPartial = (_state.value as? VoiceInputState.Listening)?.partialText ?: ""
                _state.value = VoiceInputState.Listening(currentAmplitude, currentPartial)
            }

            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                _state.value = VoiceInputState.Processing
            }

            override fun onError(error: Int) {
                val voiceError = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> VoiceError.NoMatch
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> VoiceError.NoSpeech
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> {
                        if (busyRetryCount < 1) {
                            busyRetryCount++
                            mainHandler.postDelayed({ start(locale) }, 300)
                            return
                        }
                        VoiceError.Busy
                    }
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
                _state.value = VoiceInputState.Idle
                _events.tryEmit(VoiceInputEvent.FinalResult(text))
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
