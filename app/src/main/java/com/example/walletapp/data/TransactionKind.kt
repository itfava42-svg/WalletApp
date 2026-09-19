package com.example.walletapp.data

/**
 * Defines the financial nature of a transaction.
 *
 * NORMAL:
 *     A regular income or expense.
 *
 * TRANSFER:
 *     Money moved between two accounts owned by the user.
 *
 * TransactionType remains INCOME/EXPENSE for backward compatibility
 * with the existing SMS, budget, loan and dashboard components.
 */
enum class TransactionKind {
    NORMAL,
    TRANSFER,
}
