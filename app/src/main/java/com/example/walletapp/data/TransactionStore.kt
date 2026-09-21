package com.example.walletapp.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import java.util.UUID

/**
 * Single in-memory source of truth for transactions, shared between the
 * SMS BroadcastReceiver, the SMS inbox re-scan, and the Compose dashboard.
 *
 * Also persists to disk (via [TransactionPersistence]) so transactions
 * survive the app process being killed - call [attachContext] as early as
 * possible from every entry point (MainActivity, SmsReceiver,
 * SmsInboxScanner) since each may run in its own fresh process.
 */
object TransactionStore {

    val transactions = mutableStateListOf<Transaction>()

    private var appContext: Context? = null
    private var loadedFromDisk = false

    /**
     * Call this from every entry point (Activity, BroadcastReceiver, etc.)
     * before mutating the store.
     */
    fun attachContext(context: Context) {
        if (appContext == null) {
            appContext = context.applicationContext
        }

        if (!loadedFromDisk) {
            loadedFromDisk = true

            appContext?.let { ctx ->
                val loaded = TransactionPersistence.load(ctx)

                if (loaded.isNotEmpty()) {
                    transactions.clear()
                    transactions.addAll(loaded)
                }
            }
        }
    }

    private fun persist() {
        appContext?.let {
            TransactionPersistence.save(
                it,
                transactions
            )
        }
    }

    /**
     * Adds a manually created financial transaction.
     *
     * Normal transactions must belong to an active account.
     *
     * Internal transfers use addTransfer() and are validated
     * separately through source/destination accounts.
     *
     * Pending bank SMS transactions remain outside this method.
     */
    fun addManual(transaction: Transaction) {

        if (transaction.kind == TransactionKind.NORMAL) {

            val accountId =
                transaction.accountId?.takeIf {
                    it.isNotBlank()
                }
                    ?: throw IllegalArgumentException(
                        "A normal transaction must have an account."
                    )

            AccountStore.accounts.firstOrNull {
                it.id == accountId &&
                    it.isActive
            }
                ?: throw IllegalArgumentException(
                    "Transaction account does not exist or is inactive."
                )
        }

        val now = System.currentTimeMillis()

        val storedTransaction =
            transaction.copy(
                createdAt =
                    transaction.createdAt.takeIf { it > 0L }
                        ?: transaction.dateEpochMillis
                        ?: now,
                updatedAt =
                    transaction.updatedAt.takeIf { it > 0L }
                        ?: transaction.dateEpochMillis
                        ?: now,
                source = TransactionSource.MANUAL,
                syncStatus =
                    if (transaction.syncStatus == SyncStatus.SYNCED) {
                        SyncStatus.SYNCED
                    } else {
                        SyncStatus.LOCAL_ONLY
                    },
            )

        transactions.add(
            0,
            storedTransaction
        )

        persist()
    }

    /**
     * Updates a transaction as a LOCAL user change.
     *
     * Sync rules:
     * - createdAt is preserved.
     * - updatedAt is refreshed.
     * - a previously SYNCED transaction becomes PENDING.
     * - a previously FAILED transaction becomes PENDING again.
     * - LOCAL_ONLY remains LOCAL_ONLY.
     *
     * Remote synchronization must NOT use this method because
     * remote timestamps must be preserved during remote apply.
     */
    fun update(transaction: Transaction) {

        if (transaction.kind == TransactionKind.NORMAL) {
            val accountId = transaction.accountId?.takeIf {
                it.isNotBlank()
            }

            if (accountId != null) {
                AccountStore.accounts.firstOrNull {
                    it.id == accountId &&
                        it.isActive
                } ?: throw IllegalArgumentException(
                    "Transaction account does not exist or is inactive."
                )
            }
        }

        if (transaction.kind == TransactionKind.TRANSFER) {
            val sourceAccountId =
                transaction.sourceAccountId?.takeIf {
                    it.isNotBlank()
                } ?: throw IllegalArgumentException(
                    "Transfer source account is required."
                )

            val destinationAccountId =
                transaction.destinationAccountId?.takeIf {
                    it.isNotBlank()
                } ?: throw IllegalArgumentException(
                    "Transfer destination account is required."
                )

            require(sourceAccountId != destinationAccountId) {
                "Transfer source and destination accounts must differ."
            }

            AccountStore.accounts.firstOrNull {
                it.id == sourceAccountId &&
                    it.isActive
            } ?: throw IllegalArgumentException(
                "Transfer source account does not exist or is inactive."
            )

            AccountStore.accounts.firstOrNull {
                it.id == destinationAccountId &&
                    it.isActive
            } ?: throw IllegalArgumentException(
                "Transfer destination account does not exist or is inactive."
            )
        }

        val index =
            transactions.indexOfFirst {
                it.id == transaction.id
            }

        if (index >= 0) {

            val now = System.currentTimeMillis()

            val storedTransaction =
                transaction.copy(
                    createdAt =
                        transaction.createdAt.takeIf {
                            it > 0L
                        }
                            ?: transactions[index].createdAt.takeIf {
                                it > 0L
                            }
                            ?: transaction.dateEpochMillis
                            ?: now,

                    updatedAt = now,

                    syncStatus =
                        when (transaction.syncStatus) {
                            SyncStatus.SYNCED ->
                                SyncStatus.PENDING

                            SyncStatus.FAILED ->
                                SyncStatus.PENDING

                            else ->
                                transaction.syncStatus
                        }
                )

            transactions[index] = storedTransaction

            persist()
        }
    }

    /**
     * Restores an existing transaction to a previously captured local state.
     *
     * This is intentionally a low-level rollback primitive for domain
     * operations that coordinate more than one persistence boundary.
     * It does not re-run current transaction validation because the supplied
     * value is the already-persisted state being restored.
     */
    fun restore(transaction: Transaction) {
        val index =
            transactions.indexOfFirst { it.id == transaction.id }

        if (index >= 0) {
            transactions[index] = transaction
            persist()
        }
    }

    /**
     * Applies a transaction received from a remote sync source.
     *
     * IMPORTANT:
     * - This method intentionally does NOT call update().
     * - Remote createdAt / updatedAt values are preserved.
     * - Remote source / sourceId / account / transfer metadata are preserved.
     * - Existing transaction with the same id is replaced.
     * - Missing transaction is inserted.
     *
     * Conflict resolution belongs to the sync/merge layer.
     */
    fun applyRemote(transaction: Transaction) {

        require(transaction.id.isNotBlank()) {
            "Remote transaction id is required."
        }

        val index =
            transactions.indexOfFirst {
                it.id == transaction.id
            }

        if (index >= 0) {

            transactions[index] = transaction

        } else {

            transactions.add(
                0,
                transaction
            )
        }

        persist()
    }
    /**
     * Deduplicates and merges a transaction before remote apply.
     *
     * Identity priority:
     * 1. Transaction id
     * 2. sourceId for transactions originating from the same source event
     *
     * This method does not modify SMS parsing or ingestion.
     */
    fun mergeOrApply(transaction: Transaction): Transaction {

        require(transaction.id.isNotBlank()) {
            "Transaction id is required."
        }

        /*
         * Identity priority:
         * 1. Exact transaction id.
         * 2. sourceId for the original source event.
         *
         * This prevents duplicate transactions when the same
         * source event arrives with a different local id.
         */

        val sourceId =
            transaction.sourceId
                ?.takeIf {
                    it.isNotBlank()
                }

        val existingIndex =
            transactions.indexOfFirst { existing ->

                existing.id == transaction.id ||

                    (
                        sourceId != null &&
                            !existing.sourceId.isNullOrBlank() &&
                            existing.sourceId == sourceId
                    )
            }

        if (existingIndex >= 0) {

            val existing =
                transactions[existingIndex]

            val resolved =
                resolveConflict(
                    local = existing,
                    remote = transaction
                )

            transactions[existingIndex] = resolved

            persist()

            return resolved
        }

        transactions.add(
            0,
            transaction
        )

        persist()

        return transaction
    }
    /**
     * Resolves a local/remote transaction conflict.
     *
     * Resolution order:
     * 1. Same transaction id -> compare updatedAt.
     * 2. Same sourceId -> compare updatedAt.
     * 3. No matching identity -> remote transaction is accepted.
     *
     * The method is deterministic and does not invoke local update().
     */
    fun resolveConflict(
        local: Transaction?,
        remote: Transaction,
    ): Transaction {

        require(remote.id.isNotBlank()) {
            "Remote transaction id is required."
        }

        if (local == null) {
            return remote
        }

        val sameIdentity =
            local.id == remote.id ||
                (
                    !local.sourceId.isNullOrBlank() &&
                        !remote.sourceId.isNullOrBlank() &&
                        local.sourceId == remote.sourceId
                )

        if (!sameIdentity) {
            return remote
        }

        val localUpdated =
            local.updatedAt.takeIf {
                it > 0L
            } ?: local.createdAt

        val remoteUpdated =
            remote.updatedAt.takeIf {
                it > 0L
            } ?: remote.createdAt

        return when {
            remoteUpdated > localUpdated -> remote

            remoteUpdated < localUpdated -> local

            else -> {
                /*
                 * Deterministic tie-breaker.
                 * If timestamps are identical, keep the local
                 * version to avoid unnecessary remote churn.
                 */
                local
            }
        }
    }


    fun remove(id: String) {
        transactions.removeAll {
            it.id == id
        }

        persist()
    }

    /**
     * Registers one internal transfer between two active accounts.
     *
     * IMPORTANT:
     * The transfer is stored as ONE Transaction.
     *
     * sourceAccountId:
     *     Account from which money leaves.
     *
     * destinationAccountId:
     *     Account that receives the money.
     *
     * TransactionKind.TRANSFER is the authoritative classification.
     *
     * AccountLedger calculates:
     *
     * source      -> -amount
     * destination -> +amount
     *
     * This prevents an internal transfer from becoming two
     * independent expense transactions.
     */
    fun addTransfer(
        sourceAccountId: String,
        destinationAccountId: String,
        amount: Long,
        dateEpochMillis: Long? = null,
        note: String = "",
    ): Transaction {

        require(sourceAccountId.isNotBlank()) {
            "Source account is required."
        }

        require(destinationAccountId.isNotBlank()) {
            "Destination account is required."
        }

        require(sourceAccountId != destinationAccountId) {
            "Source and destination accounts must be different."
        }

        require(amount > 0L) {
            "Transfer amount must be greater than zero."
        }

        val sourceAccount =
            AccountStore.accounts.firstOrNull {
                it.id == sourceAccountId && it.isActive
            }
                ?: throw IllegalArgumentException(
                    "Source account does not exist or is inactive."
                )

        val destinationAccount =
            AccountStore.accounts.firstOrNull {
                it.id == destinationAccountId && it.isActive
            }
                ?: throw IllegalArgumentException(
                    "Destination account does not exist or is inactive."
                )

        val now = System.currentTimeMillis()

        val transaction =
            Transaction(
                id = UUID.randomUUID().toString(),
                title = "انتقال بین حساب‌ها",
                subtitle =
                    "${sourceAccount.name} → ${destinationAccount.name}",
                amount = amount,
                type = TransactionType.EXPENSE,
                    iconKey = "انتقال",
                // Internal transfers are not income/expense categories.
                category = null,
                dateEpochMillis = dateEpochMillis ?: now,
                note = note.trim(),
                kind = TransactionKind.TRANSFER,
                sourceAccountId = sourceAccountId,
                destinationAccountId = destinationAccountId,
                source = TransactionSource.TRANSFER,
                syncStatus = SyncStatus.LOCAL_ONLY,
            )


        val storedTransaction =
            transaction.copy(
                createdAt = now,
                updatedAt = now,
                source = TransactionSource.TRANSFER,
                syncStatus =
                    if (transaction.syncStatus == SyncStatus.SYNCED) {
                        SyncStatus.SYNCED
                    } else {
                        SyncStatus.LOCAL_ONLY
                    },
            )

        transactions.add(
            0,
            storedTransaction
        )

        persist()

        return storedTransaction
    }
    /**
     * Creates a transaction from a bank SMS and returns the exact
     * transaction that was created.
     *
     * Returning the transaction allows SMS-specific metadata to be
     * linked to the generated transaction ID without changing the
     * Transaction model itself.
     */
    fun addFromBankSms(
        bankName: String,
        amount: Long,
        merchant: String?,
        type: TransactionType,
        smsDate: Long? = null,
        sourceId: String? = null,
        kind: TransactionKind = TransactionKind.NORMAL,
    ): Transaction {

        val now = System.currentTimeMillis()
        val transaction =
            Transaction(
                id = UUID.randomUUID().toString(),
                title =
                    merchant
                        ?.takeIf { it.isNotBlank() }
                        ?: bankName,
                subtitle =
                    if (merchant != null) {
                        bankName
                    } else {
                        "تراکنش بانکی"
                    },
                amount = amount,
                type = type,
                dateEpochMillis = smsDate ?: now,
                kind = kind,
            requiresCompletion = true,
                iconKey = "بانک",
                sourceId = sourceId,
                source = TransactionSource.SMS,
                syncStatus = SyncStatus.LOCAL_ONLY,
            )


        val storedTransaction =
            transaction.copy(
                createdAt = now,
                updatedAt = now,
                source = TransactionSource.SMS,
                syncStatus =
                    if (transaction.syncStatus == SyncStatus.SYNCED) {
                        SyncStatus.SYNCED
                    } else {
                        SyncStatus.LOCAL_ONLY
                    },
            )

        transactions.add(
            0,
            storedTransaction
        )

        persist()

        return storedTransaction
    }
}








