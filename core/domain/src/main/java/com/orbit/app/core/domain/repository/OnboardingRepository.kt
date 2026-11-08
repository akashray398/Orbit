package com.orbit.app.core.domain.repository

import com.orbit.app.core.domain.model.OnboardingState
import kotlinx.coroutines.flow.Flow

interface OnboardingRepository {
    val onboardingState: Flow<OnboardingState>
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun saveLastCompletedPage(page: Int)
    suspend fun savePermissionStepSeen(seen: Boolean)
    suspend fun savePermissionAsked(permissionKey: String, asked: Boolean)
    suspend fun resetOnboarding()
}
