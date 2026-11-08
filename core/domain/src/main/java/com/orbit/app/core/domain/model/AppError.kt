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

    data class Unknown(
        override val message: String? = null,
        override val cause: Throwable? = null
    ) : AppError()
}
