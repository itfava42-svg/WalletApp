package com.example.walletapp.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionSyncConcurrentEditIntegrityTest {

    private fun transaction(
        id: String = "tx-1",
        syncStatus: SyncStatus = SyncStatus.PENDING,
        updatedAt: Long = 100L,
        amount: Long = 1000L
    ) =
        Transaction(
            id = id,
            title = "تست",
            subtitle = "CI-43",
            amount = amount,
            type = TransactionType.EXPENSE,
            iconKey = "test",
            createdAt = 100L,
            updatedAt = updatedAt,
            source = TransactionSource.MANUAL,
            syncStatus = syncStatus
        )

    @Test
    fun successfulUploadOfOlderSnapshotDoesNotMarkNewerLocalEditAsSynced() {
        val uploadedSnapshot = transaction()
        TransactionStore.transactions.removeAll { it.id == uploadedSnapshot.id }
        TransactionStore.transactions.add(uploadedSnapshot)

        val newerLocalEdit = uploadedSnapshot.copy(
            amount = 2000L,
            updatedAt = 200L,
            syncStatus = SyncStatus.PENDING
        )
        TransactionStore.update(newerLocalEdit)

        val marked = TransactionSyncRepository.markSynced(uploadedSnapshot)
        val current = TransactionStore.transactions.first { it.id == uploadedSnapshot.id }

        assertFalse(marked)
        assertEquals(2000L, current.amount)
        assertEquals(SyncStatus.PENDING, current.syncStatus)

        TransactionStore.remove(uploadedSnapshot.id)
    }

    @Test
    fun failedUploadOfOlderSnapshotDoesNotOverwriteNewerLocalEdit() {
        val uploadedSnapshot = transaction(id = "tx-2")
        TransactionStore.transactions.removeAll { it.id == uploadedSnapshot.id }
        TransactionStore.transactions.add(uploadedSnapshot)

        val newerLocalEdit = uploadedSnapshot.copy(
            amount = 3000L,
            updatedAt = 300L,
            syncStatus = SyncStatus.PENDING
        )
        TransactionStore.update(newerLocalEdit)

        val marked = TransactionSyncRepository.markFailed(uploadedSnapshot)
        val current = TransactionStore.transactions.first { it.id == uploadedSnapshot.id }

        assertFalse(marked)
        assertEquals(3000L, current.amount)
        assertEquals(SyncStatus.PENDING, current.syncStatus)

        TransactionStore.remove(uploadedSnapshot.id)
    }

    @Test
    fun unchangedUploadSnapshotCanBeMarkedSynced() {
        val uploadedSnapshot = transaction(id = "tx-3")
        TransactionStore.transactions.removeAll { it.id == uploadedSnapshot.id }
        TransactionStore.transactions.add(uploadedSnapshot)

        val marked = TransactionSyncRepository.markSynced(uploadedSnapshot)
        val current = TransactionStore.transactions.first { it.id == uploadedSnapshot.id }

        assertTrue(marked)
        assertEquals(SyncStatus.SYNCED, current.syncStatus)

        TransactionStore.remove(uploadedSnapshot.id)
    }
}
