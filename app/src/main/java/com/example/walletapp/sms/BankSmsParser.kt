package com.example.walletapp.sms

import com.example.walletapp.data.TransactionType

data class ParsedBankSms(
    val bankName: String,
    val amount: Long?,
    val merchant: String?,
    val type: TransactionType,
    val rawBody: String,
    val accountNumber: String? = null,
    val cardNumber: String? = null,
    val cardLast4: String? = null,
    val balance: Long? = null,
    val isTransfer: Boolean = false,
)

/**
 * Parser for common Iranian bank SMS formats.
 *
 * Persian strings are represented with Unicode escapes intentionally
 * so PowerShell/source-file encoding cannot corrupt the parser.
 */
object BankSmsParser {

    
    /**
     * Used when the SMS is clearly transactional but the bank
     * cannot yet be identified from sender/body/learning.
     */
    const val UNKNOWN_BANK = "\u0628\u0627\u0646\u06a9 \u0646\u0627\u0645\u0634\u062e\u0635"
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

    private val transferKeywords =
        listOf(
            "\u0627\u0646\u062a\u0642\u0627\u0644 \u0648\u062c\u0647",
            "\u0627\u0646\u062a\u0642\u0627\u0644 \u0627\u0632 \u06a9\u0627\u0631\u062a",
            "\u06a9\u0627\u0631\u062a \u0628\u0647 \u06a9\u0627\u0631\u062a",
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
            """(?<![0-9\u06F0-\u06F9\u0660-\u0669])([+-]?)\s*([0-9\u06F0-\u06F9\u0660-\u0669]{1,3}(?:[,\u060C\u066C][0-9\u06F0-\u06F9\u0660-\u0669]{3})+|[0-9\u06F0-\u06F9\u0660-\u0669]+)\s*([+-]?)(?![0-9\u06F0-\u06F9\u0660-\u0669])"""
        )

    private val currencyAmountRegex =
        Regex(
            """(?<![0-9\u06F0-\u06F9\u0660-\u0669])([+-]?)\s*([0-9\u06F0-\u06F9\u0660-\u0669]{1,3}(?:[,\u060C\u066C][0-9\u06F0-\u06F9\u0660-\u0669]{3})+|[0-9\u06F0-\u06F9\u0660-\u0669]+)\s*(?:\u0631\u06cc\u0627\u0644|\u062a\u0648\u0645\u0627\u0646)\s*([+-]?)(?![0-9\u06F0-\u06F9\u0660-\u0669])"""
        )

    /*
     * Account/card extraction is intentionally conservative.
     *
     * Account numbers are extracted only when an explicit
     * account cue exists, such as:
     *   حساب226284257
     *   حساب 226284257
     *   شماره حساب: 226284257
     *
     * Card numbers are extracted only when an explicit card cue
     * exists. We keep only the last four digits for matching.
     *
     * We do NOT interpret arbitrary SMS numbers as accounts/cards.
     */
    private val accountNumberRegex =
        Regex(
            """(?:\u0634\u0645\u0627\u0631\u0647\s*)?\u062d\u0633\u0627\u0628\s*[:\-]?\s*([0-9\u06F0-\u06F9\u0660-\u0669]{6,20})"""
        )

    private val cardNumberRegex =
        Regex(
            """(?:\u0634\u0645\u0627\u0631\u0647\s*)?\u06a9\u0627\u0631\u062a\s*[:\-]?\s*((?:[0-9\u06F0-\u06F9\u0660-\u0669Xx*]{4}[\s\-_]*){0,3}[0-9\u06F0-\u06F9\u0660-\u0669Xx*]{4})"""
        )

    private val balanceRegex =
        Regex(
            """(?:موجودی|مانده)(?:\s*حساب)?\s*[:=]?\s*([0-9۰-۹٠-٩]{1,3}(?:[,،٬][0-9۰-۹٠-٩]{3})+|[0-9۰-۹٠-٩]+)"""
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

        val detectedBank =
            bankFromSender
                ?: bankFromKeyword

        /*
         * Bank identification is independent from transaction
         * detection. Unknown/new senders must not cause a valid
         * transaction SMS to be discarded.
         */
        val bankName =
            detectedBank
                ?: UNKNOWN_BANK

        /*
         * Protect non-amount numeric fields before amount parsing.
         * The old parser selected the first numeric token, so card/account/
         * sender/balance numbers could be mistaken for the transaction amount.
         * We mask protected ranges with spaces while preserving string indexes.
         */
        val accountNumberMatch =
            accountNumberRegex.find(normalizedBody)

        val cardNumberMatch =
            cardNumberRegex.find(normalizedBody)

        val balanceMatch =
            balanceRegex.find(normalizedBody)

        var amountSearchBody = normalizedBody

        listOf(
            accountNumberMatch,
            cardNumberMatch,
            balanceMatch,
        )
            .filterNotNull()
            .sortedByDescending { it.range.first }
            .forEach { match ->
                val length = match.range.last - match.range.first + 1
                amountSearchBody =
                    amountSearchBody.replaceRange(
                        match.range.first,
                        match.range.last + 1,
                        " ".repeat(length),
                    )
            }

        /*
         * A sender short-code/number can also appear in the SMS body.
         * Mask the exact sender digits so it cannot become the amount.
         */
        val senderDigits =
            sender
                ?.let(::normalizeDigits)
                ?.filter { it.isDigit() }
                ?.takeIf { it.isNotBlank() }

        if (!senderDigits.isNullOrBlank()) {
            var senderIndex = amountSearchBody.indexOf(senderDigits)

            while (senderIndex >= 0) {
                amountSearchBody =
                    amountSearchBody.replaceRange(
                        senderIndex,
                        senderIndex + senderDigits.length,
                        " ".repeat(senderDigits.length),
                    )

                senderIndex =
                    amountSearchBody.indexOf(
                        senderDigits,
                        senderIndex + senderDigits.length,
                    )
            }
        }

        var signFromAmount: Char? = null
        var amount: Long? = null

        val currencyMatch =
            currencyAmountRegex.find(amountSearchBody)

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
                    ?.let { parsedAmount ->
                        // WalletApp stores and displays amounts in toman.
                        // Explicit rial amounts must be converted (10 rial = 1 toman).
                        if (currencyMatch.value.contains("\\u0631\\u06cc\\u0627\\u0644")) {
                            parsedAmount / 10L
                        } else {
                            parsedAmount
                        }
                    }
        }

        if (amount == null) {
            val signedMatch =
                signedAmountRegex
                    .find(amountSearchBody)

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

        val isTransfer =
            transferKeywords.any {
                normalizedBody.contains(it)
            }

        val type =
            when {
                signFromAmount == '-' ->
                    TransactionType.EXPENSE

                signFromAmount == '+' ->
                    TransactionType.INCOME

                isTransfer ->
                    TransactionType.EXPENSE

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

        /*
         * If the bank is unknown, require strong transaction
         * evidence. This prevents ordinary numeric SMS messages
         * from becoming transactions.
         */
        if (
            detectedBank == null &&
            !hasStrongTransactionEvidence(
                normalizedBody = normalizedBody,
                signFromAmount = signFromAmount,
            )
        ) {
            return null
        }

        val accountNumber =
            accountNumberMatch
                ?.groupValues
                ?.getOrNull(1)
                ?.let(::normalizeDigits)
                ?.takeIf { it.length in 6..20 }

        val cardNumber =
            cardNumberMatch
                ?.groupValues
                ?.getOrNull(1)
                ?.let(::normalizeDigits)
                ?.filter { it.isDigit() }
                ?.takeIf { it.length >= 4 }

        val cardLast4 =
            cardNumber
                ?.takeLast(4)
                ?.takeIf { it.length == 4 }

        val balance =
            balanceMatch
                ?.groupValues
                ?.getOrNull(1)
                ?.let(::normalizeAmountToLong)
                ?.let { parsedBalance ->
                    // Balance numbers in the same SMS use the transaction's explicit currency unit.
                    if (currencyMatch?.value?.contains("\\u0631\\u06cc\\u0627\\u0644") == true) {
                        parsedBalance / 10L
                    } else {
                        parsedBalance
                    }
                }

        return ParsedBankSms(
            bankName = bankName,
            amount = amount,
            merchant = merchant,
            type = type,
            rawBody = body,
            accountNumber = accountNumber,
            cardNumber = cardNumber,
            cardLast4 = cardLast4,
            balance = balance,
            isTransfer = isTransfer,
        )
    }

    private fun hasStrongTransactionEvidence(
        normalizedBody: String,
        signFromAmount: Char?,
    ): Boolean {

        /*
         * Explicit +/- amount is strong transaction evidence.
         */
        if (signFromAmount == '+' || signFromAmount == '-') {
            return true
        }

        /*
         * Unsigned amounts require explicit transaction language.
         */
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
                "\u067e\u0631\u06cc\u062f",
            )

        val strongTransferKeywords =
            listOf(
                "\u0627\u0646\u062a\u0642\u0627\u0644 \u0648\u062c\u0647",
                "\u0627\u0646\u062a\u0642\u0627\u0644 \u0627\u0632 \u06a9\u0627\u0631\u062a",
                "\u06a9\u0627\u0631\u062a \u0628\u0647 \u06a9\u0627\u0631\u062a",
            )

        return strongIncomeKeywords.any {
            normalizedBody.contains(it)
        } || strongExpenseKeywords.any {
            normalizedBody.contains(it)
        } || strongTransferKeywords.any {
            normalizedBody.contains(it)
        }
    }


    private fun normalizeDigits(
        raw: String,
    ): String {

        return buildString {
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

                    else ->
                        append(char)
                }
            }
        }
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





