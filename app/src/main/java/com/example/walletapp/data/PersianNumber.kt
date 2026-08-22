package com.example.walletapp.data

// Unicode escapes used instead of literal Persian glyphs to avoid
// "too many characters in a character literal" / mojibake errors caused by
// source-file encoding mismatches on some Windows/Gradle setups.
private val PERSIAN_DIGITS = charArrayOf(
    '\u06F0', '\u06F1', '\u06F2', '\u06F3', '\u06F4',
    '\u06F5', '\u06F6', '\u06F7', '\u06F8', '\u06F9',
)

/** Converts any string's ASCII digits (0-9) to Persian digits, leaving other characters untouched. */
fun String.toPersianDigits(): String {
    val sb = StringBuilder()
    for (c in this) {
        sb.append(if (c.isDigit()) PERSIAN_DIGITS[c - '0'] else c)
    }
    return sb.toString()
}

/** Formats a Long as a thousands-separated Persian-digit string, e.g. 45000000 -> "۴۵,۰۰۰,۰۰۰" */
fun Long.toPersianCurrency(): String {
    val grouped = "%,d".format(this)
    val sb = StringBuilder()
    for (c in grouped) {
        sb.append(if (c.isDigit()) PERSIAN_DIGITS[c - '0'] else c)
    }
    return sb.toString()
}