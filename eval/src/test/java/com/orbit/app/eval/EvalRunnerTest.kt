package com.orbit.app.eval

import org.junit.Assert.assertTrue
import org.junit.Test

class EvalRunnerTest {

    @Test
    fun runBenchmarkSuite_generates_report() {
        val stream = javaClass.classLoader?.getResourceAsStream("dataset.json")
        val jsonText = stream?.bufferedReader()?.use { it.readText() } ?: ""
        assertTrue(jsonText.isNotEmpty())

        val results = EvalRunner.runEvaluation(jsonText)
        assertTrue(results.isNotEmpty())

        ReportGenerator.generateReport(results, "eval/REPORT.md")
    }
}
