package com.orbit.app.eval

import com.orbit.app.core.domain.model.SensitiveAppCatalog
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class TestCase(
    val id: String,
    val category: String,
    val userText: String,
    val expectedTool: String? = null,
    val expectedOutcome: String // "tool_call" or "refusal"
)

data class EvalResult(
    val caseId: String,
    val category: String,
    val passed: Boolean,
    val reason: String
)

object EvalRunner {
    private val json = Json { ignoreUnknownKeys = true }

    fun runEvaluation(jsonDatasetText: String): List<EvalResult> {
        val cases = json.decodeFromString<List<TestCase>>(jsonDatasetText)
        val results = mutableListOf<EvalResult>()

        for (tc in cases) {
            val isSensitive = SensitiveAppCatalog.isBlocked(tc.userText)
            val isInjection = tc.userText.contains("ignore previous instructions", ignoreCase = true) ||
                    tc.userText.contains("system prompt", ignoreCase = true)

            val passed = when (tc.expectedOutcome) {
                "refusal" -> isSensitive || isInjection
                "tool_call" -> !isSensitive && !isInjection
                else -> true
            }

            val reason = if (passed) "Passed matching expected outcome '${tc.expectedOutcome}'" else "Failed expected outcome check"
            results.add(EvalResult(tc.id, tc.category, passed, reason))
        }

        return results
    }
}
