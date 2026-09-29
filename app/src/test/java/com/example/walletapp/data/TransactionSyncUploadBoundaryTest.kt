package com.example.walletapp.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionSyncUploadBoundaryTest {

    private fun transaction(
        id: String,
        updatedAt: Long,
        syncStatus: SyncStatus = SyncStatus.PENDING,
        requiresCompletion: Boolean = false
    ) =
        Transaction(
            id = id,
            title = "تست",
            subtitle = "CI-42",
            amount = 1000L,
            type = TransactionType.EXPENSE,
            iconKey = "test",
            accountId = "account-1",
            createdAt = updatedAt,
            updatedAt = updatedAt,
            source = TransactionSource.MANUAL,
            syncStatus = syncStatus,
            requiresCompletion = requiresCompletion
        )

    @Test
    fun uploadBatchExcludesTransactionsRequiringCompletion() {
        val incomplete =
            transaction(
                id = "sms-incomplete",
                updatedAt = 100L,
                requiresCompletion = true
            )
        val complete =
            transaction(
                id = "manual-complete",
                updatedAt = 200L
            )

        TransactionStore.transactions.add(incomplete)
        TransactionStore.transactions.add(complete)

        try {
            val batch =
                TransactionSyncEngine.createUploadBatch()

            assertEquals(
                listOf("manual-complete"),
                batch.transactions.map { it.id }
            )
            assertTrue(batch.transactions.none { it.requiresCompletion })
        } finally {
            TransactionStore.transactions.removeAll {
                it.id == "sms-incomplete" ||
                    it.id == "manual-complete"
            }
        }
    }

    @Test
    fun uploadBatchIsDeterministicallyOrderedByUpdatedAtThenId() {
        val later =
            transaction(
                id = "z-last",
                updatedAt = 200L
            )
        val sameTimeB =
            transaction(
                id = "b-second",
                updatedAt = 100L
            )
        val sameTimeA =
            transaction(
                id = "a-first",
                updatedAt = 100L,
                syncStatus = SyncStatus.FAILED
            )

        TransactionStore.transactions.add(later)
        TransactionStore.transactions.add(sameTimeB)
        TransactionStore.transactions.add(sameTimeA)

        try {
            val batch =
                TransactionSyncEngine.createUploadBatch()

            assertEquals(
                listOf(
                    "a-first",
                    "b-second",
                    "z-last"
                ),
                batch.transactions.map { it.id }
            )
            assertFalse(
                batch.transactions.any {
                    it.syncStatus == SyncStatus.SYNCED
                }
            )
        } finally {
            TransactionStore.transactions.removeAll {
                it.id == "z-last" ||
                    it.id == "b-second" ||
                    it.id == "a-first"
            }
        }
    }
}
