package com.orbit.app.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class RedactorTest {

    @Test
    fun redactor_masks_email_addresses() {
        val input = "Contact user@example.com for details"
        val result = Redactor.redact(input)
        assertEquals("Contact [REDACTED_EMAIL] for details", result)
    }

    @Test
    fun redactor_masks_phone_numbers() {
        val input = "Call me at +1 555-123-4567 today"
        val result = Redactor.redact(input)
        assertEquals("Call me at [REDACTED_PHONE] today", result)
    }

    @Test
    fun redactor_masks_otp_codes() {
        val input = "Your verification OTP is 654321"
        val result = Redactor.redact(input)
        assertEquals("Your verification OTP is [REDACTED_OTP]", result)
    }

    @Test
    fun redactor_preserves_dates_and_times() {
        val input = "Meeting on 2025-11-12 at 08:30 AM"
        val result = Redactor.redact(input)
        assertEquals("Meeting on 2025-11-12 at 08:30 AM", result)
    }
}
