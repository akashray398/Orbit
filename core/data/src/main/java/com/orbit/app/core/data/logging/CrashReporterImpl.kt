package com.orbit.app.core.data.logging

import com.orbit.app.core.domain.repository.CrashReporter
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CrashReporterImpl @Inject constructor() : CrashReporter {

    override fun logError(throwable: Throwable, tag: String?) {
        val cleanMsg = throwable.message?.take(100) ?: "Exception"
        Timber.e(throwable, "[CrashReporter] Tag: ${tag ?: "General"} - Error: $cleanMsg")
    }

    override fun setCustomKey(key: String, value: String) {
        Timber.d("[CrashReporter] Key: $key = $value")
    }
}
