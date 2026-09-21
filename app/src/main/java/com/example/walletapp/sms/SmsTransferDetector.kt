package com.example.walletapp.sms

import com.example.walletapp.data.Account

/**
 * Deterministic, local-only transfer inference for SMS completion.
 *
 * The detector is deliberately conservative:
 * - it only auto-suggests a transfer when the SMS contains transfer evidence;
 * - the source account must be uniquely identifiable from the SMS;
 * - the destination is auto-filled only when another active account is
 *   explicitly identifiable from an account/card number in the same SMS.
 *
 * This keeps financial data safe while removing repetitive manual selection.
 */
object SmsTransferDetector {

    data class Suggestion(
        val sourceAccountId: String?,
        val destinationAccountId: String?,
        val confidence: Int,
        val reason: String,
    ) {
        val isStrong: Boolean
            get() = confidence >= 90 &&
                sourceAccountId != null &&
                destinationAccountId != null

        val isUsableTransferSuggestion: Boolean
            get() = confidence >= 70 && sourceAccountId != null
    }

    private val transferKeywords = listOf(
        "انتقال وجه",
        "انتقال از کارت",
        "کارت به کارت",
        "انتقال بین حساب",
        "انتقال بین کارت",
        "حواله",
        "پایا",
        "ساتنا",
        "پل",
        "transfer",
        "card to card",
    )

    private val accountCueRegex = Regex(
        """(?:شماره\s*)?حساب\s*[:\-]?\s*([0-9۰-۹٠-٩]{6,20})"""
    )

    private val cardCueRegex = Regex(
        """(?:شماره\s*)?کارت\s*[:\-]?\s*((?:[0-9۰-۹٠-٩Xx*]{4}[\s\-_]*){0,3}[0-9۰-۹٠-٩Xx*]{4})"""
    )

    fun suggest(
        rawBody: String?,
        detectedBank: String?,
        smsAccountNumber: String?,
        smsCardLast4: String?,
        accounts: List<Account>,
    ): Suggestion? {
        if (rawBody.isNullOrBlank()) return null

        val normalizedBody = normalizeText(rawBody)
        if (transferKeywords.none { normalizedBody.contains(it) }) {
            return null
        }

        val activeAccounts = accounts.filter { it.isActive }
        if (activeAccounts.isEmpty()) return null

        val sourceMatches = linkedSetOf<String>()

        val normalizedSmsAccount = normalizeDigits(smsAccountNumber.orEmpty())
        val normalizedSmsCard = normalizeDigits(smsCardLast4.orEmpty()).takeLast(4)
        val normalizedBank = detectedBank?.trim().orEmpty()

        if (normalizedSmsAccount.isNotBlank()) {
            activeAccounts
                .filter {
                    normalizeDigits(it.accountNumber) == normalizedSmsAccount &&
                        bankCompatible(it.bankName, normalizedBank)
                }
                .forEach { sourceMatches += it.id }
        }

        if (normalizedSmsCard.length == 4) {
            activeAccounts
                .filter {
                    normalizeDigits(it.cardLast4).takeLast(4) == normalizedSmsCard &&
                        bankCompatible(it.bankName, normalizedBank)
                }
                .forEach { sourceMatches += it.id }
        }

        if (sourceMatches.size != 1) {
            return null
        }

        val sourceId = sourceMatches.first()
        val source = activeAccounts.firstOrNull { it.id == sourceId } ?: return null

        val accountCandidates = accountCueRegex
            .findAll(normalizedBody)
            .map { normalizeDigits(it.groupValues[1]) }
            .filter { it.length >= 6 }
            .distinct()
            .toList()

        val cardCandidates = cardCueRegex
            .findAll(normalizedBody)
            .map { normalizeDigits(it.groupValues[1]).takeLast(4) }
            .filter { it.length == 4 }
            .distinct()
            .toList()

        val destinationMatches = linkedSetOf<String>()

        accountCandidates.forEach { number ->
            activeAccounts
                .filter {
                    it.id != sourceId &&
                        normalizeDigits(it.accountNumber) == number
                }
                .forEach { destinationMatches += it.id }
        }

        cardCandidates.forEach { last4 ->
            activeAccounts
                .filter {
                    it.id != sourceId &&
                        normalizeDigits(it.cardLast4).takeLast(4) == last4
                }
                .forEach { destinationMatches += it.id }
        }

        val destinationId =
            destinationMatches.singleOrNull()

        val confidence =
            when {
                destinationId != null -> 95
                normalizedSmsAccount.isNotBlank() ||
                    normalizedSmsCard.length == 4 -> 75
                else -> 70
            }

        val reason =
            if (destinationId != null) {
                "انتقال وجه تشخیص داده شد؛ حساب مبدأ و مقصد از اطلاعات پیامک با حساب‌های شما تطبیق داده شدند."
            } else {
                "انتقال وجه تشخیص داده شد و حساب مبدأ از اطلاعات پیامک با حساب شما تطبیق داده شد."
            }

        return Suggestion(
            sourceAccountId = source.id,
            destinationAccountId = destinationId,
            confidence = confidence,
            reason = reason,
        )
    }

    private fun bankCompatible(
        accountBank: String,
        detectedBank: String,
    ): Boolean {
        if (detectedBank.isBlank() || accountBank.isBlank()) return true
        return accountBank.trim() == detectedBank.trim()
    }

    private fun normalizeText(value: String): String =
        value
            .replace('ي', 'ی')
            .replace('ك', 'ک')
            .replace('\u200C', ' ')
            .replace(Regex("\\s+"), " ")
            .trim()
            .lowercase()

    private fun normalizeDigits(value: String): String =
        buildString {
            value.forEach { char ->
                when (char) {
                    in '۰'..'۹' ->
                        append(('0'.code + char.code - '۰'.code).toChar())
                    in '٠'..'٩' ->
                        append(('0'.code + char.code - '٠'.code).toChar())
                    else ->
                        if (char.isDigit()) append(char)
                }
            }
        }
}
