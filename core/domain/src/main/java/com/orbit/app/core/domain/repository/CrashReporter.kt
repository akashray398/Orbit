package com.orbit.app.core.domain.repository

interface CrashReporter {
    fun logError(throwable: Throwable, tag: String? = null)
    fun setCustomKey(key: String, value: String)
}
