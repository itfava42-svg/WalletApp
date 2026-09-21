package com.example.walletapp.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.example.walletapp.data.TransactionStore

private const val TAG = "WalletSms"

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        Log.d(TAG, "onReceive fired, action=${intent.action}")

        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            return
        }

        val messages =
            Telephony.Sms.Intents.getMessagesFromIntent(intent)

        if (messages == null || messages.isEmpty()) {
            Log.d(TAG, "no messages in intent")
            return
        }

        val pendingResult = goAsync()

        TransactionStore.attachContext(context)
        SmsTransactionMetadataStore.attachContext(context)
        SmsProcessedManager.attachContext(context)

        try {

            val sender =
                messages.first().originatingAddress

            val fullBody =
                messages.joinToString(separator = "") {
                    it.messageBody ?: ""
                }

            val smsDate =
                messages.maxOfOrNull {
                    it.timestampMillis
                } ?: System.currentTimeMillis()

            Log.d(
                TAG,
                "sender=$sender bodyLength=${fullBody.length} smsDate=$smsDate"
            )

            /*
             * IMPORTANT:
             * Claim the SMS BEFORE creating a transaction.
             *
             * If Android delivers the same SMS again, or another
             * import path sees it, it will be rejected here.
             */
            val claimed =
                SmsProcessedManager.tryClaim(
                    context = context,
                    sender = sender,
                    body = fullBody,
                    smsDate = smsDate,
                )

            if (!claimed) {
                Log.w(
                    TAG,
                    "Duplicate SMS ignored. sender=$sender smsDate=$smsDate"
                )
                return
            }

            try {

                val decision =
                    SmsDecisionEngine.decide(
                        context = context,
                        sender = sender,
                        body = fullBody,
                    )

                if (decision.ignored) {
                    Log.d(
                        TAG,
                        "SMS ignored. sender=$sender pattern=${decision.matchedPattern}"
                    )
                    return
                }

                val parsed =
                    decision.parsed

                if (parsed == null) {
                    Log.d(
                        TAG,
                        "SMS was not recognized as a bank transaction"
                    )

                    /*
                     * Release the claim because this SMS was not imported.
                     * This allows a later improved parser/inbox scan to retry it.
                     */
                    SmsProcessedManager.release(
                        context = context,
                        sender = sender,
                        body = fullBody,
                        smsDate = smsDate,
                    )

                    return
                }

                Log.d(
                    TAG,
                    "decision bank=${parsed.bankName} " +
                        "amount=${parsed.amount} " +
                        "merchant=${parsed.merchant} " +
                        "type=${parsed.type} " +
                        "learnedPattern=${decision.matchedPattern}"
                )

                val amount =
                    parsed.amount

                if (amount == null) {
                    SmsProcessedManager.release(
                        context = context,
                        sender = sender,
                        body = fullBody,
                        smsDate = smsDate,
                    )

                    Log.d(
                        TAG,
                        "amount is null, skipping store/popup"
                    )

                    return
                }

                val sourceId = SmsProcessedManager.sourceId(
                    sender = sender ?: "",
                    smsDate = smsDate,
                    body = fullBody,
                )

                val transaction =
                    TransactionStore.addFromBankSms(
                        bankName = parsed.bankName,
                        amount = amount,
                        merchant = parsed.merchant,
                        type = parsed.type,
                        smsDate = smsDate,
                        sourceId = sourceId,
                    )

                SmsTransactionMetadataStore.save(
                    transactionId = transaction.id,
                    sender = sender ?: "",
                    rawBody = fullBody,
                    smsDate = smsDate,
                    originalBank = parsed.bankName,
                    accountNumber = parsed.accountNumber,
                    cardNumber = parsed.cardNumber,
                    cardLast4 = parsed.cardLast4,
                )

                Log.d(
                    TAG,
                    "transaction added and metadata saved. " +
                        "transactionId=${transaction.id} " +
                        "total count=${TransactionStore.transactions.size}"
                )

                /*
                 * Queue owns the completion flow.
                 * Only one completion activity should be visible at a time.
                 */
                val completionScreenOpened =
                    SmsTransactionQueue.showNextIfNeeded(
                        context = context
                    )

                if (completionScreenOpened) {
                    return
                }

                if (
                    OverlayPopupManager.canShowOverlay(
                        context
                    )
                ) {

                    Log.d(
                        TAG,
                        "showing overlay popup"
                    )

                    OverlayPopupManager.showTransactionPopup(
                        context = context,
                        bankName = parsed.bankName,
                        amount = amount,
                        merchant = parsed.merchant,
                        type = parsed.type,
                    )

                } else {

                    Log.d(
                        TAG,
                        "overlay permission not granted, " +
                            "falling back to notification"
                    )

                    BankSmsNotifier.notify(
                        context,
                        parsed.bankName,
                        amount,
                        parsed.merchant,
                        parsed.type,
                    )
                }

            } catch (error: Throwable) {

                /*
                 * If importing failed after claiming, allow retry.
                 */
                SmsProcessedManager.release(
                    context = context,
                    sender = sender,
                    body = fullBody,
                    smsDate = smsDate,
                )

                Log.e(
                    TAG,
                    "SMS processing failed",
                    error
                )

                throw error
            }

        } finally {
            pendingResult.finish()
        }
    }
}
