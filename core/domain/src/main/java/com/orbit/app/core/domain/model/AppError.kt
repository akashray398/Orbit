package com.orbit.app.core.domain.model

sealed class AppError : Throwable() {
    data class Network(
        override val message: String? = null,
        override val cause: Throwable? = null
    ) : AppError()

    data class Unauthorized(
        override val message: String? = null
    ) : AppError()

    data class Permission(
        val permissionName: String? = null,
        override val message: String? = "Permission denied: $permissionName"
    ) : AppError()

    data class Database(
        override val message: String? = null,
        override val cause: Throwable? = null
    ) : AppError()

    data class RateLimited(
        val retryAfterSeconds: Int? = null,
        override val message: String? = "Rate limit exceeded"
    ) : AppError()

    data object BudgetExceeded : AppError()

    data class Timeout(
        override val message: String? = "Request timed out"
    ) : AppError()

    data class ServerUnavailable(
        override val message: String? = "Server unavailable"
    ) : AppError()

    data class InvalidResponse(
        override val message: String? = "Invalid response format"
    ) : AppError()

    data class Unknown(
        override val message: String? = null,
        override val cause: Throwable? = null
    ) : AppError()
}
