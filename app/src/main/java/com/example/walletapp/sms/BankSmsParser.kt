package com.example.walletapp.sms

import com.example.walletapp.data.TransactionType

data class ParsedBankSms(
    val bankName: String,
    val amount: Long?,
    val merchant: String?,
    val type: TransactionType,
    val rawBody: String,
)

/**
 * Parser for common Iranian bank SMS formats.
 *
 * Persian strings are represented with Unicode escapes intentionally
 * so PowerShell/source-file encoding cannot corrupt the parser.
 */
object BankSmsParser {

    private val bankKeywords: List<Pair<String, String>> =
        listOf(
            "\u0645\u0644\u062a" to "\u0628\u0627\u0646\u06a9 \u0645\u0644\u062a",
            "\u0645\u0644\u06cc" to "\u0628\u0627\u0646\u06a9 \u0645\u0644\u06cc",
            "\u0633\u0627\u0645\u0627\u0646" to "\u0628\u0627\u0646\u06a9 \u0633\u0627\u0645\u0627\u0646",
            "\u067e\u0627\u0631\u0633\u06cc\u0627\u0646" to "\u0628\u0627\u0646\u06a9 \u067e\u0627\u0631\u0633\u06cc\u0627\u0646",
            "\u067e\u0627\u0633\u0627\u0631\u06af\u0627\u062f" to "\u0628\u0627\u0646\u06a9 \u067e\u0627\u0633\u0627\u0631\u06af\u0627\u062f",
            "\u0633\u067e\u0647" to "\u0628\u0627\u0646\u06a9 \u0633\u067e\u0647",
            "\u06a9\u0634\u0627\u0648\u0631\u0632\u06cc" to "\u0628\u0627\u0646\u06a9 \u06a9\u0634\u0627\u0648\u0631\u0632\u06cc",
            "\u062a\u062c\u0627\u0631\u062a" to "\u0628\u0627\u0646\u06a9 \u062a\u062c\u0627\u0631\u062a",
            "\u0635\u0627\u062f\u0631\u0627\u062a" to "\u0628\u0627\u0646\u06a9 \u0635\u0627\u062f\u0631\u0627\u062a",
            "\u0631\u0641\u0627\u0647" to "\u0628\u0627\u0646\u06a9 \u0631\u0641\u0627\u0647 \u06a9\u0627\u0631\u06af\u0631\u0627\u0646",
            "\u0627\u0642\u062a\u0635\u0627\u062f \u0646\u0648\u06cc\u0646" to "\u0628\u0627\u0646\u06a9 \u0627\u0642\u062a\u0635\u0627\u062f \u0646\u0648\u06cc\u0646",
            "\u0627\u0646\u0635\u0627\u0631" to "\u0628\u0627\u0646\u06a9 \u0627\u0646\u0635\u0627\u0631",
            "\u0634\u0647\u0631" to "\u0628\u0627\u0646\u06a9 \u0634\u0647\u0631",
            "\u0627\u06cc\u0631\u0627\u0646 \u0632\u0645\u06cc\u0646" to "\u0628\u0627\u0646\u06a9 \u0627\u06cc\u0631\u0627\u0646 \u0632\u0645\u06cc\u0646",
            "\u06af\u0631\u062f\u0634\u06af\u0631\u06cc" to "\u0628\u0627\u0646\u06a9 \u06af\u0631\u062f\u0634\u06af\u0631\u06cc",
            "\u067e\u0633\u062a \u0628\u0627\u0646\u06a9" to "\u067e\u0633\u062a \u0628\u0627\u0646\u06a9",
            "\u06a9\u0627\u0631\u0622\u0641\u0631\u06cc\u0646" to "\u0628\u0627\u0646\u06a9 \u06a9\u0627\u0631\u0622\u0641\u0631\u06cc\u0646",
            "\u0633\u06cc\u0646\u0627" to "\u0628\u0627\u0646\u06a9 \u0633\u06cc\u0646\u0627",
            "\u0645\u0633\u06a9\u0646" to "\u0628\u0627\u0646\u06a9 \u0645\u0633\u06a9\u0646",
            "\u0633\u0631\u0645\u0627\u06cc\u0647" to "\u0628\u0627\u0646\u06a9 \u0633\u0631\u0645\u0627\u06cc\u0647",
            "\u0628\u0644\u0648\u0628\u0627\u0646\u06a9" to "\u0628\u0644\u0648 (\u0628\u0627\u0646\u06a9 \u0622\u06cc\u0646\u062f\u0647)",
            "\u0628\u0644\u0648" to "\u0628\u0644\u0648 (\u0628\u0627\u0646\u06a9 \u0622\u06cc\u0646\u062f\u0647)",
            "\u0628\u0627\u0646\u06a9 \u0622\u06cc\u0646\u062f\u0647" to "\u0628\u0627\u0646\u06a9 \u0622\u06cc\u0646\u062f\u0647",
        )

    private val expenseKeywords =
        listOf(
            "\u0628\u0631\u062f\u0627\u0634\u062a",
            "\u062e\u0631\u06cc\u062f",
            "\u06a9\u0633\u0631",
            "\u067e\u0631\u062f\u0627\u062e\u062a",
            "\u0627\u0646\u062a\u0642\u0627\u0644 \u0627\u0632",
            "\u062d\u0648\u0627\u0644\u0647",
            "\u067e\u0631\u06cc\u062f",
        )

    private val incomeKeywords =
        listOf(
            "\u0648\u0627\u0631\u06cc\u0632",
            "\u0627\u0641\u0632\u0627\u06cc\u0634 \u0645\u0648\u062c\u0648\u062f\u06cc",
            "\u0628\u0627\u0632\u06af\u0634\u062a \u0648\u062c\u0647",
            "\u0648\u0627\u0631\u06cc\u0632 \u0628\u0647 \u062d\u0633\u0627\u0628",

            // Blue / پل incoming transfers
            "\u062f\u0631\u06cc\u0627\u0641\u062a \u067e\u0644",
            "\u0628\u0647 \u062d\u0633\u0627\u0628 \u0634\u0645\u0627 \u0646\u0634\u0633\u062a",
        )

    private val nonTransactionalKeywords =
        listOf(
            "\u062c\u0647\u062a \u0648\u0631\u0648\u062f",
            "\u0648\u0631\u0648\u062f \u0628\u0647 \u0633\u0627\u0645\u0627\u0646\u0647",
            "\u0631\u0645\u0632 \u067e\u0648\u06cc\u0627",
            "\u06a9\u062f \u06cc\u06a9\u0628\u0627\u0631 \u0645\u0635\u0631\u0641",
            "\u0634\u0646\u0627\u0633\u0647 \u062a\u0627\u06cc\u06cc\u062f \u0628\u0631\u062f\u0627\u0634\u062a",
            "\u0628\u0644\u06cc\u0637 \u0627\u0645\u0646\u06cc\u062a\u06cc \u0627\u0646\u062a\u0642\u0627\u0644 \u0648\u062c\u0647 \u0622\u0646\u06cc",
        )

    /*
     * Supports:
     *   -200,000,000
     *   +200,000,000
     *   200,000,000-
     *   200,000,000+
     * and normal unsigned amounts.
     */
    private val signedAmountRegex =
        Regex(
            """([+-]?)\s*([0-9\u06F0-\u06F9\u0660-\u0669]{1,3}(?:[,\u060C\u066C][0-9\u06F0-\u06F9\u0660-\u0669]{3})+|[0-9\u06F0-\u06F9\u0660-\u0669]+)\s*([+-]?)"""
        )

    private val currencyAmountRegex =
        Regex(
            """([+-]?)\s*([0-9\u06F0-\u06F9\u0660-\u0669]{1,3}(?:[,\u060C\u066C][0-9\u06F0-\u06F9\u0660-\u0669]{3})+|[0-9\u06F0-\u06F9\u0660-\u0669]+)\s*(?:\u0631\u06cc\u0627\u0644|\u062a\u0648\u0645\u0627\u0646)\s*([+-]?)"""
        )

    private val merchantCueRegex =
        Regex(
            """(?:\u062e\u0631\u06cc\u062f \u0627\u0632|\u0641\u0631\u0648\u0634\u06af\u0627\u0647[:\s]|\u067e\u0630\u06cc\u0631\u0646\u062f\u0647[:\s]|\u0628\u0627\u0628\u062a|\u0646\u0632\u062f|\u0628\u0647 \u0646\u0627\u0645[:\s])\s*([^\n\r,\u060C]{2,40})"""
        )

    fun parse(
        sender: String?,
        body: String,
    ): ParsedBankSms? {

        val normalizedBody =
            body
                .replace('\u064A', '\u06CC')
                .replace('\u0643', '\u06A9')
                .replace('\u200C', ' ')
                .replace(Regex("\\s+"), " ")
                .trim()

        if (normalizedBody.isBlank()) {
            return null
        }

        if (
            nonTransactionalKeywords.any {
                normalizedBody.contains(it)
            }
        ) {
            return null
        }

        val bankFromSender =
            BankSenderRegistry.lookup(sender)

        val bankFromKeyword =
            bankKeywords
                .firstOrNull { (keyword, _) ->
                    normalizedBody.contains(keyword)
                }
                ?.second

        val bankName =
            bankFromSender
                ?: bankFromKeyword
                ?: return null

        var signFromAmount: Char? = null
        var amount: Long? = null

        val currencyMatch =
            currencyAmountRegex.find(normalizedBody)

        if (currencyMatch != null) {

            val prefixSign =
                currencyMatch
                    .groupValues[1]
                    .firstOrNull()

            val number =
                currencyMatch
                    .groupValues[2]

            val suffixSign =
                currencyMatch
                    .groupValues[3]
                    .firstOrNull()

            signFromAmount =
                when {
                    prefixSign == '+' || prefixSign == '-' ->
                        prefixSign

                    suffixSign == '+' || suffixSign == '-' ->
                        suffixSign

                    else ->
                        null
                }

            amount =
                normalizeAmountToLong(number)
        }

        if (amount == null) {

            val signedMatch =
                signedAmountRegex
                    .find(normalizedBody)

            if (signedMatch != null) {

                val prefixSign =
                    signedMatch
                        .groupValues[1]
                        .firstOrNull()

                val number =
                    signedMatch
                        .groupValues[2]

                val suffixSign =
                    signedMatch
                        .groupValues[3]
                        .firstOrNull()

                signFromAmount =
                    when {
                        prefixSign == '+' || prefixSign == '-' ->
                            prefixSign

                        suffixSign == '+' || suffixSign == '-' ->
                            suffixSign

                        else ->
                            null
                    }

                amount =
                    normalizeAmountToLong(number)
            }
        }

        val merchant =
            merchantCueRegex
                .find(normalizedBody)
                ?.groupValues
                ?.get(1)
                ?.trim()

        val type =
            when {
                signFromAmount == '-' ->
                    TransactionType.EXPENSE

                signFromAmount == '+' ->
                    TransactionType.INCOME

                incomeKeywords.any {
                    normalizedBody.contains(it)
                } ->
                    TransactionType.INCOME

                expenseKeywords.any {
                    normalizedBody.contains(it)
                } ->
                    TransactionType.EXPENSE

                else ->
                    null
            }

        /*
         * Do not silently classify an unknown-direction bank SMS
         * as EXPENSE.
         *
         * This is the key fix for the previous "Blue deposit became -200m"
         * behavior.
         */
        if (amount == null || type == null) {
            return null
        }

        return ParsedBankSms(
            bankName = bankName,
            amount = amount,
            merchant = merchant,
            type = type,
            rawBody = body,
        )
    }

    private fun normalizeAmountToLong(
        raw: String,
    ): Long? {

        val normalized =
            buildString {
                raw.forEach { char ->

                    when (char) {

                        in '\u06F0'..'\u06F9' ->
                            append(
                                ('0'.code +
                                    (char.code - '\u06F0'.code))
                                    .toChar()
                            )

                        in '\u0660'..'\u0669' ->
                            append(
                                ('0'.code +
                                    (char.code - '\u0660'.code))
                                    .toChar()
                            )

                        ',' , '\u060C', '\u066C' ->
                            Unit

                        else ->
                            append(char)
                    }
                }
            }

        return normalized.toLongOrNull()
    }
}

