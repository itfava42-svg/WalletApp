package com.example.walletapp.data

/**
 * Validation helpers for the local synchronization architecture.
 *
 * These checks do not create, modify or delete application transactions.
 * They only validate Sync data structures supplied by the caller.
 */
object TransactionSyncValidation {

    fun validateTransactionForSync(
        transaction: Transaction
    ): Boolean {

        if (transaction.id.isBlank()) {
            return false
        }

        if (transaction.createdAt < 0L) {
            return false
        }

        if (transaction.updatedAt < 0L) {
            return false
        }

        if (
            transaction.updatedAt > 0L &&
                transaction.createdAt > 0L &&
                transaction.updatedAt < transaction.createdAt
        ) {
            return false
        }

        if (
            transaction.kind == TransactionKind.NORMAL &&
                transaction.accountId.isNullOrBlank()
        ) {
            return false
        }

        if (transaction.kind == TransactionKind.TRANSFER) {

            if (transaction.sourceAccountId.isNullOrBlank()) {
                return false
            }

            if (transaction.destinationAccountId.isNullOrBlank()) {
                return false
            }

            if (
                transaction.sourceAccountId ==
                    transaction.destinationAccountId
            ) {
                return false
            }
        }

        return true
    }

    fun validateBatch(
        transactions: List<Transaction>
    ): Boolean {

        return transactions.all {
            validateTransactionForSync(it)
        }
    }
}
