package com.example.walletapp.data

import org.junit.Test
import org.junit.Assert.assertEquals

class TransactionSyncIdempotencyKeyTest {

    @Test
    fun sameUploadBatch_keepsSameKeyAcrossRetryTimestamps() {
        val client = TransactionSyncClient(
            deviceId = "test-device",
            protocolVersion = TransactionSyncProtocol.VERSION
        )

        val transactions = listOf(
            Transaction(
                id = "tx-1",
                title = "Test",
                subtitle = "",
                amount = 1000L,
                type = TransactionType.EXPENSE,
                iconKey = "test",
                createdAt = 100L,
                updatedAt = 200L
            ),
            Transaction(
                id = "tx-2",
                title = "Test 2",
                subtitle = "",
                amount = 2000L,
                type = TransactionType.INCOME,
                iconKey = "test",
                createdAt = 110L,
                updatedAt = 210L
            )
        )

        val firstAttempt = TransactionSyncIdempotencyKey.forUpload(
            client = client,
            generatedAt = 1_000L,
            transactions = transactions
        )

        val retryAttempt = TransactionSyncIdempotencyKey.forUpload(
            client = client,
            generatedAt = 2_000L,
            transactions = transactions
        )

        assertEquals(firstAttempt, retryAttempt)
    }

    @Test
    fun transactionOrdering_doesNotChangeKey() {
        val client = TransactionSyncClient(
            deviceId = "test-device",
            protocolVersion = TransactionSyncProtocol.VERSION
        )

        val first = Transaction(
            id = "tx-1",
            title = "Test",
            subtitle = "",
            amount = 1000L,
            type = TransactionType.EXPENSE,
            iconKey = "test",
            updatedAt = 200L
        )
        val second = Transaction(
            id = "tx-2",
            title = "Test 2",
            subtitle = "",
            amount = 2000L,
            type = TransactionType.INCOME,
            iconKey = "test",
            updatedAt = 210L
        )

        val forward = TransactionSyncIdempotencyKey.forUpload(
            client = client,
            generatedAt = 1_000L,
            transactions = listOf(first, second)
        )

        val reversed = TransactionSyncIdempotencyKey.forUpload(
            client = client,
            generatedAt = 1_000L,
            transactions = listOf(second, first)
        )

        assertEquals(forward, reversed)
    }
}
