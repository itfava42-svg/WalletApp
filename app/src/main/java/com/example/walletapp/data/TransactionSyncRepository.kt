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

    /**
     * Marks an upload snapshot as synced only when the transaction has not
     * changed locally while the upload was in flight.
     *
     * A local edit after the snapshot must remain pending so that the newer
     * version is not accidentally hidden as SYNCED by an older upload result.
     */
    fun markSynced(uploadedSnapshot: Transaction): Boolean {
        val current =
            TransactionStore.transactions.firstOrNull {
                it.id == uploadedSnapshot.id
            } ?: return false

        if (current != uploadedSnapshot) {
            return false
        }

        val synced =
            current.copy(
                syncStatus = SyncStatus.SYNCED
            )

        TransactionStore.applyRemote(synced)

        return true
    }

    /**
     * Marks an upload snapshot as failed only when the transaction has not
     * changed locally while the upload was in flight.
     *
     * A newer local edit must remain in its current sync state rather than
     * being overwritten by a failure result for an older snapshot.
     */
    fun markFailed(uploadedSnapshot: Transaction): Boolean {
        val current =
            TransactionStore.transactions.firstOrNull {
                it.id == uploadedSnapshot.id
            } ?: return false

        if (current != uploadedSnapshot) {
            return false
        }

        val failed =
            current.copy(
                syncStatus = SyncStatus.FAILED
            )

        TransactionStore.applyRemote(failed)

        return true
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
