package com.example.walletapp.data

/**
 * Central synchronization engine.
 *
 * This layer coordinates local pending transactions and remote changes.
 * Network transport is intentionally not implemented here.
 *
 * SMS ingestion, parsing and TransactionStore local update semantics
 * remain independent from the sync transport.
 */
object TransactionSyncEngine {

    data class SyncBatch(
        val transactions: List<Transaction>,
        val generatedAt: Long = System.currentTimeMillis()
    )

    /**
     * Creates a deterministic batch of transactions waiting for sync.
     */
    fun createUploadBatch(): SyncBatch {
        return SyncBatch(
            transactions =
                TransactionSyncRepository
                    .pendingTransactions()
                    .sortedWith(
                        compareBy<Transaction> {
                            it.updatedAt
                        }.thenBy {
                            it.id
                        }
                    )
        )
    }

    /**
     * Applies one remote transaction through the repository layer.
     *
     * Conflict resolution and deduplication remain centralized.
     */
    fun applyRemote(
        transaction: Transaction
    ): Transaction {
        return TransactionSyncRepository.applyRemote(
            transaction
        )
    }

    /**
     * Applies multiple remote transactions.
     *
     * Each transaction is independently resolved and merged.
     */
    fun applyRemoteBatch(
        transactions: List<Transaction>
    ): List<Transaction> {

        return transactions.map {
            applyRemote(it)
        }
    }

    /**
     * Marks a successfully uploaded transaction as synced.
     */
    fun markUploadSuccess(
        transactionId: String
    ): Transaction? {
        return TransactionSyncRepository.markSynced(
            transactionId
        )
    }

    /**
     * Marks a failed upload so it can be retried.
     */
    fun markUploadFailure(
        transactionId: String
    ): Transaction? {
        return TransactionSyncRepository.markFailed(
            transactionId
        )
    }

    /**
     * Returns whether this transaction already exists locally.
     */
    fun isDuplicate(
        transaction: Transaction
    ): Boolean {
        return TransactionSyncRepository.containsEquivalent(
            transaction
        )
    }
}
