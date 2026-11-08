package com.orbit.app.core.domain.usecase

import com.orbit.app.core.domain.repository.OnboardingRepository
import javax.inject.Inject

class SavePermissionsStepUseCase @Inject constructor(
    private val repository: OnboardingRepository
) {
    suspend fun setPage(page: Int) {
        repository.saveLastCompletedPage(page)
    }

    suspend fun setPermissionAsked(permissionKey: String, asked: Boolean = true) {
        repository.savePermissionAsked(permissionKey, asked)
    }

    suspend fun setPermissionStepSeen(seen: Boolean = true) {
        repository.savePermissionStepSeen(seen)
    }
}
