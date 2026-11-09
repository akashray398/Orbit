package com.orbit.app.core.domain.repository

import com.orbit.app.core.domain.model.Tool
import com.orbit.app.core.domain.model.ToolSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToolRegistry @Inject constructor(
    toolsSet: Set<@JvmSuppressWildcards Tool>
) {
    private val toolsMap: Map<String, Tool> = toolsSet.associateBy { it.spec.name }

    init {
        toolsMap.forEach { (name, tool) ->
            require(name.isNotBlank() && name.matches(Regex("^[a-z0-9_]+$"))) {
                "Tool name '$name' must be non-empty snake_case."
            }
            require(tool.spec.description.isNotBlank()) {
                "Tool '$name' must have a non-empty description."
            }
        }
    }

    fun getAvailableToolSpecs(): List<ToolSpec> {
        return toolsMap.values.map { it.spec }
    }

    fun findTool(name: String): Tool? {
        return toolsMap[name]
    }
}
