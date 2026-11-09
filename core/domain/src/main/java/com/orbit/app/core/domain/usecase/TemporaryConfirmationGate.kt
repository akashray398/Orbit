package com.orbit.app.core.domain.usecase

import com.orbit.app.core.domain.model.ConfirmationDecision
import com.orbit.app.core.domain.model.ConfirmationGate
import com.orbit.app.core.domain.model.PendingAction
import com.orbit.app.core.domain.model.RiskLevel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemporaryConfirmationGate @Inject constructor() : ConfirmationGate {

    override suspend fun request(action: PendingAction): ConfirmationDecision {
        return when (action.riskLevel) {
            RiskLevel.Low -> ConfirmationDecision.Approved
            RiskLevel.Medium, RiskLevel.High -> ConfirmationDecision.Denied
        }
    }
}
