package com.example.walletapp.data

import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionSyncRemoteConflictIntegrityTest {

    private fun transaction(
        id: String,
        sourceId: String? = null,
        updatedAt: Long,
        amount: Long
    ) =
        Transaction(
            id = id,
            title = "تست",
            subtitle = "CI-40",
            amount = amount,
            type = TransactionType.EXPENSE,
            iconKey = "test",
            accountId = "account-1",
            sourceId = sourceId,
            createdAt = 100L,
            updatedAt = updatedAt,
            source = TransactionSource.MANUAL,
            syncStatus = SyncStatus.SYNCED
        )

    @Test
    fun newerRemoteWinsForSameTransactionId() {
        val local =
            transaction(
                id = "tx-1",
                updatedAt = 100L,
                amount = 1000L
            )
        val remote =
            transaction(
                id = "tx-1",
                updatedAt = 200L,
                amount = 2000L
            )

        val resolved =
            TransactionStore.resolveConflict(
                local = local,
                remote = remote
            )

        assertEquals(2000L, resolved.amount)
        assertEquals(200L, resolved.updatedAt)
    }

    @Test
    fun newerLocalWinsForSameSourceEventWithDifferentId() {
        val local =
            transaction(
                id = "local-id",
                sourceId = "sms-1",
                updatedAt = 300L,
                amount = 3000L
            )
        val remote =
            transaction(
                id = "remote-id",
                sourceId = "sms-1",
                updatedAt = 200L,
                amount = 2000L
            )

        val resolved =
            TransactionStore.resolveConflict(
                local = local,
                remote = remote
            )

        assertEquals("local-id", resolved.id)
        assertEquals(3000L, resolved.amount)
    }

    @Test
    fun equalTimestampsKeepLocalDeterministically() {
        val local =
            transaction(
                id = "tx-1",
                updatedAt = 200L,
                amount = 1000L
            )
        val remote =
            transaction(
                id = "tx-1",
                updatedAt = 200L,
                amount = 9000L
            )

        val resolved =
            TransactionStore.resolveConflict(
                local = local,
                remote = remote
            )

        assertEquals(1000L, resolved.amount)
    }

    @Test
    fun unrelatedIdentityAcceptsRemote() {
        val local =
            transaction(
                id = "local-id",
                sourceId = "sms-local",
                updatedAt = 300L,
                amount = 3000L
            )
        val remote =
            transaction(
                id = "remote-id",
                sourceId = "sms-remote",
                updatedAt = 200L,
                amount = 2000L
            )

        val resolved =
            TransactionStore.resolveConflict(
                local = local,
                remote = remote
            )

        assertEquals("remote-id", resolved.id)
        assertEquals(2000L, resolved.amount)
    }
}
