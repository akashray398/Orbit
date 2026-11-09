package com.orbit.app.core.domain.usecase

import com.orbit.app.core.domain.model.ConfirmationDecision
import com.orbit.app.core.domain.model.ConfirmationGate
import com.orbit.app.core.domain.model.PendingAction
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

data class PendingConfirmation(
    val action: PendingAction,
    val deferred: CompletableDeferred<ConfirmationDecision>
)

interface PendingConfirmationSource {
    val pendingConfirmation: StateFlow<PendingConfirmation?>
}

@Singleton
class UiConfirmationGate @Inject constructor() : ConfirmationGate, PendingConfirmationSource {

    private val _pendingConfirmation = MutableStateFlow<PendingConfirmation?>(null)
    override val pendingConfirmation: StateFlow<PendingConfirmation?> = _pendingConfirmation.asStateFlow()

    override suspend fun request(action: PendingAction): ConfirmationDecision {
        val deferred = CompletableDeferred<ConfirmationDecision>()
        val pending = PendingConfirmation(action, deferred)

        _pendingConfirmation.value = pending

        val decision = withTimeoutOrNull(60000L) {
            deferred.await()
        } ?: ConfirmationDecision.TimedOut

        _pendingConfirmation.value = null
        return decision
    }

    fun approveCurrent() {
        _pendingConfirmation.value?.deferred?.complete(ConfirmationDecision.Approved)
    }

    fun denyCurrent() {
        _pendingConfirmation.value?.deferred?.complete(ConfirmationDecision.Denied)
    }
}
