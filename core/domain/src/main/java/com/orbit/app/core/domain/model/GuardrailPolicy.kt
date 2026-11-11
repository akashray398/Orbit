package com.orbit.app.core.domain.model

data class GuardrailRule(
    val id: String,
    val description: String,
    val enforcingLayer: String
)

object GuardrailPolicy {
    val rules = listOf(
        GuardrailRule("RULE_01", "Financial, payment, and credential apps are strictly blocked.", "Security & Tool Layer"),
        GuardrailRule("RULE_02", "Medium and High risk actions require explicit user confirmation.", "Confirmation Gate"),
        GuardrailRule("RULE_03", "Untrusted content (notifications/web) escalates confirmation rules.", "Policy Evaluator"),
        GuardrailRule("RULE_04", "Sensitive data patterns (card/OTP) are redacted before network egress.", "Outbound Guard"),
        GuardrailRule("RULE_05", "Max 8 tool execution steps per agent run.", "Agent Engine Loop"),
        GuardrailRule("RULE_06", "Assistant mode session never captures screen content and blocks private tools when phone is locked.", "Assistant Keyguard Policy")
    )
}
