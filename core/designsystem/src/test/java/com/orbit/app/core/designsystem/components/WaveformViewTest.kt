package com.orbit.app.core.designsystem.components

import org.junit.Assert.assertEquals
import org.junit.Test

class WaveformViewTest {

    @Test
    fun waveform_amplitude_coercion_bounds_correctly() {
        val rawOverbound = 1.45f
        val rawUnderbound = -0.3f

        val coercedOver = rawOverbound.coerceIn(0f, 1f)
        val coercedUnder = rawUnderbound.coerceIn(0f, 1f)

        assertEquals(1.0f, coercedOver, 0.001f)
        assertEquals(0.0f, coercedUnder, 0.001f)
    }
}
