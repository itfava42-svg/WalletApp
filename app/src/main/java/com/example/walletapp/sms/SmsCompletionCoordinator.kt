package com.example.walletapp.sms

import android.os.Handler
import android.os.Looper

/**
 * Coordinates delivery of SMS-completion transactions to the
 * foreground Compose host.
 *
 * This coordinator does NOT create or modify transactions.
 */
object SmsCompletionCoordinator {

    private val mainHandler = Handler(Looper.getMainLooper())

    @Volatile
    private var host: ((String) -> Unit)? = null

    /**
     * FIFO queue of transaction IDs waiting for the foreground host.
     * LinkedHashSet prevents duplicates while preserving insertion order.
     */
    private val pendingTransactionIds = LinkedHashSet<String>()

    /**
     * Registers the foreground Compose host.
     *
     * If transactions are already waiting, the first one is delivered.
     */
    @Synchronized
    fun registerHost(callback: (String) -> Unit): String? {
        host = callback
        android.util.Log.d("TEMP_SMS_FLOW", "HOST_REGISTERED")

        val next = pendingTransactionIds.firstOrNull()

        if (!next.isNullOrBlank()) {

            pendingTransactionIds.remove(next)

            mainHandler.post {
                android.util.Log.d(
                    "TEMP_SMS_FLOW",
                    "REGISTER_DELIVERY_CALLBACK transactionId=$next"
                )

                runCatching {
                    callback(next)
                    android.util.Log.d(
                        "TEMP_SMS_FLOW",
                        "REGISTER_CALLBACK_SUCCESS transactionId=$next"
                    )
                }.onFailure {
                    android.util.Log.e(
                        "TEMP_SMS_FLOW",
                        "REGISTER_CALLBACK_FAILED transactionId=$next",
                        it
                    )
                }
            }
        }

        return next
    }

    /**
     * Removes the foreground host.
     */
    @Synchronized
    fun unregisterHost() {
        host = null
    }

    /**
     * Returns true when a foreground Compose host is registered.
     */
    @Synchronized
    fun isHostRegistered(): Boolean {
        return host != null
    }

    /**
     * Delivers a transaction ID to the foreground Compose host.
     *
     * If no foreground host exists, the transaction ID is retained
     * in the pending queue and will be delivered when the host
     * registers.
     */
    @Synchronized
    fun deliver(transactionId: String): Boolean {

        if (transactionId.isBlank()) {
            return false
        }

        val callback = host

        android.util.Log.d(
            "TEMP_SMS_FLOW",
            "DELIVER_CALLED transactionId=$transactionId hostPresent=${callback != null}"
        )

        if (callback == null) {

            pendingTransactionIds.add(transactionId)

            return true
        }

        mainHandler.post {
            android.util.Log.d(
                "TEMP_SMS_FLOW",
                "DELIVERY_CALLBACK transactionId=$transactionId"
            )

            runCatching {
                callback(transactionId)

                android.util.Log.d(
                    "TEMP_SMS_FLOW",
                    "CALLBACK_SUCCESS transactionId=$transactionId"
                )
            }.onFailure {
                android.util.Log.e(
                    "TEMP_SMS_FLOW",
                    "CALLBACK_FAILED transactionId=$transactionId",
                    it
                )
            }
        }

        return true
    }

    /**
     * Returns the first pending transaction ID without removing it.
     */
    @Synchronized
    fun peekPendingTransactionId(): String? {
        return pendingTransactionIds.firstOrNull()
    }

    /**
     * Removes the first pending transaction ID.
     */
    @Synchronized
    fun clearPendingTransactionId() {

        val first = pendingTransactionIds.firstOrNull()

        if (first != null) {
            pendingTransactionIds.remove(first)
        }
    }

    /**
     * Number of transactions currently waiting for the host.
     */
    @Synchronized
    fun pendingCount(): Int {
        return pendingTransactionIds.size
    }

    /**
     * Clears host and pending in-memory delivery state.
     */
    @Synchronized
    fun reset() {

        host = null
        pendingTransactionIds.clear()
    }
}



