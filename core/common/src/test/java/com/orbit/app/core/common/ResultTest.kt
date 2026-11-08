package com.orbit.app.core.common

import com.orbit.app.core.common.result.Result
import com.orbit.app.core.common.result.map
import com.orbit.app.core.common.result.onError
import com.orbit.app.core.common.result.onSuccess
import com.orbit.app.core.common.result.safeCall
import com.orbit.app.core.domain.model.AppError
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResultTest {

    @Test
    fun map_transforms_success_value() {
        val result: Result<Int> = Result.Success(5)
        val mapped = result.map { it * 2 }
        assertTrue(mapped is Result.Success)
        assertEquals(10, (mapped as Result.Success).data)
    }

    @Test
    fun onSuccess_triggers_for_success() {
        var triggered = false
        val result: Result<String> = Result.Success("Orbit")
        result.onSuccess { data ->
            triggered = true
            assertEquals("Orbit", data)
        }
        assertTrue(triggered)
    }

    @Test
    fun onError_triggers_for_error() {
        var triggered = false
        val exception = RuntimeException("Failed")
        val result: Result<String> = Result.Error(exception)
        result.onError { err, appError ->
            triggered = true
            assertEquals(exception, err)
            assertTrue(appError is AppError.Unknown)
        }
        assertTrue(triggered)
    }

    @Test
    fun safeCall_returns_success_when_block_succeeds() = runTest {
        val result = safeCall { "Orbit Success" }
        assertTrue(result is Result.Success)
        assertEquals("Orbit Success", (result as Result.Success).data)
    }

    @Test
    fun safeCall_catches_exception_and_returns_error() = runTest {
        val result = safeCall { throw IllegalStateException("Call failed") }
        assertTrue(result is Result.Error)
        val errorResult = result as Result.Error
        assertEquals("Call failed", errorResult.exception.message)
    }
}
