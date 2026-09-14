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
 * - Falls back to the existing BankSmsParser.
 *
 * This class does NOT create transactions.
 * Receiver and InboxScanner can both use the same decision logic safely.
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
        SmsLearningStore.attachContext(context)
        SmsIgnoreStore.attachContext(context)

        if (sender.isNullOrBlank() || body.isBlank()) {
            return Decision(
                ignored = true,
            )
        }

        // -------------------------------------------------------------
        // 1. Complete sender-level ignore
        // -------------------------------------------------------------
        if (SmsIgnoreStore.isSenderIgnored(sender)) {
            return Decision(
                ignored = true,
            )
        }

        // -------------------------------------------------------------
        // 2. Sender + message-pattern ignore
        // -------------------------------------------------------------
        val ignoredPattern =
            SmsIgnoreStore.findIgnoredPattern(
                sender = sender,
                messageBody = body,
            )

        if (ignoredPattern != null) {
            return Decision(
                ignored = true,
                matchedPattern = ignoredPattern.pattern,
            )
        }

        // -------------------------------------------------------------
        // 3. Parse using the existing stable parser
        // -------------------------------------------------------------
        val parsed = BankSmsParser.parse(
            sender = sender,
            body = body,
        ) ?: return Decision(
            ignored = false,
            parsed = null,
        )

        // -------------------------------------------------------------
        // 4. Apply learned pattern rule first
        //
        // Pattern rules are more specific than sender-wide rules.
        // -------------------------------------------------------------
        val learnedPattern =
            SmsLearningStore.findPatternRule(
                sender = sender,
                messageBody = body,
            )

        if (learnedPattern != null) {
            val correctedBank =
                learnedPattern.bankName
                    ?.takeIf { it.isNotBlank() }
                    ?: parsed.bankName

            /*
             * Transaction type safety:
             *
             * Strong evidence already parsed from the SMS must beat
             * stale learned type rules.
             *
             * Explicit +/- signs and clear income/expense keywords
             * are considered strong evidence.
             */
            val correctedType =
                if (hasStrongTransactionTypeEvidence(body)) {
                    parsed.type
                } else {
                    learnedPattern.transactionType
                        ?.let { value ->
                            parseTransactionType(value)
                        }
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

        // -------------------------------------------------------------
        // 5. Apply learned sender-wide rules
        // -------------------------------------------------------------
        val learnedBank =
            SmsLearningStore.getSenderBank(sender)

        val learnedType =
            SmsLearningStore.getSenderTransactionType(sender)
                ?.let { value ->
                    parseTransactionType(value)
                }

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
                    bankName = learnedBank ?: parsed.bankName,
                    type = correctedType,
                ),
            )
        }

        // -------------------------------------------------------------
        // 6. No learned correction; use parser result unchanged.
        // -------------------------------------------------------------
        return Decision(
            ignored = false,
            parsed = parsed,
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

        /*
         * Explicit signed amount:
         *   +200,000
         *   -200,000
         *   200,000+
         *   200,000-
         */
        val explicitSignRegex =
            Regex(
                """(?:^|[^\d۰-۹٠-٩])([+-])\s*[0-9۰-۹٠-٩]{1,3}(?:[,،٬][0-9۰-۹٠-٩]{3})*(?:[+-])?(?=\D|$)|[0-9۰-۹٠-٩]{1,3}(?:[,،٬][0-9۰-۹٠-٩]{3})*\s*([+-])"""
            )

        if (explicitSignRegex.containsMatchIn(normalized)) {
            return true
        }

        val strongIncomeKeywords =
            listOf(
                "\u0648\u0627\u0631\u06cc\u0632",
                "\u0627\u0641\u0632\u0627\u06cc\u0634 \u0645\u0648\u062c\u0648\u062f\u06cc",
                "\u0628\u0627\u0632\u06af\u0634\u062a \u0648\u062c\u0647",
                "\u0648\u0627\u0631\u06cc\u0632 \u0628\u0647 \u062d\u0633\u0627\u0628",
                "\u062f\u0631\u06cc\u0627\u0641\u062a \u067e\u0644",
                "\u0628\u0647 \u062d\u0633\u0627\u0628 \u0634\u0645\u0627 \u0646\u0634\u0633\u062a",
            )

        val strongExpenseKeywords =
            listOf(
                "\u0628\u0631\u062f\u0627\u0634\u062a",
                "\u062e\u0631\u06cc\u062f",
                "\u06a9\u0633\u0631",
                "\u067e\u0631\u062f\u0627\u062e\u062a",
                "\u0627\u0646\u062a\u0642\u0627\u0644 \u0627\u0632",
                "\u062d\u0648\u0627\u0644\u0647",
            )

        return strongIncomeKeywords.any { normalized.contains(it) } ||
            strongExpenseKeywords.any { normalized.contains(it) }
    }

    private fun parseTransactionType(
        value: String,
    ): TransactionType? {
        return when (
            value.trim().uppercase()
        ) {
            "INCOME" -> TransactionType.INCOME
            "EXPENSE" -> TransactionType.EXPENSE
            "درآمد" -> TransactionType.INCOME
            "هزینه" -> TransactionType.EXPENSE
            else -> null
        }
    }
}

