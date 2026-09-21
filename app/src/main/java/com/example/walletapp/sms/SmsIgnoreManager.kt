package com.example.walletapp.sms

import android.content.Context

/**
 * Domain boundary for SMS ignore-rule operations.
 *
 * SmsIgnoreStore remains the authoritative persistence layer.
 * UI and SMS flows use this manager for ignore-rule mutations and reads.
 */
object SmsIgnoreManager {

    fun attachContext(context: Context) {
        SmsIgnoreStore.attachContext(context)
    }

    fun ignoreSender(sender: String) {
        SmsIgnoreStore.ignoreSender(sender)
    }

    fun isSenderIgnored(sender: String?): Boolean =
        SmsIgnoreStore.isSenderIgnored(sender)

    fun unignoreSender(sender: String) {
        SmsIgnoreStore.unignoreSender(sender)
    }

    fun getIgnoredSenders(): Set<String> =
        SmsIgnoreStore.getIgnoredSenders()

    fun ignorePattern(
        sender: String,
        pattern: String,
    ) {
        SmsIgnoreStore.ignorePattern(sender, pattern)
    }

    fun isPatternIgnored(
        sender: String?,
        messageBody: String,
    ): Boolean =
        SmsIgnoreStore.isPatternIgnored(sender, messageBody)

    fun findIgnoredPattern(
        sender: String?,
        messageBody: String,
    ): SmsIgnoreStore.IgnoredPattern? =
        SmsIgnoreStore.findIgnoredPattern(sender, messageBody)

    fun unignorePattern(
        sender: String,
        pattern: String,
    ) {
        SmsIgnoreStore.unignorePattern(sender, pattern)
    }

    fun getIgnoredPatterns(): List<SmsIgnoreStore.IgnoredPattern> =
        SmsIgnoreStore.getIgnoredPatterns()

    fun clearAllIgnored() {
        SmsIgnoreStore.clearAllIgnored()
    }
}
