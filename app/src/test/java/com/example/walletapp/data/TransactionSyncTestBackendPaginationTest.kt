package com.example.walletapp.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionSyncTestBackendPaginationTest {

    private val client =
        TransactionSyncClient(
            deviceId = "ci-39-device",
            protocolVersion = TransactionSyncProtocol.VERSION
        )

    private fun transaction(
        id: String,
        updatedAt: Long,
        amount: Long = 1000L
    ) =
        Transaction(
            id = id,
            title = "تست",
            subtitle = "CI-39",
            amount = amount,
            type = TransactionType.EXPENSE,
            iconKey = "test",
            accountId = "account-1",
            createdAt = updatedAt,
            updatedAt = updatedAt,
            source = TransactionSource.MANUAL,
            syncStatus = SyncStatus.PENDING
        )

    @Test
    fun downloadPaginatesWithStrictlyAdvancingCursor() = kotlinx.coroutines.runBlocking {
        val backend = TransactionSyncTestBackend()

        val firstPage =
            (1..TransactionSyncProtocol.MAX_DOWNLOAD_BATCH_SIZE)
                .map { index ->
                    transaction(
                        id = "tx-%03d".format(index),
                        updatedAt = index.toLong()
                    )
                }

        val finalTransaction =
            transaction(
                id = "tx-501",
                updatedAt = 501L
            )

        backend.upload(
            TransactionSyncUploadRequest(
                client = client,
                generatedAt = 1000L,
                transactions = firstPage
            )
        ).getOrThrow()

        backend.upload(
            TransactionSyncUploadRequest(
                client = client,
                generatedAt = 1001L,
                transactions = listOf(finalTransaction)
            )
        ).getOrThrow()

        val pageOne =
            backend.download(
                TransactionSyncDownloadRequest(
                    client = client,
                    cursor = TransactionSyncCursor.START
                )
            ).getOrThrow()

        assertEquals(
            TransactionSyncProtocol.MAX_DOWNLOAD_BATCH_SIZE,
            pageOne.transactions.size
        )
        assertEquals(500L, pageOne.nextUpdatedAfter)
        assertEquals("tx-500", pageOne.nextUpdatedAfterId)

        val pageTwo =
            backend.download(
                TransactionSyncDownloadRequest(
                    client = client,
                    cursor = pageOne.nextCursor()
                )
            ).getOrThrow()

        assertEquals(1, pageTwo.transactions.size)
        assertEquals("tx-501", pageTwo.transactions.single().id)
        assertTrue(
            compareCursor(
                pageTwo.nextCursor(),
                pageOne.nextCursor()
            ) > 0
        )
    }

    @Test
    fun olderUploadDoesNotOverwriteNewerRemoteTransaction() = kotlinx.coroutines.runBlocking {
        val backend = TransactionSyncTestBackend()

        val newer =
            transaction(
                id = "same-id",
                updatedAt = 200L,
                amount = 2000L
            )

        val older =
            transaction(
                id = "same-id",
                updatedAt = 100L,
                amount = 1000L
            )

        backend.upload(
            TransactionSyncUploadRequest(
                client = client,
                generatedAt = 2000L,
                transactions = listOf(newer)
            )
        ).getOrThrow()

        backend.upload(
            TransactionSyncUploadRequest(
                client = client,
                generatedAt = 2001L,
                transactions = listOf(older)
            )
        ).getOrThrow()

        val remote =
            backend.remoteSnapshot()

        assertEquals(1, remote.size)
        assertEquals(200L, remote.single().updatedAt)
        assertEquals(2000L, remote.single().amount)
        assertEquals(SyncStatus.SYNCED, remote.single().syncStatus)
    }

    private fun compareCursor(
        a: TransactionSyncCursor,
        b: TransactionSyncCursor
    ): Int {
        val timeCompare = a.updatedAt.compareTo(b.updatedAt)
        if (timeCompare != 0) {
            return timeCompare
        }
        return a.id.compareTo(b.id)
    }
}
