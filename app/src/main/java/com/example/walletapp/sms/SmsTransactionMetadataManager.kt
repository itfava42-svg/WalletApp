package com.example.walletapp.sms

import android.content.Context

/**
 * Application boundary for SMS transaction metadata.
 * Keeps callers independent from the persistence implementation.
 */
object SmsTransactionMetadataManager {

    fun attachContext(context: Context) =
        SmsTransactionMetadataStore.attachContext(context)

    fun save(
        transactionId: String,
        sender: String,
        rawBody: String,
        smsDate: Long,
        originalBank: String,
        accountNumber: String? = null,
        cardNumber: String? = null,
        cardLast4: String? = null,
    ) = SmsTransactionMetadataStore.save(
        transactionId = transactionId,
        sender = sender,
        rawBody = rawBody,
        smsDate = smsDate,
        originalBank = originalBank,
        accountNumber = accountNumber,
        cardNumber = cardNumber,
        cardLast4 = cardLast4,
    )

    fun get(transactionId: String): SmsTransactionMetadataStore.Metadata? =
        SmsTransactionMetadataStore.get(transactionId)

    fun remove(transactionId: String) =
        SmsTransactionMetadataStore.remove(transactionId)

    fun getAll(): List<SmsTransactionMetadataStore.Metadata> =
        SmsTransactionMetadataStore.getAll()

    fun clear() =
        SmsTransactionMetadataStore.clear()
}
