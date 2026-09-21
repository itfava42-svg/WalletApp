package com.example.walletapp.sms

import android.content.Context

/**
 * Application boundary for SMS duplicate-processing state.
 *
 * Keeps receiver/import flows independent from the persistence store while
 * preserving the existing fingerprint and retry semantics.
 */
object SmsProcessedManager {

    fun attachContext(context: Context) {
        SmsProcessedStore.attachContext(context)
    }

    fun tryClaim(
        context: Context,
        sender: String?,
        body: String,
        smsDate: Long,
    ): Boolean {
        return SmsProcessedStore.tryClaim(
            context = context,
            sender = sender,
            body = body,
            smsDate = smsDate,
        )
    }

    fun release(
        context: Context,
        sender: String?,
        body: String,
        smsDate: Long,
    ) {
        SmsProcessedStore.release(
            context = context,
            sender = sender,
            body = body,
            smsDate = smsDate,
        )
    }

    fun sourceId(
        sender: String,
        smsDate: Long,
        body: String,
    ): String {
        return SmsProcessedStore.sourceId(
            sender = sender,
            smsDate = smsDate,
            body = body,
        )
    }

    fun clear(context: Context) {
        SmsProcessedStore.clear(context)
    }
}
