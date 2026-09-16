package com.example.walletapp.sms

import android.content.Context
import com.example.walletapp.data.TransactionStore

/**
 * Coordinates SMS transactions that require user completion.
 *
 * The actual queue state is derived from TransactionStore so it can
 * survive process death and remain consistent with persisted transactions.
 */
object SmsTransactionQueue {

    private var isShowing = false

    /**
     * Starts showing the next incomplete SMS transaction, if any.
     */
    fun showNextIfNeeded(context: Context): Boolean {
        if (isShowing) return true

        TransactionStore.attachContext(context)

        val next = TransactionStore.transactions
            .firstOrNull { it.requiresCompletion }

        if (next == null) return false

        isShowing = true

        // Prefer the real Compose ModalBottomSheet when the host is active.
        if (SmsCompletionCoordinator.isHostRegistered()) {
            if (SmsCompletionCoordinator.deliver(next.id)) {
                return true
            }
        }

        // Preserve the existing Activity fallback when no Compose host exists.
        return runCatching {
            SmsTransactionCompletionActivity.start(
                context = context,
                transactionId = next.id,
            )
        }.fold(
            onSuccess = { true },
            onFailure = {
                isShowing = false
                false
            },
        )
    }

    /**
     * Called when the completion activity finishes.
     */
    fun onCompletionFinished(context: Context) {
        isShowing = false

        // Give the activity time to finish before opening the next item.
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            showNextIfNeeded(context)
        }
    }

    /**
     * Allows the queue to recover after the application process is recreated.
     */
    fun reset() {
        isShowing = false
    }
}
