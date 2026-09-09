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

            val correctedType =
                learnedPattern.transactionType
                    ?.let { value ->
                        parseTransactionType(value)
                    }
                    ?: parsed.type

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
            return Decision(
                ignored = false,
                parsed = parsed.copy(
                    bankName = learnedBank ?: parsed.bankName,
                    type = learnedType ?: parsed.type,
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
