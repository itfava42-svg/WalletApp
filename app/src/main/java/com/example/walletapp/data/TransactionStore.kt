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

    fun addManual(transaction: Transaction) {
        transactions.add(
            0,
            transaction
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

        val transaction =
            Transaction(
                id = UUID.randomUUID().toString(),
                title = "انتقال بین حساب‌ها",
                subtitle =
                    "${sourceAccount.name} → ${destinationAccount.name}",
                amount = amount,
                type = TransactionType.EXPENSE,
                iconKey = "انتقال",
                category = "انتقال داخلی",
                dateEpochMillis = dateEpochMillis,
                note = note.trim(),
                kind = TransactionKind.TRANSFER,
                sourceAccountId = sourceAccountId,
                destinationAccountId = destinationAccountId,
            )

        transactions.add(
            0,
            transaction
        )

        persist()

        return transaction
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
    ): Transaction {

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
                requiresCompletion = true,
                iconKey = "بانک",
            )

        transactions.add(
            0,
            transaction
        )

        persist()

        return transaction
    }
}

