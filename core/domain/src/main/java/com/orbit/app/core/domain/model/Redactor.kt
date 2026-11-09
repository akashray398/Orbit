package com.orbit.app.core.domain.model

object Redactor {
    private val EMAIL_REGEX = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
    private val PHONE_REGEX = Regex("(\\+?\\d{1,3}[- .]?)?\\(?\\d{3}\\)?[- .]?\\d{3}[- .]?\\d{4}")
    private val CARD_REGEX = Regex("\\b(?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14}|3[47][0-9]{13})\\b")
    private val OTP_TOKEN_REGEX = Regex("\\b\\d{6}\\b")

    fun redact(input: String): String {
        return input
            .replace(EMAIL_REGEX, "[REDACTED_EMAIL]")
            .replace(PHONE_REGEX, "[REDACTED_PHONE]")
            .replace(CARD_REGEX, "[REDACTED_CARD]")
            .replace(OTP_TOKEN_REGEX, "[REDACTED_OTP]")
    }
}
