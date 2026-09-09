package com.example.walletapp.data

enum class TransactionType { INCOME, EXPENSE }

/**
 * iconKey is a stable string (a category label like "ط®ظˆط±ط§ع©", or "ط¨ط§ظ†ع©" for
 * SMS-sourced transactions without a category) rather than an ImageVector
 * directly â€” ImageVector isn't serializable, and Transaction needs to be
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

    // Optional transaction details.
    // Used by loan payments and SMS transaction completion.
    val description: String = "",
    val receiptUri: String? = null,
    val requiresCompletion: Boolean = false,
)