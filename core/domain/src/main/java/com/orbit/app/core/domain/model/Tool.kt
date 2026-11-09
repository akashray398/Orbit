package com.orbit.app.core.domain.model

interface Tool {
    val spec: ToolSpec
    val riskLevel: RiskLevel
    val displayLabel: String
    val requiredPermissions: List<String> get() = emptyList()
    val isReadOnly: Boolean get() = false
    val returnsUntrustedContent: Boolean get() = false

    suspend fun execute(args: ToolArguments, ctx: ToolContext): ToolResult
    fun describeAction(args: ToolArguments): String
    suspend fun undo(payload: String): ToolResult? = null
}
