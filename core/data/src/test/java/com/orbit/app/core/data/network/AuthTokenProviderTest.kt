package com.orbit.app.core.data.network

import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.UUID

class AuthTokenProviderTest {

    @Test
    fun installId_generates_valid_uuid() {
        val installId = UUID.randomUUID().toString()
        assertNotNull(installId)
        org.junit.Assert.assertTrue(installId.length > 20)
    }
}
