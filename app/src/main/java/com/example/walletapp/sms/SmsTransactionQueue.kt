package com.example.walletapp.sms

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.walletapp.data.TransactionStore

/**
 * Coordinates SMS transactions that require user completion.
 *
 * The actual queue state is derived from TransactionStore so it can
 * survive process death and remain consistent with persisted transactions.
 */
object SmsTransactionQueue {

    private const val TAG = "SmsQueue"

    private var isShowing = false

    /**
     * Transactions dismissed from the completion sheet are deferred
     * for the current process so the same pending transaction does not
     * immediately reopen after the user closes the sheet.
     */
    private val dismissedTransactionIds = LinkedHashSet<String>()

    /**
     * Starts showing the next incomplete SMS transaction, if any.
     */
    fun showNextIfNeeded(context: Context): Boolean {

        Log.d(
            TAG,
            "showNextIfNeeded() called. " +
                "isShowing=$isShowing " +
                "hostRegistered=${SmsCompletionCoordinator.isHostRegistered()}"
        )

        if (isShowing) {
            Log.w(TAG, "BLOCKED: isShowing=true")
            return true
        }

        TransactionStore.attachContext(context)

        val next = TransactionStore.transactions
            .firstOrNull {
                it.requiresCompletion &&
                    !dismissedTransactionIds.contains(it.id)
            }

        if (next == null) {
            Log.d(
                TAG,
                "No pending transaction requiring completion"
            )
            return false
        }

        Log.d(
            TAG,
            "NEXT transaction id=${next.id} " +
                "requiresCompletion=${next.requiresCompletion}"
        )

        isShowing = true

        /*
         * Compose is the only completion UI path.
         *
         * If the foreground Compose host exists, the transaction is
         * delivered immediately.
         *
         * If the host does not exist, SmsCompletionCoordinator stores
         * the transaction ID in its pending queue. MainActivity will
         * consume it when the Compose host registers.
         *
         * The legacy SmsTransactionCompletionActivity is intentionally
         * NOT launched from this queue anymore.
         */
        Log.d(
            TAG,
            "Sending transaction to Compose coordinator: ${next.id}"
        )

        val accepted =
            SmsCompletionCoordinator.deliver(next.id)

        if (accepted) {

            if (SmsCompletionCoordinator.isHostRegistered()) {

                Log.d(
                    TAG,
                    "Compose delivery accepted immediately for ${next.id}"
                )

            } else {

                Log.d(
                    TAG,
                    "Compose host unavailable. " +
                        "Transaction queued as pending: ${next.id}"
                )
            }

            return true
        }

        Log.w(
            TAG,
            "Compose coordinator rejected transaction ${next.id}"
        )

        isShowing = false

        return false
    }

    /**
     * Called when the completion activity finishes.
     */
    fun onCompletionFinished(context: Context) {

        Log.d(
            TAG,
            "onCompletionFinished()"
        )

        isShowing = false

        // Give the activity time to finish before opening the next item.
        Handler(Looper.getMainLooper()).post {
            showNextIfNeeded(context)
        }
    }

    /**
     * Called when the user closes/dismisses the completion sheet
     * without completing or ignoring the transaction.
     *
     * The transaction intentionally remains requiresCompletion=true.
     * It is only deferred for the current process so it does not
     * immediately reopen. Other pending SMS transactions may continue.
     */
    fun onCompletionDismissed(
        context: Context,
        transactionId: String,
    ) {

        Log.d(
            TAG,
            "onCompletionDismissed(transactionId=$transactionId)"
        )

        if (transactionId.isNotBlank()) {
            dismissedTransactionIds.add(transactionId)
        }

        isShowing = false

        Handler(Looper.getMainLooper()).post {
            showNextIfNeeded(context)
        }
    }

    /**
     * Allows the queue to recover after the application process is recreated.
     */
    fun reset() {

        Log.d(
            TAG,
            "reset()"
        )

        isShowing = false
        dismissedTransactionIds.clear()
    }
}

