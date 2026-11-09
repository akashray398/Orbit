package com.orbit.app.core.domain.usecase

import com.orbit.app.core.domain.model.Redactor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OutboundGuard @Inject constructor() {

    fun sanitizePayload(payload: String): String {
        return Redactor.redact(payload)
    }
}
