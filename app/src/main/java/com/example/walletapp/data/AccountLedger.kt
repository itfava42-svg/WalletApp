package com.example.walletapp.data

/**
 * Central financial ledger for account balances.
 *
 * IMPORTANT:
 * Account.openingBalance is used only as the starting balance; current balance is derived from the ledger.
 *
 * The ledger derives balances from transactions:
 *
 * NORMAL + INCOME
 *     accountId -> +amount
 *
 * NORMAL + EXPENSE
 *     accountId -> -amount
 *
 * TRANSFER
 *     sourceAccountId      -> -amount
 *     destinationAccountId -> +amount
 *
 * Old transactions remain compatible because their kind defaults
 * to TransactionKind.NORMAL.
 */
object AccountLedger {

    /**
     * Calculates the current balance of one account
     * from the transaction history.
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

        transactions.forEach { transaction ->

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

                    if (transaction.sourceAccountId == accountId) {
                        balance -= transaction.amount
                    }

                    if (transaction.destinationAccountId == accountId) {
                        balance += transaction.amount
                    }
                }
            }
        }

        return balance
    }

    /**
     * Calculates the current balance using the exact Account
     * instance supplied by the caller.
     *
     * This keeps balances(accounts = customList) independent
     * from AccountStore.accounts.
     */
    private fun balanceOf(
        account: Account,
        transactions: List<Transaction>,
    ): Long {

        var balance = account.openingBalance

        transactions.forEach { transaction ->

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
     *
     * Current balance is derived from openingBalance plus transaction history.
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
     * Transfers between the user's own accounts cancel each other
     * automatically because one account is debited and the other
     * account is credited.
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
     * Returns only normal income transactions assigned to an account.
     */
    fun incomeForAccount(
        accountId: String,
        transactions: List<Transaction> = TransactionStore.transactions,
    ): Long {

        return transactions
            .asSequence()
            .filter {
                it.kind == TransactionKind.NORMAL &&
                    it.type == TransactionType.INCOME &&
                    it.accountId == accountId
            }
            .sumOf { it.amount }
    }

    /**
     * Returns only normal expense transactions assigned to an account.
     */
    fun expenseForAccount(
        accountId: String,
        transactions: List<Transaction> = TransactionStore.transactions,
    ): Long {

        return transactions
            .asSequence()
            .filter {
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
                it.kind == TransactionKind.TRANSFER &&
                    it.destinationAccountId == accountId
            }
            .sumOf { it.amount }
    }
}


