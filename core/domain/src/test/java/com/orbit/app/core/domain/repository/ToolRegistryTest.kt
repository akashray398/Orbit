package com.orbit.app.core.domain.repository

import com.orbit.app.core.domain.tools.EchoDebugTool
import com.orbit.app.core.domain.tools.GetCurrentTimeTool
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ToolRegistryTest {

    @Test
    fun toolRegistry_registers_and_retrieves_tools() {
        val registry = ToolRegistry(setOf(GetCurrentTimeTool(), EchoDebugTool()))
        val specs = registry.getAvailableToolSpecs()

        assertEquals(2, specs.size)
        assertNotNull(registry.findTool("get_current_time"))
        assertNotNull(registry.findTool("echo_debug"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun toolRegistry_rejects_non_snake_case_names() {
        val invalidTool = object : com.orbit.app.core.domain.model.Tool {
            override val spec = com.orbit.app.core.domain.model.ToolSpec("InvalidName", "Test", "{}")
            override val riskLevel = com.orbit.app.core.domain.model.RiskLevel.Low
            override val displayLabel = "Test"
            override suspend fun execute(args: com.orbit.app.core.domain.model.ToolArguments, ctx: com.orbit.app.core.domain.model.ToolContext): com.orbit.app.core.domain.model.ToolResult = com.orbit.app.core.domain.model.ToolResult.Success("OK")
            override fun describeAction(args: com.orbit.app.core.domain.model.ToolArguments) = "Test"
        }
        ToolRegistry(setOf(invalidTool))
    }
}
