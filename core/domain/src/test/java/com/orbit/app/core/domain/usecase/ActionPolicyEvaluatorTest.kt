package com.orbit.app.core.domain.usecase

import com.orbit.app.core.domain.model.ToolArguments
import com.orbit.app.core.domain.tools.GetCurrentTimeTool
import org.junit.Assert.assertTrue
import org.junit.Test

class ActionPolicyEvaluatorTest {

    private val evaluator = ActionPolicyEvaluator()

    @Test
    fun evaluator_allows_low_risk_read_only_tools() {
        val tool = GetCurrentTimeTool()
        val args = ToolArguments.fromJsonString("{}")
        val decision = evaluator.evaluate(tool, args, touchedUntrustedData = false)
        assertTrue(decision is PolicyDecision.Allow)
    }

    @Test
    fun evaluator_blocks_financial_app_keywords() {
        val tool = GetCurrentTimeTool()
        val args = ToolArguments.fromJsonString("{\"appName\":\"my_bank_app\"}")
        val decision = evaluator.evaluate(tool, args, touchedUntrustedData = false)
        assertTrue(decision is PolicyDecision.Block)
    }
}
