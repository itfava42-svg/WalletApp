package com.example.walletapp.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionSyncValidationTest {

    private fun normal(
        accountId: String? = "account-1",
        createdAt: Long = 100L,
        updatedAt: Long = 200L
    ) = Transaction(
        id = "tx-1",
        title = "Test",
        subtitle = "",
        amount = 1000L,
        type = TransactionType.EXPENSE,
        iconKey = "test",
        accountId = accountId,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    @Test
    fun validNormalTransaction_isAccepted() {
        assertTrue(
            TransactionSyncValidation.validateTransactionForSync(
                normal()
            )
        )
    }

    @Test
    fun normalTransactionWithoutAccount_isRejected() {
        assertFalse(
            TransactionSyncValidation.validateTransactionForSync(
                normal(accountId = null)
            )
        )
    }

    @Test
    fun updatedBeforeCreated_isRejected() {
        assertFalse(
            TransactionSyncValidation.validateTransactionForSync(
                normal(createdAt = 200L, updatedAt = 100L)
            )
        )
    }

    @Test
    fun transferWithoutBothAccounts_isRejected() {
        val transfer = normal().copy(
            kind = TransactionKind.TRANSFER,
            accountId = null,
            sourceAccountId = "account-1",
            destinationAccountId = null
        )

        assertFalse(
            TransactionSyncValidation.validateTransactionForSync(
                transfer
            )
        )
    }

    @Test
    fun transferToSameAccount_isRejected() {
        val transfer = normal().copy(
            kind = TransactionKind.TRANSFER,
            accountId = null,
            sourceAccountId = "account-1",
            destinationAccountId = "account-1"
        )

        assertFalse(
            TransactionSyncValidation.validateTransactionForSync(
                transfer
            )
        )
    }

    @Test
    fun validTransfer_isAccepted() {
        val transfer = normal().copy(
            kind = TransactionKind.TRANSFER,
            accountId = null,
            sourceAccountId = "account-1",
            destinationAccountId = "account-2"
        )

        assertTrue(
            TransactionSyncValidation.validateTransactionForSync(
                transfer
            )
        )
    }

    @Test
    fun batchRejectsWhenAnyTransactionIsInvalid() {
        val valid = normal()
        val invalid = normal(accountId = null)

        assertFalse(
            TransactionSyncValidation.validateBatch(
                listOf(valid, invalid)
            )
        )
    }
}
