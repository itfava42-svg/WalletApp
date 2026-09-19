package com.example.walletapp.data

/**
 * Coordinates local transaction synchronization.
 *
 * Transport/backend concerns remain outside TransactionStore.
 */
object TransactionSyncRepository {

    fun pendingTransactions(): List<Transaction> {
        return TransactionStore.transactions.filter {
            it.syncStatus == SyncStatus.LOCAL_ONLY ||
                it.syncStatus == SyncStatus.PENDING ||
                it.syncStatus == SyncStatus.FAILED
        }
    }

    fun applyRemote(remote: Transaction): Transaction {
        require(
            TransactionSyncValidation.validateTransactionForSync(remote)
        ) {
            "Remote transaction failed sync validation."
        }

        return TransactionStore.mergeOrApply(remote)
    }

    fun markSynced(transactionId: String): Transaction? {
        val current =
            TransactionStore.transactions.firstOrNull {
                it.id == transactionId
            } ?: return null

        val synced =
            current.copy(
                syncStatus = SyncStatus.SYNCED
            )

        TransactionStore.applyRemote(synced)

        return synced
    }

    fun markFailed(transactionId: String): Transaction? {
        val current =
            TransactionStore.transactions.firstOrNull {
                it.id == transactionId
            } ?: return null

        val failed =
            current.copy(
                syncStatus = SyncStatus.FAILED
            )

        TransactionStore.applyRemote(failed)

        return failed
    }

    fun containsEquivalent(transaction: Transaction): Boolean {
        return TransactionStore.transactions.any {
            it.id == transaction.id ||
                (
                    !transaction.sourceId.isNullOrBlank() &&
                        it.sourceId == transaction.sourceId
                )
        }
    }
}


