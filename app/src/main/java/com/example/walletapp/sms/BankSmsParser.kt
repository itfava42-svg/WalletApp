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
 * Heuristic parser for Persian-language Iranian bank transaction SMS.
 *
 * Bank SMS formats differ from bank to bank and change over time, so this
 * uses a combination of:
 *  - keyword matching in the message body to name the bank and detect a
 *    few common bank-name synonyms/typos, and
 *  - the sender's short-code number or alphanumeric ID (via
 *    [BankSenderRegistry]) as a second, more reliable signal when known.
 *
 * If matching stays inaccurate for a particular bank's real message format,
 * add/adjust the keyword and regex lists below to match real samples.
 */
object BankSmsParser {

    private val bankKeywords: List<Pair<String, String>> = listOf(
        "ملت" to "بانک ملت",
        "ملی" to "بانک ملی",
        "سامان" to "بانک سامان",
        "پارسیان" to "بانک پارسیان",
        "پاسارگاد" to "بانک پاسارگاد",
        "سپه" to "بانک سپه",
        "کشاورزی" to "بانک کشاورزی",
        "تجارت" to "بانک تجارت",
        "صادرات" to "بانک صادرات",
        "رفاه" to "بانک رفاه",
        "اقتصاد نوین" to "بانک اقتصاد نوین",
        "انصار" to "بانک انصار",
        "شهر" to "بانک شهر",
        "ایران زمین" to "بانک ایران زمین",
        "گردشگری" to "بانک گردشگری",
        "پست بانک" to "پست بانک",
        "کارآفرین" to "بانک کارآفرین",
        "سینا" to "بانک سینا",
        "مسکن" to "بانک مسکن",
        "سرمایه" to "بانک سرمایه",
        "بلو" to "بلو (بانک آینده)",
        "بانک آینده" to "بانک آینده",
        "بلوبانک" to "بلو (بانک آینده)",
    )

    private val expenseKeywords = listOf("برداشت", "خرید", "کسر", "پرداخت", "انتقال از", "پرید", "حواله")
    private val incomeKeywords = listOf("واریز", "افزایش موجودی", "انتقال به", "بازگشت وجه")

    // Messages that are login/verification codes, not actual transactions —
    // even though they may mention a bank name or an amount. Skipped
    // entirely so we don't log false "transactions" for OTP/login SMS.
    private val nonTransactionalKeywords = listOf(
        "جهت ورود", "ورود به سامانه", "رمز پویا", "کد یکبار مصرف",
        "شناسه تایید برداشت", "شناسه تاييد برداشت",
    )

    // Matches amounts like "1,250,000" or Persian-digit "۱٬۲۵۰٬۰۰۰" followed by ریال/تومان.
    private val amountRegex = Regex(
        "([0-9۰-۹][0-9۰-۹,،٬]{2,})\\s*(ریال|تومان)"
    )

    // Fallback for compact-format bank SMS with no ریال/تومان word at all —
    // just a signed, comma-grouped number, with the sign either BEFORE it
    // (e.g. Resalat Bank: "-10,000,000") or AFTER it (e.g. Melli's "حواله
    // پل" format: "1,000,000-"). An explicit +/- sign is required so we
    // don't accidentally match unrelated numbers (balances, reference
    // codes, phone numbers, etc.) elsewhere in the message.
    private val signedAmountRegex = Regex(
        "(?:([+-])([0-9۰-۹]{1,3}(?:[,،٬][0-9۰-۹]{3})+))" +
            "|(?:([0-9۰-۹]{1,3}(?:[,،٬][0-9۰-۹]{3})+)([+-]))"
    )

    // Cue phrases that tend to precede a merchant / recipient / installment description.
    private val merchantCueRegex = Regex(
        "(?:خرید از|فروشگاه[:\\s]|پذیرنده[:\\s]|بابت|نزد|به نام[:\\s])\\s*([^\\n\\r,،]{2,40})"
    )

    fun parse(sender: String?, body: String): ParsedBankSms? {
        // Some bank SMS gateways use Arabic-script Yeh/Kaf (ي, ك) instead of
        // the Persian variants (ی, ک), which look identical but are
        // different Unicode code points — normalize before any keyword
        // matching, or "ملي" silently fails to match "ملی".
        val normalizedBody = body.replace('\u064A', '\u06CC').replace('\u0643', '\u06A9')

        if (nonTransactionalKeywords.any { normalizedBody.contains(it) }) return null

        val bankFromSender = BankSenderRegistry.lookup(sender)
        val bankFromKeyword = bankKeywords.firstOrNull { (kw, _) -> normalizedBody.contains(kw) }?.second

        // Require at least one signal (sender number OR keyword) to treat
        // this as a bank SMS at all — avoids misclassifying unrelated texts.
        val bankName = bankFromSender ?: bankFromKeyword ?: return null

        var signFromAmount: Char? = null
        var amount = amountRegex.find(normalizedBody)?.groupValues?.get(1)?.let { normalizeAmountToLong(it) }
        if (amount == null) {
            val fallbackMatch = signedAmountRegex.find(normalizedBody)
            if (fallbackMatch != null) {
                val groups = fallbackMatch.groupValues
                val prefixSign = groups[1]
                val prefixNumber = groups[2]
                val suffixNumber = groups[3]
                val suffixSign = groups[4]
                if (prefixNumber.isNotEmpty()) {
                    signFromAmount = prefixSign.firstOrNull()
                    amount = normalizeAmountToLong(prefixNumber)
                } else if (suffixNumber.isNotEmpty()) {
                    signFromAmount = suffixSign.firstOrNull()
                    amount = normalizeAmountToLong(suffixNumber)
                }
            }
        }

        val merchant = merchantCueRegex.find(normalizedBody)?.groupValues?.get(1)?.trim()

        val type = when {
            signFromAmount == '-' -> TransactionType.EXPENSE
            signFromAmount == '+' -> TransactionType.INCOME
            incomeKeywords.any { normalizedBody.contains(it) } -> TransactionType.INCOME
            expenseKeywords.any { normalizedBody.contains(it) } -> TransactionType.EXPENSE
            else -> TransactionType.EXPENSE // default assumption for card-purchase alerts
        }

        return ParsedBankSms(
            bankName = bankName,
            amount = amount,
            merchant = merchant,
            type = type,
            rawBody = body,
        )
    }

    private fun normalizeAmountToLong(raw: String): Long? {
        val persianToEnglish = mapOf(
            '۰' to '0', '۱' to '1', '۲' to '2', '۳' to '3', '۴' to '4',
            '۵' to '5', '۶' to '6', '۷' to '7', '۸' to '8', '۹' to '9',
        )
        val normalized = raw.map { c -> persianToEnglish[c] ?: c }
            .joinToString("")
            .replace(",", "")
            .replace("،", "")
            .replace("٬", "")
        return normalized.toLongOrNull()
    }
}