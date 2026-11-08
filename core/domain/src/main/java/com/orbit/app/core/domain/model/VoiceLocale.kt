package com.orbit.app.core.domain.model

enum class VoiceLocale(
    val displayName: String,
    val nativeName: String,
    val bcp47Tag: String
) {
    ENGLISH("English (India)", "English", "en-IN"),
    HINDI("Hindi", "हिन्दी", "hi-IN"),
    PUNJABI("Punjabi", "ਪੰਜਾਬੀ", "pa-IN");

    companion object {
        fun fromBcp47(tag: String): VoiceLocale {
            return entries.find { it.bcp47Tag.equals(tag, ignoreCase = true) } ?: ENGLISH
        }
    }
}
