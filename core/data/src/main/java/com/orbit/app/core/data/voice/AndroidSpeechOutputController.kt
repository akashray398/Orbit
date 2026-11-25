package com.orbit.app.core.data.voice

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.orbit.app.core.domain.model.VoiceLocale
import com.orbit.app.core.domain.repository.SpeechOutputController
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidSpeechOutputController @Inject constructor(
    @ApplicationContext private val context: Context
) : SpeechOutputController, TextToSpeech.OnInitListener {

    private val _isSpeaking = MutableStateFlow(false)
    override val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var focusRequest: AudioFocusRequest? = null

    private var pendingSpeakText: String? = null
    private var pendingLocale: VoiceLocale? = null

    init {
        tts = try {
            TextToSpeech(context, this, "com.google.android.tts")
        } catch (_: Exception) {
            TextToSpeech(context, this)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            try {
                tts?.language = Locale.US
            } catch (_: Exception) {}

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    abandonAudioFocus()
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    abandonAudioFocus()
                }
            })

            val text = pendingSpeakText
            val loc = pendingLocale
            if (text != null && loc != null) {
                pendingSpeakText = null
                pendingLocale = null
                speak(text, loc)
            }
        }
    }

    override fun speak(text: String, locale: VoiceLocale) {
        val cleanText = stripMarkdown(text)
        if (cleanText.isEmpty()) return

        if (!isInitialized || tts == null) {
            pendingSpeakText = cleanText
            pendingLocale = locale
            return
        }

        requestAudioFocus()
        val javaLocale = try {
            Locale.forLanguageTag(locale.bcp47Tag)
        } catch (_: Exception) {
            Locale.US
        }

        val setRes = try {
            tts?.setLanguage(javaLocale)
        } catch (_: Exception) {
            TextToSpeech.LANG_NOT_SUPPORTED
        }

        if (setRes == TextToSpeech.LANG_MISSING_DATA || setRes == TextToSpeech.LANG_NOT_SUPPORTED) {
            try {
                tts?.language = Locale.US
            } catch (_: Exception) {}
        }

        val params = Bundle().apply {
            putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC)
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
        }
        val utteranceId = "orbit_tts_${System.currentTimeMillis()}"

        try {
            tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        } catch (_: Exception) {}
    }

    override fun stop() {
        try {
            tts?.stop()
        } catch (_: Exception) {}
        pendingSpeakText = null
        pendingLocale = null
        _isSpeaking.value = false
        abandonAudioFocus()
    }

    override fun isLanguageAvailable(locale: VoiceLocale): Boolean {
        if (!isInitialized || tts == null) return false
        val result = try {
            tts?.isLanguageAvailable(Locale.forLanguageTag(locale.bcp47Tag))
        } catch (_: Exception) {
            TextToSpeech.LANG_NOT_SUPPORTED
        }
        return result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
    }

    override fun release() {
        stop()
        try {
            tts?.shutdown()
        } catch (_: Exception) {}
        tts = null
        isInitialized = false
    }

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()

            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(playbackAttributes)
                .build()

            focusRequest = request
            audioManager.requestAudioFocus(request)
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        }
    }

    private fun stripMarkdown(input: String): String {
        return input
            .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
            .replace(Regex("`(.*?)`"), "$1")
            .replace(Regex("[-*]\\s+"), "")
            .trim()
    }
}
