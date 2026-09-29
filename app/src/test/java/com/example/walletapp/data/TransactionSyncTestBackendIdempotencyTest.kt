package com.example.walletapp.data

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionSyncTestBackendIdempotencyTest {

    @Test
    fun sameTransactionsWithDifferentGeneratedAtReuseStableIdempotencyRecord() =
        runBlocking {
            val backend = TransactionSyncTestBackend()

            val transaction = Transaction(
                id = "tx-idempotent-1",
                title = "Test",
                subtitle = "",
                amount = 1000L,
                type = TransactionType.EXPENSE,
                iconKey = "test",
                accountId = "account-1",
                createdAt = 100L,
                updatedAt = 200L,
            )

            val client = TransactionSyncClient(
                deviceId = "device-1",
                protocolVersion = TransactionSyncProtocol.VERSION,
            )

            val first = backend.upload(
                TransactionSyncUploadRequest(
                    client = client,
                    generatedAt = 1_000L,
                    transactions = listOf(transaction),
                )
            ).getOrThrow()

            val retry = backend.upload(
                TransactionSyncUploadRequest(
                    client = client,
                    generatedAt = 9_000L,
                    transactions = listOf(transaction),
                )
            ).getOrThrow()

            assertEquals(first, retry)
            assertEquals(1, backend.idempotencyRecordCount())
            assertEquals(1, backend.remoteSnapshot().size)
            assertTrue(
                backend.remoteSnapshot()
                    .single()
                    .syncStatus == SyncStatus.SYNCED
            )
        }
}
