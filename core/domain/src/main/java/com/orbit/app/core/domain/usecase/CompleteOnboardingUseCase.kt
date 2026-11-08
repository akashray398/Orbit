package com.orbit.app.core.domain.usecase

import com.orbit.app.core.domain.repository.OnboardingRepository
import javax.inject.Inject

class CompleteOnboardingUseCase @Inject constructor(
    private val repository: OnboardingRepository
) {
    suspend operator fun invoke() {
        repository.setOnboardingCompleted(true)
    }
}
