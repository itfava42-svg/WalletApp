package com.example.walletapp.data

import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionSyncUploadResponseTest {

    @Test
    fun acceptedAlreadyExistsAndUpdated_areProcessedTogether() {
        val response = TransactionSyncServerUploadResult(
            acceptedCount = 1,
            alreadyExistsCount = 2,
            updatedCount = 1,
            rejectedCount = 0,
            serverTime = 1000L
        )

        assertEquals(4, response.processedCount)
    }

    @Test
    fun missingNewCounts_areBackwardCompatible() {
        val response = TransactionSyncServerUploadResult(
            acceptedCount = 2,
            alreadyExistsCount = 0,
            updatedCount = 0,
            rejectedCount = 0,
            serverTime = 1000L
        )

        assertEquals(2, response.processedCount)
    }
}
