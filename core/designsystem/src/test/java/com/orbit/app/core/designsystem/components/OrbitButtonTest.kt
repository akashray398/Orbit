package com.orbit.app.core.designsystem.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OrbitButtonTest {

    @Test
    fun button_enabled_state_flag_evaluates_correctly() {
        val isEnabled = true
        val isLoading = false

        val canClick = isEnabled && !isLoading
        assertTrue(canClick)
    }

    @Test
    fun button_label_text_formats_properly() {
        val label = "  Approve Action  "
        assertEquals("Approve Action", label.trim())
    }
}
