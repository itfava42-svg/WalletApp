package com.example.walletapp.data

/**
 * Central financial ledger for account balances.
 *
 * IMPORTANT:
 * Account.openingBalance is used only as the starting balance; current balance
 * is derived from finalized ledger transactions.
 *
 * A transaction that still requires user completion is a pending SMS review
 * item, not a finalized financial transaction. It must therefore have NO
 * effect on balances, income/expense totals, or transfer totals until the
 * completion flow sets requiresCompletion=false.
 */
object AccountLedger {

    /**
     * Calculates the current balance of one account
     * from finalized transaction history.
     */
    fun balanceOf(
        accountId: String,
        transactions: List<Transaction> = TransactionStore.transactions,
    ): Long {
        var balance =
            AccountStore.accounts
                .firstOrNull { it.id == accountId }
                ?.openingBalance
                ?: 0L

        val knownAccountIds = AccountStore.accounts.mapTo(mutableSetOf()) { it.id }

        transactions.forEach { transaction ->
            if (transaction.requiresCompletion || transaction.amount <= 0L) {
                return@forEach
            }

            when (transaction.kind) {
                TransactionKind.NORMAL -> {
                    if (transaction.accountId != accountId) {
                        return@forEach
                    }

                    balance += when (transaction.type) {
                        TransactionType.INCOME -> transaction.amount
                        TransactionType.EXPENSE -> -transaction.amount
                    }
                }

                TransactionKind.TRANSFER -> {
                    val sourceId = transaction.sourceAccountId
                    val destinationId = transaction.destinationAccountId

                    if (sourceId.isNullOrBlank() ||
                        destinationId.isNullOrBlank() ||
                        sourceId == destinationId ||
                        sourceId !in knownAccountIds ||
                        destinationId !in knownAccountIds
                    ) {
                        return@forEach
                    }

                    if (sourceId == accountId) {
                        balance -= transaction.amount
                    }

                    if (destinationId == accountId) {
                        balance += transaction.amount
                    }
                }
            }
        }

        return balance
    }

    /**
     * Calculates the current balance using the exact Account instance
     * supplied by the caller.
     */
    private fun balanceOf(
        account: Account,
        transactions: List<Transaction>,
    ): Long {
        var balance = account.openingBalance

        val knownAccountIds = AccountStore.accounts.mapTo(mutableSetOf()) { it.id }

        transactions.forEach { transaction ->
            if (transaction.requiresCompletion || transaction.amount <= 0L) {
                return@forEach
            }

            when (transaction.kind) {
                TransactionKind.NORMAL -> {
                    if (transaction.accountId != account.id) {
                        return@forEach
                    }

                    balance += when (transaction.type) {
                        TransactionType.INCOME -> transaction.amount
                        TransactionType.EXPENSE -> -transaction.amount
                    }
                }

                TransactionKind.TRANSFER -> {
                    if (transaction.sourceAccountId == account.id) {
                        balance -= transaction.amount
                    }

                    if (transaction.destinationAccountId == account.id) {
                        balance += transaction.amount
                    }
                }
            }
        }

        return balance
    }

    /**
     * Calculates balances for all active accounts.
     */
    fun balances(
        accounts: List<Account> = AccountStore.accounts,
        transactions: List<Transaction> = TransactionStore.transactions,
    ): Map<String, Long> {
        return accounts
            .filter { it.isActive }
            .associate { account ->
                account.id to balanceOf(
                    account = account,
                    transactions = transactions,
                )
            }
    }

    /**
     * Calculates the total balance of all active accounts.
     *
     * Internal transfers cancel automatically because one account is debited
     * and the other account is credited.
     */
    fun totalBalance(
        accounts: List<Account> = AccountStore.accounts,
        transactions: List<Transaction> = TransactionStore.transactions,
    ): Long {
        return balances(
            accounts = accounts,
            transactions = transactions,
        ).values.sum()
    }

    /**
     * Returns only finalized normal income transactions assigned to an account.
     */
    fun incomeForAccount(
        accountId: String,
        transactions: List<Transaction> = TransactionStore.transactions,
    ): Long {
        return transactions
            .asSequence()
            .filter {
                !it.requiresCompletion &&
                    it.kind == TransactionKind.NORMAL &&
                    it.type == TransactionType.INCOME &&
                    it.accountId == accountId
            }
            .sumOf { it.amount }
    }

    /**
     * Returns only finalized normal expense transactions assigned to an account.
     */
    fun expenseForAccount(
        accountId: String,
        transactions: List<Transaction> = TransactionStore.transactions,
    ): Long {
        return transactions
            .asSequence()
            .filter {
                !it.requiresCompletion &&
                    it.kind == TransactionKind.NORMAL &&
                    it.type == TransactionType.EXPENSE &&
                    it.accountId == accountId
            }
            .sumOf { it.amount }
    }

    /**
     * Amount transferred out of an account.
     */
    fun transferOutForAccount(
        accountId: String,
        transactions: List<Transaction> = TransactionStore.transactions,
    ): Long {
        return transactions
            .asSequence()
            .filter {
                !it.requiresCompletion &&
                    it.kind == TransactionKind.TRANSFER &&
                    it.sourceAccountId == accountId
            }
            .sumOf { it.amount }
    }

    /**
     * Amount transferred into an account.
     */
    fun transferInForAccount(
        accountId: String,
        transactions: List<Transaction> = TransactionStore.transactions,
    ): Long {
        return transactions
            .asSequence()
            .filter {
                !it.requiresCompletion &&
                    it.kind == TransactionKind.TRANSFER &&
                    it.destinationAccountId == accountId
            }
            .sumOf { it.amount }
    }
}
