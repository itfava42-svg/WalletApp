package com.example.walletapp.data

/**
 * Domain boundary for budget mutations.
 *
 * BudgetStore remains the authoritative owner of budget state,
 * persistence, and budget invariants.
 */
object BudgetManager {

    fun add(
        category: String,
        amount: Long,
        period: BudgetPeriod,
    ) {
        BudgetStore.add(
            category = category,
            amount = amount,
            period = period,
        )
    }

    fun remove(id: String) {
        BudgetStore.remove(id)
    }
}
