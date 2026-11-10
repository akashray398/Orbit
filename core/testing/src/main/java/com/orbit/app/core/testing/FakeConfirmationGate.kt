package com.orbit.app.core.testing

import com.orbit.app.core.domain.model.ConfirmationDecision
import com.orbit.app.core.domain.model.ConfirmationGate
import com.orbit.app.core.domain.model.PendingAction

class FakeConfirmationGate(
    var defaultDecision: ConfirmationDecision = ConfirmationDecision.Approved
) : ConfirmationGate {

    val requestedActions = mutableListOf<PendingAction>()

    override suspend fun request(action: PendingAction): ConfirmationDecision {
        requestedActions.add(action)
        return defaultDecision
    }
}
