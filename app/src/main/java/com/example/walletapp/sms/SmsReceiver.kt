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
        Log.d(
            TAG,
            "onReceive fired, action=${intent.action}"
        )

        if (
            intent.action !=
                Telephony.Sms.Intents.SMS_RECEIVED_ACTION
        ) {
            return
        }

        val messages =
            Telephony.Sms.Intents.getMessagesFromIntent(
                intent
            )

        if (
            messages == null ||
            messages.isEmpty()
        ) {
            Log.d(
                TAG,
                "no messages in intent"
            )
            return
        }

        // Keep the receiver alive while processing the SMS.
        val pendingResult = goAsync()

        // This receiver may run in a fresh process,
        // so persistence must be attached first.
        TransactionStore.attachContext(context)

        // SMS-specific metadata is stored separately
        // from the Transaction model.
        SmsTransactionMetadataStore.attachContext(
            context
        )

        try {
            val sender =
                messages
                    .first()
                    .originatingAddress

            val fullBody =
                messages.joinToString(
                    separator = ""
                ) {
                    it.messageBody ?: ""
                }

            Log.d(
                TAG,
                "sender=$sender bodyLength=${fullBody.length}"
            )

            Log.d(
                TAG,
                "body=$fullBody"
            )

            // Central decision:
            // ignore rules + learned corrections + parser.
            val decision =
                SmsDecisionEngine.decide(
                    context = context,
                    sender = sender,
                    body = fullBody,
                )

            if (decision.ignored) {
                Log.d(
                    TAG,
                    "SMS ignored. " +
                        "sender=$sender " +
                        "pattern=${decision.matchedPattern}"
                )
                return
            }

            val parsed = decision.parsed

            if (parsed == null) {
                Log.d(
                    TAG,
                    "SMS was not recognized " +
                        "as a bank transaction"
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

            val amount = parsed.amount

            if (amount == null) {
                Log.d(
                    TAG,
                    "amount is null, " +
                        "skipping store/popup"
                )
                return
            }

            /*
             * Create the transaction and keep the exact
             * generated transaction ID.
             */
            val transaction =
                TransactionStore.addFromBankSms(
                    bankName = parsed.bankName,
                    amount = amount,
                    merchant = parsed.merchant,
                    type = parsed.type,
                )

            /*
             * Store SMS-specific metadata separately.
             *
             * This gives the completion screen access to:
             * - sender
             * - raw SMS body
             * - SMS timestamp
             * - detected bank
             *
             * without changing Transaction.kt.
             */
            SmsTransactionMetadataStore.save(
                transactionId = transaction.id,
                sender = sender ?: "",
                rawBody = fullBody,
                smsDate =
                    messages
                        .maxOfOrNull {
                            it.timestampMillis
                        }
                        ?: System.currentTimeMillis(),
                originalBank = parsed.bankName,
            )

            Log.d(
                TAG,
                "transaction added and metadata saved. " +
                    "transactionId=${transaction.id} " +
                    "total count=" +
                    TransactionStore.transactions.size
            )

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
        } finally {
            pendingResult.finish()
        }
    }
}
