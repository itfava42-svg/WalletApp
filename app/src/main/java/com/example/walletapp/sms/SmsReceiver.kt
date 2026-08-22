package com.example.walletapp.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.example.walletapp.data.TransactionStore

private const val TAG = "WalletSms"

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive fired, action=${intent.action}")

        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages == null || messages.isEmpty()) {
            Log.d(TAG, "no messages in intent")
            return
        }

        // goAsync() tells Android this receiver needs a little more time and
        // keeps the (often freshly-spawned) process at a higher scheduling
        // priority while we finish — this measurably reduces the chance of
        // the process being killed by the low-memory killer (lmkd) right
        // after being started for this broadcast, though it can't fully
        // prevent it under extreme memory pressure. Call finish() in every
        // exit path once done.
        val pendingResult = goAsync()

        // Attach context (and load any previously-persisted transactions)
        // as early as possible — this may be a fresh, receiver-only process
        // that never opens MainActivity, so this is the only chance to wire
        // up disk persistence before adding the new transaction.
        TransactionStore.attachContext(context)

        try {
            val sender = messages.first().originatingAddress
            val fullBody = messages.joinToString(separator = "") { it.messageBody ?: "" }
            Log.d(TAG, "sender=$sender bodyLength=${fullBody.length}")
            Log.d(TAG, "body=$fullBody")

            val parsed = BankSmsParser.parse(sender, fullBody)
            if (parsed == null) {
                Log.d(TAG, "parser returned null (no bank matched via sender or keyword)")
                return
            }
            Log.d(TAG, "parsed bank=${parsed.bankName} amount=${parsed.amount} merchant=${parsed.merchant} type=${parsed.type}")

            val amount = parsed.amount
            if (amount == null) {
                Log.d(TAG, "amount is null, skipping store/popup")
                return
            }

            TransactionStore.addFromBankSms(
                bankName = parsed.bankName,
                amount = amount,
                merchant = parsed.merchant,
                type = parsed.type,
            )
            Log.d(TAG, "transaction added to store, total count now ${TransactionStore.transactions.size}")

            if (OverlayPopupManager.canShowOverlay(context)) {
                Log.d(TAG, "showing overlay popup")
                OverlayPopupManager.showTransactionPopup(
                    context = context,
                    bankName = parsed.bankName,
                    amount = amount,
                    merchant = parsed.merchant,
                    type = parsed.type,
                )
            } else {
                Log.d(TAG, "overlay permission not granted, falling back to notification")
                BankSmsNotifier.notify(context, parsed.bankName, amount, parsed.merchant, parsed.type)
            }
        } finally {
            pendingResult.finish()
        }
    }
}