package com.example.walletapp.data

import androidx.compose.runtime.mutableStateListOf
import java.util.UUID

enum class BudgetPeriod(val label: String) {
    WEEKLY("هفتگی"),
    MONTHLY("ماهانه"),
    ANNUAL("سالانه"),
}

data class Budget(
    val id: String,
    val category: String,
    val amount: Long,
    val period: BudgetPeriod,
)

object BudgetStore {
    val budgets = mutableStateListOf<Budget>()

    fun add(category: String, amount: Long, period: BudgetPeriod) {
        budgets.add(0, Budget(UUID.randomUUID().toString(), category, amount, period))
    }

    fun remove(id: String) {
        budgets.removeAll { it.id == id }
    }

    /**
     * Live-computed amount spent so far against this budget, based on
     * expense transactions in [TransactionStore] whose category matches
     * (matched via the category label stored at the start of
     * [Transaction.subtitle]). Computed on demand rather than stored, so it
     * always reflects the current transaction list.
     */
    fun spentFor(budget: Budget): Long {
        return TransactionStore.transactions
            .filter {
                it.kind == TransactionKind.NORMAL &&
                    it.type == TransactionType.EXPENSE &&
                    it.subtitle.startsWith(budget.category)
            }
            .sumOf { it.amount }
    }
}


