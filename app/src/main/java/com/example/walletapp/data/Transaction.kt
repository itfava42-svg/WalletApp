package com.example.walletapp.data

enum class TransactionType { INCOME, EXPENSE }

/**
 * iconKey is a stable string (a category label like "ط·آ®ط¸ث†ط·آ±ط·آ§ط¹آ©", or "ط·آ¨ط·آ§ط¸â€ ط¹آ©" for
 * SMS-sourced transactions without a category) rather than an ImageVector
 * directly أ¢â‚¬â€‌ ImageVector isn't serializable, and Transaction needs to be
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
)

