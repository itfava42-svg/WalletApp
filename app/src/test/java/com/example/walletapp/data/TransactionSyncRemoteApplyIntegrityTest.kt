package com.example.walletapp.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionSyncRemoteApplyIntegrityTest {

    private fun transaction(
        id: String,
        sourceId: String? = null,
        updatedAt: Long,
        amount: Long
    ) =
        Transaction(
            id = id,
            title = "CI-44",
            subtitle = "Remote Apply",
            amount = amount,
            type = TransactionType.EXPENSE,
            iconKey = "test",
            accountId = "ci44-account",
            sourceId = sourceId,
            createdAt = 100L,
            updatedAt = updatedAt,
            source = TransactionSource.MANUAL,
            syncStatus = SyncStatus.SYNCED
        )

    @Test
    fun olderRemoteDoesNotReplaceNewerLocalTransaction() {
        val local =
            transaction(
                id = "ci44-local-newer",
                updatedAt = 300L,
                amount = 3000L
            )

        val remote =
            transaction(
                id = "ci44-local-newer",
                updatedAt = 200L,
                amount = 2000L
            )

        TransactionStore.applyRemote(local)

        TransactionSyncEngine.applyRemote(remote)

        val resolved =
            TransactionStore.transactions.first {
                it.id == "ci44-local-newer"
            }

        assertEquals(3000L, resolved.amount)
        assertEquals(300L, resolved.updatedAt)

        TransactionStore.remove("ci44-local-newer")
    }

    @Test
    fun newerRemoteReplacesOlderLocalTransaction() {
        val local =
            transaction(
                id = "ci44-remote-newer",
                updatedAt = 200L,
                amount = 2000L
            )

        val remote =
            transaction(
                id = "ci44-remote-newer",
                updatedAt = 300L,
                amount = 3000L
            )

        TransactionStore.applyRemote(local)

        TransactionSyncEngine.applyRemote(remote)

        val resolved =
            TransactionStore.transactions.first {
                it.id == "ci44-remote-newer"
            }

        assertEquals(3000L, resolved.amount)
        assertEquals(300L, resolved.updatedAt)

        TransactionStore.remove("ci44-remote-newer")
    }

    @Test
    fun independentRemoteTransactionIsAccepted() {
        val remote =
            transaction(
                id = "ci44-independent",
                sourceId = "remote-event",
                updatedAt = 200L,
                amount = 2000L
            )

        TransactionSyncEngine.applyRemote(remote)

        assertTrue(
            TransactionStore.transactions.any {
                it.id == "ci44-independent" &&
                    it.amount == 2000L
            }
        )

        TransactionStore.remove("ci44-independent")
    }
}

