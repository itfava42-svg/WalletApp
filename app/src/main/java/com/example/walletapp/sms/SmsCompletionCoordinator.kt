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

    /**
     * Optional synchronous dispatcher used only by JVM unit tests.
     * Production delivery always falls back to the Android main looper.
     */
    private var testDispatcher: ((() -> Unit) -> Unit)? = null

    @Volatile
    private var host: ((String) -> Unit)? = null

    /**
     * FIFO queue of transaction IDs waiting for the foreground host.
     * LinkedHashSet prevents duplicates while preserving insertion order.
     */
    private val pendingTransactionIds = LinkedHashSet<String>()

    /**
     * Test-only seam for JVM unit tests. Production code leaves this unset.
     */
    internal fun setDispatcherForTests(dispatcher: ((() -> Unit) -> Unit)?) {
        testDispatcher = dispatcher
    }

    private fun postToMain(action: () -> Unit) {
        val dispatcher = testDispatcher
        if (dispatcher != null) {
            dispatcher(action)
        } else {
            Handler(Looper.getMainLooper()).post(action)
        }
    }

    /**
     * Registers the foreground Compose host.
     *
     * If transactions are already waiting, the first one is delivered.
     */
    @Synchronized
    fun registerHost(callback: (String) -> Unit): String? {
        host = callback

        val next = pendingTransactionIds.firstOrNull()

        if (!next.isNullOrBlank()) {
            pendingTransactionIds.remove(next)

            postToMain {
                runCatching {
                    callback(next)
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

        if (callback == null) {
            pendingTransactionIds.add(transactionId)
            return true
        }

        postToMain {
            runCatching {
                callback(transactionId)
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
