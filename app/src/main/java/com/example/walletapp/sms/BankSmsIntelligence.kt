package com.example.walletapp.sms

/**
 * First-stage financial SMS understanding library.
 *
 * This deterministic baseline is deliberately conservative: it classifies
 * message intent, but does not claim that the sender or SMS is authentic.
 * A future on-device model can enrich this result without bypassing these
 * safety checks or the existing transaction-completion flow.
 */
enum class FinancialSmsCategory {
    PURCHASE,
    WITHDRAWAL,
    DEPOSIT,
    TRANSFER,
    REFUND,
    BANK_FEE,
    LOAN_INSTALLMENT,
    LOAN,
    CHEQUE,
    BALANCE_INQUIRY,
    SECURITY_OR_OTP,
    NON_FINANCIAL,
    UNKNOWN,
}

data class FinancialSmsAnalysis(
    val category: FinancialSmsCategory,
    /** Heuristic confidence, not proof of authenticity. Range 0.0..1.0. */
    val confidence: Float,
    val requiresUserReview: Boolean,
    val matchedSignals: List<String>,
)

object BankSmsIntelligence {
    private val securitySignals = listOf(
        "\\u0631\\u0645\\u0632 \\u067e\\u0648\\u06cc\\u0627",
        "\\u06a9\\u062f \\u06cc\\u06a9\\u0628\\u0627\\u0631 \\u0645\\u0635\\u0631\\u0641",
        "otp",
        "one time password",
    )
    private val signals = linkedMapOf(
        FinancialSmsCategory.PURCHASE to listOf("\\u062e\\u0631\\u06cc\\u062f", "\\u067e\\u0630\\u06cc\\u0631\\u0646\\u062f\\u0647", "purchase", "pos"),
        FinancialSmsCategory.WITHDRAWAL to listOf("\\u0628\\u0631\\u062f\\u0627\\u0634\\u062a", "withdrawal", "atm"),
        FinancialSmsCategory.DEPOSIT to listOf("\\u0648\\u0627\\u0631\\u06cc\\u0632", "\\u0648\\u0627\\u0631\\u062f \\u0634\\u062f", "deposit", "credited"),
        FinancialSmsCategory.TRANSFER to listOf("\\u0627\\u0646\\u062a\\u0642\\u0627\\u0644 \\u0648\\u062c\\u0647", "\\u06a9\\u0627\\u0631\\u062a \\u0628\\u0647 \\u06a9\\u0627\\u0631\\u062a", "transfer", "paya", "satna"),
        FinancialSmsCategory.REFUND to listOf("\\u0628\\u0627\\u0632\\u06af\\u0634\\u062a \\u0648\\u062c\\u0647", "\\u0631\\u0641\\u0639 \\u062a\\u0631\\u0627\\u06a9\\u0646\\u0634", "refund", "reversal"),
        FinancialSmsCategory.BANK_FEE to listOf("\\u06a9\\u0627\\u0631\\u0645\\u0632\\u062f", "bank fee", "service fee"),
        FinancialSmsCategory.LOAN_INSTALLMENT to listOf("\\u0642\\u0633\\u0637", "\\u0633\\u0631\\u0631\\u0633\\u06cc\\u062f \\u0642\\u0633\\u0637", "installment", "loan payment"),
        FinancialSmsCategory.LOAN to listOf("\\u0648\\u0627\\u0645", "\\u062a\\u0633\\u0647\\u06cc\\u0644\\u0627\\u062a", "loan", "credit facility"),
        FinancialSmsCategory.CHEQUE to listOf("\\u0686\\u06a9", "\\u0635\\u06cc\\u0627\\u062f", "cheque", "check"),
        FinancialSmsCategory.BALANCE_INQUIRY to listOf("\\u0645\\u0648\\u062c\\u0648\\u062f\\u06cc", "\\u0645\\u0627\\u0646\\u062f\\u0647 \\u062d\\u0633\\u0627\\u0628", "balance"),
    )

    fun analyze(body: String): FinancialSmsAnalysis {
        val normalized = normalize(body)
        if (normalized.isBlank()) return result(FinancialSmsCategory.UNKNOWN, 0.0f, true)

        val securityMatches = securitySignals.filter { normalized.contains(normalize(it)) }
        if (securityMatches.isNotEmpty()) {
            return FinancialSmsAnalysis(
                category = FinancialSmsCategory.SECURITY_OR_OTP,
                confidence = 0.99f,
                requiresUserReview = true,
                matchedSignals = securityMatches,
            )
        }

        val matches = signals.mapNotNull { (category, keywords) ->
            val found = keywords.filter { normalized.contains(normalize(it)) }
            if (found.isEmpty()) null else category to found
        }

        if (matches.isEmpty()) {
            return result(FinancialSmsCategory.UNKNOWN, 0.30f, true)
        }

        // Conflicting intent signals must not be silently converted to a transaction.
        if (matches.size > 1) {
            val categories = matches.map { it.first }.toSet()
            if (categories.size > 1) {
                return FinancialSmsAnalysis(
                    category = FinancialSmsCategory.UNKNOWN,
                    confidence = 0.45f,
                    requiresUserReview = true,
                    matchedSignals = matches.flatMap { it.second }.distinct(),
                )
            }
        }

        val (category, found) = matches.first()
        val hasAmount = Regex("""[0-9\\u06F0-\\u06F9\\u0660-\\u0669]{2,}""").containsMatchIn(normalized)
        val confidence = (0.62f + if (found.size > 1) 0.12f else 0f + if (hasAmount) 0.12f else 0f).coerceAtMost(0.90f)
        return FinancialSmsAnalysis(
            category = category,
            confidence = confidence,
            requiresUserReview = confidence < 0.80f || category in setOf(
                FinancialSmsCategory.LOAN,
                FinancialSmsCategory.LOAN_INSTALLMENT,
                FinancialSmsCategory.CHEQUE,
            ),
            matchedSignals = found,
        )
    }

    private fun result(category: FinancialSmsCategory, confidence: Float, review: Boolean) =
        FinancialSmsAnalysis(category, confidence, review, emptyList())

    private fun normalize(value: String): String = value
        .lowercase()
        .replace('\\u064A', '\\u06CC')
        .replace('\\u0643', '\\u06A9')
        .replace('\\u200C', ' ')
        .replace(Regex("\\s+"), " ")
        .trim()
}
