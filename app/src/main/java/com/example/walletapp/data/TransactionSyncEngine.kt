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
     *
     * A bank-SMS transaction remains local while it is waiting for
     * completion because it may not have an account yet. Uploading such
     * an incomplete transaction would violate the sync validation rule
     * for normal transactions.
     */
    fun createUploadBatch(): SyncBatch {
        return SyncBatch(
            transactions =
                TransactionSyncRepository
                    .pendingTransactions()
                    .asSequence()
                    .filterNot { it.requiresCompletion }
                    .sortedWith(
                        compareBy<Transaction> {
                            it.updatedAt
                        }.thenBy {
                            it.id
                        }
                    )
                    .toList()
        )
    }

    fun applyRemote(transaction: Transaction): Transaction {
        return TransactionSyncRepository.applyRemote(transaction)
    }

    fun applyRemoteBatch(transactions: List<Transaction>): List<Transaction> {
        require(
            TransactionSyncValidation.validateBatch(transactions)
        ) {
            "Remote batch failed sync validation."
        }

        return transactions.map { applyRemote(it) }
    }

    /**
     * Finalizes an upload result against the exact transaction snapshot
     * that was sent to the backend. A changed local transaction is left
     * untouched and remains eligible for a later upload.
     */
    fun markUploadSuccess(transaction: Transaction): Boolean {
        return TransactionSyncRepository.markSynced(transaction)
    }

    /**
     * Finalizes an upload failure against the exact transaction snapshot
     * that was sent to the backend. A changed local transaction is left
     * untouched.
     */
    fun markUploadFailure(transaction: Transaction): Boolean {
        return TransactionSyncRepository.markFailed(transaction)
    }

    fun isDuplicate(transaction: Transaction): Boolean {
        return TransactionSyncRepository.containsEquivalent(transaction)
    }
}
