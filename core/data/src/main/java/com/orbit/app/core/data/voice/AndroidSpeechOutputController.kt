package com.orbit.app.core.data.voice

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
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

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
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
        }
    }

    override fun speak(text: String, locale: VoiceLocale) {
        if (!isInitialized || tts == null) return

        val cleanText = stripMarkdown(text)
        if (cleanText.isEmpty()) return

        requestAudioFocus()
        val javaLocale = Locale.forLanguageTag(locale.bcp47Tag)
        tts?.language = javaLocale

        val utteranceId = "orbit_tts_${System.currentTimeMillis()}"
        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    override fun stop() {
        tts?.stop()
        _isSpeaking.value = false
        abandonAudioFocus()
    }

    override fun isLanguageAvailable(locale: VoiceLocale): Boolean {
        if (!isInitialized || tts == null) return false
        val result = tts?.isLanguageAvailable(Locale.forLanguageTag(locale.bcp47Tag))
        return result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
    }

    override fun release() {
        stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANT)
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
