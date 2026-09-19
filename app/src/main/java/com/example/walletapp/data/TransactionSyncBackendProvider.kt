package com.example.walletapp.data

/**
 * Central provider for the currently configured Sync backend.
 *
 * The application starts with the safe empty backend.
 * A real backend can later be installed without changing
 * TransactionSyncService or the financial core.
 */
object TransactionSyncBackendProvider {

    @Volatile
    private var currentBackend: TransactionSyncBackend =
        EmptyTransactionSyncBackend

    /**
     * Returns the currently configured backend.
     */
    fun get(): TransactionSyncBackend {
        return currentBackend
    }

    /**
     * Installs a backend implementation.
     *
     * This does not automatically start synchronization.
     */
    fun set(
        backend: TransactionSyncBackend
    ) {
        currentBackend = backend
    }

    /**
     * Restores the safe no-network backend.
     */
    fun reset() {
        currentBackend =
            EmptyTransactionSyncBackend
    }

    /**
     * Indicates whether a real backend has been configured.
     */
    fun isConfigured(): Boolean {
        return currentBackend !==
            EmptyTransactionSyncBackend
    }
}
