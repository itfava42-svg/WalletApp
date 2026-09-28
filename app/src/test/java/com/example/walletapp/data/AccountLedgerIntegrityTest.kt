package com.example.walletapp.data

import org.junit.Assert.assertEquals
import org.junit.Test

class AccountLedgerIntegrityTest {

    private val source = Account(
        id = "source",
        name = "حساب مبدا",
        type = AccountType.BANK,
        openingBalance = 1_000_000L,
    )

    private val destination = Account(
        id = "destination",
        name = "حساب مقصد",
        type = AccountType.BANK,
        openingBalance = 500_000L,
    )

    private fun transaction(
        id: String,
        accountId: String? = null,
        type: TransactionType = TransactionType.EXPENSE,
        amount: Long = 100_000L,
        kind: TransactionKind = TransactionKind.NORMAL,
        sourceAccountId: String? = null,
        destinationAccountId: String? = null,
        category: String? = "عمومی",
    ) = Transaction(
        id = id,
        title = "تست",
        subtitle = "تست",
        amount = amount,
        type = type,
        iconKey = "تست",
        accountId = accountId,
        kind = kind,
        sourceAccountId = sourceAccountId,
        destinationAccountId = destinationAccountId,
        category = category,
    )

    @Test
    fun normalIncomeAndExpenseAffectOnlyTheirAccount() {
        val transactions = listOf(
            transaction(
                id = "income",
                accountId = source.id,
                type = TransactionType.INCOME,
                amount = 300_000L,
            ),
            transaction(
                id = "expense",
                accountId = source.id,
                type = TransactionType.EXPENSE,
                amount = 100_000L,
            ),
        )

        val balances = AccountLedger.balances(
            accounts = listOf(source, destination),
            transactions = transactions,
        )

        assertEquals(1_200_000L, balances[source.id])
        assertEquals(500_000L, balances[destination.id])
    }

    @Test
    fun transferMovesMoneyWithoutChangingTotalBalance() {
        val transfer = transaction(
            id = "transfer",
            amount = 250_000L,
            kind = TransactionKind.TRANSFER,
            sourceAccountId = source.id,
            destinationAccountId = destination.id,
            category = "نباید ثبت شود",
        )

        val balances = AccountLedger.balances(
            accounts = listOf(source, destination),
            transactions = listOf(transfer),
        )

        assertEquals(750_000L, balances[source.id])
        assertEquals(750_000L, balances[destination.id])
        assertEquals(
            1_500_000L,
            AccountLedger.totalBalance(
                accounts = listOf(source, destination),
                transactions = listOf(transfer),
            ),
        )
    }

    @Test
    fun transferCategoryDoesNotCreateIncomeOrExpenseTotals() {
        val transfer = transaction(
            id = "transfer",
            amount = 250_000L,
            kind = TransactionKind.TRANSFER,
            sourceAccountId = source.id,
            destinationAccountId = destination.id,
            category = "هزینه اشتباه",
        )

        assertEquals(
            0L,
            AccountLedger.incomeForAccount(
                source.id,
                listOf(transfer),
            ),
        )
        assertEquals(
            0L,
            AccountLedger.expenseForAccount(
                source.id,
                listOf(transfer),
            ),
        )
        assertEquals(
            250_000L,
            AccountLedger.transferOutForAccount(
                source.id,
                listOf(transfer),
            ),
        )
        assertEquals(
            250_000L,
            AccountLedger.transferInForAccount(
                destination.id,
                listOf(transfer),
            ),
        )
    }
}
