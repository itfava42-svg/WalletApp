package com.example.walletapp.data

enum class TransactionType { INCOME, EXPENSE }

/**
 * iconKey is a stable string (a category label like "خوراک", or "بانک" for
 * SMS-sourced transactions without a category) rather than an ImageVector
     * directly — ImageVector isn't serializable, and Transaction needs to be
 * persisted to disk. Resolve the actual icon at render time with
 * [resolveTransactionIcon].
 */
data class Transaction(
    val id: String,
    val title: String,
    val subtitle: String,
    val amount: Long,
    val type: TransactionType,
    val iconKey: String,
    val tags: List<String> = emptyList(),

    // Core financial metadata.
    // Defaults keep all existing transactions backward-compatible.
    val accountId: String? = null,
    val category: String? = null,
    val dateEpochMillis: Long? = null,
    val note: String = "",
    val isRecurring: Boolean = false,

    // Financial transaction classification.
    // NORMAL preserves existing income/expense behavior.
    // TRANSFER represents money moved between two user-owned accounts.
    val kind: TransactionKind = TransactionKind.NORMAL,
    val sourceAccountId: String? = null,
    val destinationAccountId: String? = null,

    // Optional transaction details.
    // Used by loan payments and SMS transaction completion.
    val description: String = "",
    val receiptUri: String? = null,
    val requiresCompletion: Boolean = false,

    // Sync metadata.
    // Defaults preserve compatibility with existing constructors.
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val source: TransactionSource = TransactionSource.LEGACY,
    val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY,

    // Deterministic identity of the original source event.
    // For SMS this will be the shared SMS fingerprint.
    // Nullable for legacy/manual transactions.
    val sourceId: String? = null,
)




