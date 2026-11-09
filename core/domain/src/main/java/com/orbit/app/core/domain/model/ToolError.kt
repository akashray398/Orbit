package com.orbit.app.core.domain.model

sealed interface ToolError {
    data object InvalidArguments : ToolError
    data class PermissionMissing(val permission: String) : ToolError
    data object NotFound : ToolError
    data object Unavailable : ToolError
    data class Blocked(val reason: String) : ToolError
    data object Timeout : ToolError
    data object Cancelled : ToolError
    data class Unknown(val message: String = "") : ToolError
}
