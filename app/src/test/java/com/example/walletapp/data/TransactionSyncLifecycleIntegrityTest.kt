package com.example.walletapp.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TransactionSyncLifecycleIntegrityTest {

    @Before
    fun setUp() {
        TransactionStore.transactions.clear()
    }

    @Test
    fun pendingTransactionsIncludesOnlyUploadEligibleStatuses() {
        val localOnly = fixture(
            id = "local-only",
            syncStatus = SyncStatus.LOCAL_ONLY
        )

        val pending = fixture(
            id = "pending",
            syncStatus = SyncStatus.PENDING
        )

        val failed = fixture(
            id = "failed",
            syncStatus = SyncStatus.FAILED
        )

        val synced = fixture(
            id = "synced",
            syncStatus = SyncStatus.SYNCED
        )

        TransactionStore.transactions.addAll(
            listOf(
                localOnly,
                pending,
                failed,
                synced
            )
        )

        val result =
            TransactionSyncRepository
                .pendingTransactions()
                .map { it.id }
                .toSet()

        assertEquals(
            setOf(
                "local-only",
                "pending",
                "failed"
            ),
            result
        )

        assertFalse(
            result.contains("synced")
        )
    }

    @Test
    fun uploadBatchExcludesIncompleteSmsAndSortsDeterministically() {
        val older =
            fixture(
                id = "b",
                syncStatus = SyncStatus.PENDING,
                updatedAt = 100L
            )

        val sameTimeLowerId =
            fixture(
                id = "a",
                syncStatus = SyncStatus.LOCAL_ONLY,
                updatedAt = 200L
            )

        val sameTimeHigherId =
            fixture(
                id = "c",
                syncStatus = SyncStatus.FAILED,
                updatedAt = 200L
            )

        val incompleteSms =
            fixture(
                id = "sms-incomplete",
                syncStatus = SyncStatus.LOCAL_ONLY,
                updatedAt = 50L,
                requiresCompletion = true
            )

        val synced =
            fixture(
                id = "synced",
                syncStatus = SyncStatus.SYNCED,
                updatedAt = 1L
            )

        TransactionStore.transactions.addAll(
            listOf(
                synced,
                sameTimeHigherId,
                incompleteSms,
                older,
                sameTimeLowerId
            )
        )

        val result =
            TransactionSyncEngine
                .createUploadBatch()
                .transactions
                .map { it.id }

        assertEquals(
            listOf(
                "b",
                "a",
                "c"
            ),
            result
        )
    }

    @Test
    fun syncedAndFailedLocalEditsBecomePending() {
        val synced =
            fixture(
                id = "synced-edit",
                syncStatus = SyncStatus.SYNCED
            )

        val failed =
            fixture(
                id = "failed-edit",
                syncStatus = SyncStatus.FAILED
            )

        TransactionStore.transactions.addAll(
            listOf(
                synced,
                failed
            )
        )

        TransactionStore.update(
            synced.copy(
                note = "edited locally"
            )
        )

        TransactionStore.update(
            failed.copy(
                note = "retried locally"
            )
        )

        val currentSynced =
            TransactionStore.transactions
                .first { it.id == "synced-edit" }

        val currentFailed =
            TransactionStore.transactions
                .first { it.id == "failed-edit" }

        assertEquals(
            SyncStatus.PENDING,
            currentSynced.syncStatus
        )

        assertEquals(
            SyncStatus.PENDING,
            currentFailed.syncStatus
        )

        assertTrue(
            TransactionSyncRepository
                .pendingTransactions()
                .map { it.id }
                .containsAll(
                    listOf(
                        "synced-edit",
                        "failed-edit"
                    )
                )
        )
    }

    private fun fixture(
        id: String,
        syncStatus: SyncStatus,
        updatedAt: Long = 100L,
        requiresCompletion: Boolean = false
    ): Transaction {
        return Transaction(
            id = id,
            amount = 1000L,
            type = TransactionType.EXPENSE,
            category = "test",
            title = "CI-45 Test Transaction",
            subtitle = "Lifecycle integrity test",
            iconKey = "expense",
            dateEpochMillis = updatedAt,
            updatedAt = updatedAt,
            source = TransactionSource.MANUAL,
            syncStatus = syncStatus,
            requiresCompletion = requiresCompletion
        )
    }
}

