package com.orbit.app.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ToolArgumentsTest {

    @Test
    fun toolArguments_parses_valid_json() {
        val jsonStr = "{\"title\":\"DSA Sheet\",\"durationSeconds\":600,\"skipUi\":true}"
        val args = ToolArguments.fromJsonString(jsonStr)

        assertEquals("DSA Sheet", args.getString("title"))
        assertEquals(600, args.getInt("durationSeconds"))
        assertEquals(true, args.getBoolean("skipUi"))
    }

    @Test(expected = ToolArgumentException::class)
    fun toolArguments_throws_exception_on_missing_required_string() {
        val jsonStr = "{\"durationSeconds\":600}"
        val args = ToolArguments.fromJsonString(jsonStr)
        args.getString("title")
    }
}
