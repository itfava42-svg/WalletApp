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
)