package com.orbit.app.core.domain.usecase

import com.orbit.app.core.domain.model.PendingAction
import com.orbit.app.core.domain.model.RiskLevel
import com.orbit.app.core.domain.model.SensitiveAppCatalog
import com.orbit.app.core.domain.model.Tool
import com.orbit.app.core.domain.model.ToolArguments
import javax.inject.Inject
import javax.inject.Singleton

sealed interface PolicyDecision {
    data object Allow : PolicyDecision
    data class RequireConfirmation(val reason: String) : PolicyDecision
    data class Block(val reason: String) : PolicyDecision
}

@Singleton
class ActionPolicyEvaluator @Inject constructor() {

    fun evaluate(
        tool: Tool,
        args: ToolArguments,
        touchedUntrustedData: Boolean
    ): PolicyDecision {
        val rawArgs = args.jsonObject.toString()

        if (SensitiveAppCatalog.isBlocked(rawArgs) || SensitiveAppCatalog.isBlocked(tool.spec.name)) {
            return PolicyDecision.Block("Operation involves financial or credential sensitive application.")
        }

        if (touchedUntrustedData && tool.riskLevel != RiskLevel.Low) {
            return PolicyDecision.RequireConfirmation("Agent read outside untrusted content right before this action.")
        }

        return when (tool.riskLevel) {
            RiskLevel.Low -> PolicyDecision.Allow
            RiskLevel.Medium -> PolicyDecision.RequireConfirmation("Medium risk system modification.")
            RiskLevel.High -> PolicyDecision.RequireConfirmation("High risk action requires confirmation.")
        }
    }
}
