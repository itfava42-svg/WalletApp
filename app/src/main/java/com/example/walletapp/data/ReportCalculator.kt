package com.example.walletapp.data

/**
 * Financial reporting engine.
 *
 * IMPORTANT:
 * - Uses the same Transaction model as Dashboard and AccountLedger.
 * - Internal transfers are never counted as income or expense.
 * - Opening balances are not treated as period income.
 * - Account balances remain delegated to AccountLedger.
 *
 * This class contains calculation only. UI belongs in a separate layer.
 */
object ReportCalculator {

    data class PeriodSummary(
        val income: Long = 0L,
        val expense: Long = 0L,
        val transferIn: Long = 0L,
        val transferOut: Long = 0L,
        val transactionCount: Int = 0,
    ) {
        val net: Long
            get() = income - expense

        val cashFlowIncludingTransfers: Long
            get() = income + transferIn - expense - transferOut
    }

    data class DailyTrendPoint(
        val dayStartMillis: Long,
        val income: Long,
        val expense: Long,
    ) {
        val net: Long
            get() = income - expense
    }
    data class CategorySummary(
        val category: String,
        val income: Long,
        val expense: Long,
        val transactionCount: Int,
    )

    data class AccountSummary(
        val account: Account,
        val balance: Long,
        val income: Long,
        val expense: Long,
        val transferIn: Long,
        val transferOut: Long,
    )

    /**
     * Returns transactions whose financial date falls inside [fromMillis, toMillis].
     *
     * Both boundaries are inclusive.
     */
    fun transactionsInPeriod(
        fromMillis: Long,
        toMillis: Long,
        transactions: List<Transaction> = TransactionStore.transactions,
    ): List<Transaction> {

        require(fromMillis <= toMillis) {
            "fromMillis must be less than or equal to toMillis."
        }

        return transactions.filter { transaction ->
            val date = transaction.dateEpochMillis ?: return@filter false
            date in fromMillis..toMillis
        }
    }

    /**
     * Calculates income, expense and internal transfer totals for a period.
     */
    fun periodSummary(
        fromMillis: Long,
        toMillis: Long,
        transactions: List<Transaction> = TransactionStore.transactions,
    ): PeriodSummary {

        val periodTransactions =
            transactionsInPeriod(
                fromMillis = fromMillis,
                toMillis = toMillis,
                transactions = transactions,
            )

        var income = 0L
        var expense = 0L
        var transferIn = 0L
        var transferOut = 0L

        periodTransactions.forEach { transaction ->

            when (transaction.kind) {

                TransactionKind.NORMAL -> {
                    when (transaction.type) {
                        TransactionType.INCOME ->
                            income += transaction.amount

                        TransactionType.EXPENSE ->
                            expense += transaction.amount
                    }
                }

                TransactionKind.TRANSFER -> {
                    transferOut += transaction.amount
                    transferIn += transaction.amount
                }
            }
        }

        return PeriodSummary(
            income = income,
            expense = expense,
            transferIn = transferIn,
            transferOut = transferOut,
            transactionCount = periodTransactions.size,
        )
    }

    /**
     * Groups normal transactions by category.
     *
     * Transfers are deliberately excluded because they are not
     * financial income or expense categories.
     */
    fun dailyTrend(
        fromMillis: Long,
        toMillis: Long,
        transactions: List<Transaction> = TransactionStore.transactions,
    ): List<DailyTrendPoint> {
        if (fromMillis > toMillis) {
            return emptyList()
        }

        val fromCalendar =
            java.util.Calendar.getInstance().apply {
                timeInMillis = fromMillis
                set(
                    java.util.Calendar.HOUR_OF_DAY,
                    0,
                )
                set(
                    java.util.Calendar.MINUTE,
                    0,
                )
                set(
                    java.util.Calendar.SECOND,
                    0,
                )
                set(
                    java.util.Calendar.MILLISECOND,
                    0,
                )
            }

        val toCalendar =
            java.util.Calendar.getInstance().apply {
                timeInMillis = toMillis
                set(
                    java.util.Calendar.HOUR_OF_DAY,
                    0,
                )
                set(
                    java.util.Calendar.MINUTE,
                    0,
                )
                set(
                    java.util.Calendar.SECOND,
                    0,
                )
                set(
                    java.util.Calendar.MILLISECOND,
                    0,
                )
            }

        val totals =
            mutableMapOf<Long, Pair<Long, Long>>()

        transactions
            .asSequence()
            .filter {
                it.kind == TransactionKind.NORMAL
            }
            .forEach { transaction ->
                val timestamp =
                    transaction.dateEpochMillis
                        ?: transaction.createdAt

                if (
                    timestamp >= fromMillis &&
                    timestamp <= toMillis
                ) {
                    val day =
                        java.util.Calendar.getInstance().apply {
                            timeInMillis = timestamp
                            set(
                                java.util.Calendar.HOUR_OF_DAY,
                                0,
                            )
                            set(
                                java.util.Calendar.MINUTE,
                                0,
                            )
                            set(
                                java.util.Calendar.SECOND,
                                0,
                            )
                            set(
                                java.util.Calendar.MILLISECOND,
                                0,
                            )
                        }.timeInMillis

                    val current = totals[day] ?: (0L to 0L)

                    totals[day] =
                        when (transaction.type) {
                            TransactionType.INCOME ->
                                (current.first + transaction.amount) to current.second

                            TransactionType.EXPENSE ->
                                current.first to (current.second + transaction.amount)

                            else ->
                                current
                        }
                }
            }

        val result = mutableListOf<DailyTrendPoint>()
        val cursor =
            fromCalendar.clone() as java.util.Calendar

        while (!cursor.after(toCalendar)) {
            val dayStart = cursor.timeInMillis
            val values = totals[dayStart] ?: (0L to 0L)

            result +=
                DailyTrendPoint(
                    dayStartMillis = dayStart,
                    income = values.first,
                    expense = values.second,
                )

            cursor.add(
                java.util.Calendar.DAY_OF_MONTH,
                1,
            )
        }

        return result
    }
    fun categorySummary(
        fromMillis: Long,
        toMillis: Long,
        transactions: List<Transaction> = TransactionStore.transactions,
    ): List<CategorySummary> {

        return transactionsInPeriod(
            fromMillis = fromMillis,
            toMillis = toMillis,
            transactions = transactions,
        )
            .asSequence()
            .filter { it.kind == TransactionKind.NORMAL }
            .groupBy { it.category?.takeIf { value -> value.isNotBlank() } ?: "سایر" }
            .map { (category, items) ->

                CategorySummary(
                    category = category,
                    income =
                        items
                            .asSequence()
                            .filter { it.type == TransactionType.INCOME }
                            .sumOf { it.amount },
                    expense =
                        items
                            .asSequence()
                            .filter { it.type == TransactionType.EXPENSE }
                            .sumOf { it.amount },
                    transactionCount = items.size,
                )
            }
            .sortedByDescending {
                maxOf(it.income, it.expense)
            }
    }

    /**
     * Calculates financial statistics for every active account.
     *
     * Balance calculation is delegated to AccountLedger so Reports
     * and Dashboard use the same source of truth.
     */
    fun accountSummaries(
        fromMillis: Long? = null,
        toMillis: Long? = null,
        accounts: List<Account> = AccountStore.accounts,
        transactions: List<Transaction> = TransactionStore.transactions,
    ): List<AccountSummary> {

        val periodTransactions =
            if (fromMillis != null && toMillis != null) {
                transactionsInPeriod(
                    fromMillis = fromMillis,
                    toMillis = toMillis,
                    transactions = transactions,
                )
            } else {
                transactions
            }

        val balances =
            AccountLedger.balances(
                accounts = accounts,
                transactions = transactions,
            )

        return accounts
            .filter { it.isActive }
            .map { account ->

                AccountSummary(
                    account = account,
                    balance = balances[account.id] ?: account.openingBalance,
                    income =
                        AccountLedger.incomeForAccount(
                            accountId = account.id,
                            transactions = periodTransactions,
                        ),
                    expense =
                        AccountLedger.expenseForAccount(
                            accountId = account.id,
                            transactions = periodTransactions,
                        ),
                    transferIn =
                        AccountLedger.transferInForAccount(
                            accountId = account.id,
                            transactions = periodTransactions,
                        ),
                    transferOut =
                        AccountLedger.transferOutForAccount(
                            accountId = account.id,
                            transactions = periodTransactions,
                        ),
                )
            }
    }

    /**
     * Returns total current balance of active accounts.
     *
     * Uses exactly the same calculation as Dashboard.
     */
    fun currentTotalBalance(
        accounts: List<Account> = AccountStore.accounts,
        transactions: List<Transaction> = TransactionStore.transactions,
    ): Long {

        return AccountLedger.totalBalance(
            accounts = accounts,
            transactions = transactions,
        )
    }

    /**
     * Returns the number of normal income transactions in a period.
     */
    fun incomeTransactionCount(
        fromMillis: Long,
        toMillis: Long,
        transactions: List<Transaction> = TransactionStore.transactions,
    ): Int {

        return transactionsInPeriod(
            fromMillis,
            toMillis,
            transactions,
        ).count {
            it.kind == TransactionKind.NORMAL &&
                it.type == TransactionType.INCOME
        }
    }

    /**
     * Returns the number of normal expense transactions in a period.
     */
    fun expenseTransactionCount(
        fromMillis: Long,
        toMillis: Long,
        transactions: List<Transaction> = TransactionStore.transactions,
    ): Int {

        return transactionsInPeriod(
            fromMillis,
            toMillis,
            transactions,
        ).count {
            it.kind == TransactionKind.NORMAL &&
                it.type == TransactionType.EXPENSE
        }
    }

    /**
     * Returns only income transactions in a period.
     */
    fun incomeTransactions(
        fromMillis: Long,
        toMillis: Long,
        transactions: List<Transaction> = TransactionStore.transactions,
    ): List<Transaction> {

        return transactionsInPeriod(
            fromMillis,
            toMillis,
            transactions,
        ).filter {
            it.kind == TransactionKind.NORMAL &&
                it.type == TransactionType.INCOME
        }
    }

    /**
     * Returns only expense transactions in a period.
     */
    fun expenseTransactions(
        fromMillis: Long,
        toMillis: Long,
        transactions: List<Transaction> = TransactionStore.transactions,
    ): List<Transaction> {

        return transactionsInPeriod(
            fromMillis,
            toMillis,
            transactions,
        ).filter {
            it.kind == TransactionKind.NORMAL &&
                it.type == TransactionType.EXPENSE
        }
    }

    /**
     * Returns internal transfers in a period.
     */
    fun transferTransactions(
        fromMillis: Long,
        toMillis: Long,
        transactions: List<Transaction> = TransactionStore.transactions,
    ): List<Transaction> {

        return transactionsInPeriod(
            fromMillis,
            toMillis,
            transactions,
        ).filter {
            it.kind == TransactionKind.TRANSFER
        }
    }
}

