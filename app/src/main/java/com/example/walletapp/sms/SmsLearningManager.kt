package com.example.walletapp.sms

import android.content.Context

/**
 * Domain boundary for SMS-learning operations.
 *
 * SmsLearningStore remains the authoritative persistence layer.
 * UI and SMS flows use this manager for user-learning mutations and reads.
 */
object SmsLearningManager {

    fun attachContext(context: Context) {
        SmsLearningStore.attachContext(context)
    }

    fun getAllSenderBanks(): Map<String, String> =
        SmsLearningStore.getAllSenderBanks()

    fun getSenderBank(sender: String?): String? =
        SmsLearningStore.getSenderBank(sender)

    fun getSenderTransactionType(sender: String?): String? =
        SmsLearningStore.getSenderTransactionType(sender)

    fun saveSenderBank(
        sender: String,
        bankName: String,
    ) {
        SmsLearningStore.saveSenderBank(
            sender = sender,
            bankName = bankName,
        )
    }

    fun saveSenderCategory(
        sender: String,
        category: String,
    ) {
        SmsLearningStore.saveSenderCategory(
            sender = sender,
            category = category,
        )
    }

    fun findPatternRule(
        sender: String?,
        messageBody: String,
    ): SmsLearningStore.PatternRule? =
        SmsLearningStore.findPatternRule(
            sender = sender,
            messageBody = messageBody,
        )

    fun getAllPatternRules(): List<SmsLearningStore.PatternRule> =
        SmsLearningStore.getAllPatternRules()

    fun savePatternRule(
        sender: String,
        pattern: String,
        bankName: String? = null,
        category: String? = null,
        transactionType: String? = null,
    ) {
        SmsLearningStore.savePatternRule(
            sender = sender,
            pattern = pattern,
            bankName = bankName,
            category = category,
            transactionType = transactionType,
        )
    }

    fun removePatternRule(
        sender: String,
        pattern: String,
    ) {
        SmsLearningStore.removePatternRule(sender, pattern)
    }

    fun clearAllLearning() {
        SmsLearningStore.clearAllLearning()
    }
}
