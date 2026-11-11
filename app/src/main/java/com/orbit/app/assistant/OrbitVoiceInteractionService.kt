package com.orbit.app.assistant

import android.service.voice.VoiceInteractionService

class OrbitVoiceInteractionService : VoiceInteractionService() {

    override fun onReady() {
        super.onReady()
        setDisabledShowContext(1 or 2)
    }
}
