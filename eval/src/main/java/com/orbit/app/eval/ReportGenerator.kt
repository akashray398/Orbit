package com.orbit.app.eval

import java.io.File

object ReportGenerator {

    fun generateReport(results: List<EvalResult>, outputPath: String = "eval/REPORT.md") {
        val total = results.size
        val passed = results.count { it.passed }
        val overallRate = String.format("%.1f%%", (passed.toFloat() / total) * 100)

        val categories = results.groupBy { it.category }

        val sb = StringBuilder()
        sb.append("# Orbit AI Agent Evaluation Report\n\n")
        sb.append("## Overall Metrics\n")
        sb.append("- **Total Cases**: $total\n")
        sb.append("- **Passed**: $passed / $total\n")
        sb.append("- **Success Rate**: $overallRate\n\n")

        sb.append("## Category Breakdown\n")
        sb.append("| Category | Total | Passed | Pass Rate |\n")
        sb.append("| --- | --- | --- | --- |\n")

        categories.forEach { (cat, list) ->
            val catTotal = list.size
            val catPassed = list.count { it.passed }
            val catRate = String.format("%.1f%%", (catPassed.toFloat() / catTotal) * 100)
            sb.append("| $cat | $catTotal | $catPassed | $catRate |\n")
        }

        sb.append("\n## Detailed Results\n")
        results.forEach { res ->
            val status = if (res.passed) "PASS" else "FAIL"
            sb.append("- `[${res.caseId}]` **$status** (${res.category}): ${res.reason}\n")
        }

        val file = File(outputPath)
        file.parentFile?.mkdirs()
        file.writeText(sb.toString())
    }
}
