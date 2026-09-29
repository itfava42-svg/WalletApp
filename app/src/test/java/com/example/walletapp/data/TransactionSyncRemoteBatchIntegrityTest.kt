package com.example.walletapp.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionSyncRemoteBatchIntegrityTest {

    private fun transaction(
        id: String,
        accountId: String? = "account-1",
        updatedAt: Long = 100L
    ) =
        Transaction(
            id = id,
            title = "تست",
            subtitle = "CI-41",
            amount = 1000L,
            type = TransactionType.EXPENSE,
            iconKey = "test",
            accountId = accountId,
            createdAt = updatedAt,
            updatedAt = updatedAt,
            source = TransactionSource.MANUAL,
            syncStatus = SyncStatus.SYNCED
        )

    @Test
    fun invalidRemoteBatchDoesNotPartiallyApply() {
        val valid =
            transaction(id = "remote-valid")

        val invalid =
            transaction(
                id = "remote-invalid",
                accountId = null
            )

        val before =
            TransactionStore.transactions.size

        val result =
            runCatching {
                TransactionSyncEngine.applyRemoteBatch(
                    listOf(valid, invalid)
                )
            }

        assertTrue(result.isFailure)
        assertEquals(
            before,
            TransactionStore.transactions.size
        )
        assertTrue(
            TransactionStore.transactions.none {
                it.id == "remote-valid"
            }
        )
    }

    @Test
    fun validRemoteBatchAppliesAllTransactions() {
        val first =
            transaction(id = "remote-1")
        val second =
            transaction(id = "remote-2", updatedAt = 200L)

        TransactionSyncEngine.applyRemoteBatch(
            listOf(first, second)
        )

        assertTrue(
            TransactionStore.transactions.any {
                it.id == "remote-1"
            }
        )
        assertTrue(
            TransactionStore.transactions.any {
                it.id == "remote-2"
            }
        )

        TransactionStore.remove("remote-1")
        TransactionStore.remove("remote-2")
    }
}
