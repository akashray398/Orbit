package com.orbit.app.core.domain.usecase

import com.orbit.app.core.domain.model.OnboardingState
import com.orbit.app.core.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveOnboardingStateUseCase @Inject constructor(
    private val repository: OnboardingRepository
) {
    operator fun invoke(): Flow<OnboardingState> = repository.onboardingState
}
