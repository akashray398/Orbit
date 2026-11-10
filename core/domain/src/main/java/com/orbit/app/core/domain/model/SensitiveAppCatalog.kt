package com.orbit.app.core.domain.model

object SensitiveAppCatalog {
    val blockedKeywords = listOf(
        "bank", "upi", "wallet", "paytm", "gpay", "phonepe", "pass", "vault",
        "keepass", "bitwarden", "authenticator", "1password", "dashlane", "delete all"
    )

    fun isBlocked(input: String): Boolean {
        val lower = input.lowercase()
        return blockedKeywords.any { lower.contains(it) }
    }
}
