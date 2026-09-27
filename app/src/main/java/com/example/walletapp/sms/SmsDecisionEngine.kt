package com.example.walletapp.sms

import android.content.Context
import com.example.walletapp.data.TransactionType

/**
 * Central decision engine for bank SMS processing.
 *
 * Responsibilities:
 * - Applies sender-level ignore rules.
 * - Applies sender + message-pattern ignore rules.
 * - Applies learned sender/pattern corrections.
 * - Requires a trusted built-in sender or an explicit user-learned sender
 *   before a parsed SMS can continue toward transaction creation.
 * - Falls back to the existing BankSmsParser for transaction extraction.
 *
 * This class does NOT create transactions.
 */
object SmsDecisionEngine {

    data class Decision(
        val ignored: Boolean,
        val parsed: ParsedBankSms? = null,
        val matchedPattern: String? = null,
    )

    fun decide(
        context: Context,
        sender: String?,
        body: String,
    ): Decision {
        SmsLearningManager.attachContext(context)
        SmsIgnoreManager.attachContext(context)

        if (sender.isNullOrBlank() || body.isBlank()) {
            return Decision(ignored = true)
        }

        if (SmsIgnoreManager.isSenderIgnored(sender)) {
            return Decision(ignored = true)
        }

        val ignoredPattern =
            SmsIgnoreManager.findIgnoredPattern(
                sender = sender,
                messageBody = body,
            )

        if (ignoredPattern != null) {
            return Decision(
                ignored = true,
                matchedPattern = ignoredPattern.pattern,
            )
        }

        val builtInBank = BankSenderRegistry.lookup(sender)
        val learnedBank = SmsLearningManager.getSenderBank(sender)

        /*
         * Trust boundary:
         * A sender that is neither in the built-in registry nor explicitly
         * learned by the user must never create a financial transaction.
         *
         * This intentionally happens BEFORE parsing so a generic SMS that
         * merely contains words such as "واریز" and a number cannot reach
         * TransactionStore.
         */
        if (builtInBank == null && learnedBank == null) {
            return Decision(
                ignored = true,
            )
        }

        val parsed = BankSmsParser.parse(
            sender = sender,
            body = body,
        ) ?: return Decision(
            ignored = false,
            parsed = null,
        )

        val learnedPattern =
            SmsLearningManager.findPatternRule(
                sender = sender,
                messageBody = body,
            )

        if (learnedPattern != null) {
            val correctedBank =
                learnedPattern.bankName
                    ?.takeIf { it.isNotBlank() }
                    ?: learnedBank
                    ?: builtInBank
                    ?: parsed.bankName

            val correctedType =
                if (hasStrongTransactionTypeEvidence(body)) {
                    parsed.type
                } else {
                    learnedPattern.transactionType
                        ?.let { value -> parseTransactionType(value) }
                        ?: parsed.type
                }

            return Decision(
                ignored = false,
                parsed = parsed.copy(
                    bankName = correctedBank,
                    type = correctedType,
                ),
                matchedPattern = learnedPattern.pattern,
            )
        }

        val learnedType =
            SmsLearningManager.getSenderTransactionType(sender)
                ?.let { value -> parseTransactionType(value) }

        if (learnedBank != null || learnedType != null) {
            val correctedType =
                if (hasStrongTransactionTypeEvidence(body)) {
                    parsed.type
                } else {
                    learnedType ?: parsed.type
                }

            return Decision(
                ignored = false,
                parsed = parsed.copy(
                    bankName = learnedBank ?: builtInBank ?: parsed.bankName,
                    type = correctedType,
                ),
            )
        }

        return Decision(
            ignored = false,
            parsed = parsed.copy(
                bankName = builtInBank ?: parsed.bankName,
            ),
        )
    }

    private fun hasStrongTransactionTypeEvidence(
        body: String,
    ): Boolean {
        val normalized =
            body
                .replace('\u064A', '\u06CC')
                .replace('\u0643', '\u06A9')
                .replace('\u200C', ' ')
                .replace(Regex("\\s+"), " ")
                .trim()

        val explicitSignRegex =
            Regex(
                """(?:^|[^\d۰-۹٠-٩])([+-])\s*[0-9۰-۹٠-٩]{1,3}(?:[,،٬][0-9۰-۹٠-٩]{3})*(?:[+-])?(?=\D|$)|[0-9۰-۹٠-٩]{1,3}(?:[,،٬][0-9۰-۹٠-٩]{3})*\s*([+-])"""
            )

        if (explicitSignRegex.containsMatchIn(normalized)) {
            return true
        }

        val strongIncomeKeywords =
            listOf(
                "واریز",
                "افزایش موجودی",
                "بازگشت وجه",
                "واریز به حساب",
                "دریافت پل",
                "به حساب شما نشست",
            )

        val strongExpenseKeywords =
            listOf(
                "برداشت",
                "خرید",
                "کسر",
                "پرداخت",
                "انتقال از",
                "حواله",
            )

        return strongIncomeKeywords.any { normalized.contains(it) } ||
            strongExpenseKeywords.any { normalized.contains(it) }
    }

    private fun parseTransactionType(
        value: String,
    ): TransactionType? {
        return when (value.trim().uppercase()) {
            "INCOME" -> TransactionType.INCOME
            "EXPENSE" -> TransactionType.EXPENSE
            "درآمد" -> TransactionType.INCOME
            "هزینه" -> TransactionType.EXPENSE
            else -> null
        }
    }
}
