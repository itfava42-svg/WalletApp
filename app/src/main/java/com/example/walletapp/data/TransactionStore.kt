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

    fun update(transaction: Transaction) {
        val index =
            transactions.indexOfFirst {
                it.id == transaction.id
            }

        if (index >= 0) {
            transactions[index] = transaction
            persist()
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
                title = "ط·آ§ط¸â€ ط·ع¾ط¸â€ڑط·آ§ط¸â€‍ ط·آ¨ط؛إ’ط¸â€  ط·آ­ط·آ³ط·آ§ط·آ¨أ¢â‚¬إ’ط¸â€،ط·آ§",
                subtitle =
                    "${sourceAccount.name} أ¢â€ â€™ ${destinationAccount.name}",
                amount = amount,
                type = TransactionType.EXPENSE,
                iconKey = "ط·آ§ط¸â€ ط·ع¾ط¸â€ڑط·آ§ط¸â€‍",
                category = "ط·آ§ط¸â€ ط·ع¾ط¸â€ڑط·آ§ط¸â€‍ ط·آ¯ط·آ§ط·آ®ط¸â€‍ط؛إ’",
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
                        "ط·ع¾ط·آ±ط·آ§ط¹آ©ط¸â€ ط·آ´ ط·آ¨ط·آ§ط¸â€ ط¹آ©ط؛إ’"
                    },
                amount = amount,
                type = type,
                dateEpochMillis = smsDate ?: now,
            requiresCompletion = true,
                iconKey = "ط·آ¨ط·آ§ط¸â€ ط¹آ©",
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



