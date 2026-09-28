package com.example.walletapp.data

import org.json.JSONObject
import org.junit.Test
import org.junit.Assert.assertEquals

class TransactionSyncUploadResponseTest {

    @Test
    fun parsesAlreadyExistsAndUpdatedAsProcessedSuccess() {
        val response = TransactionSyncJson.uploadResponseFromJson(
            JSONObject(
                """
                {
                  "acceptedCount": 1,
                  "alreadyExistsCount": 2,
                  "updatedCount": 1,
                  "rejectedCount": 0,
                  "serverTime": 1000
                }
                """.trimIndent()
            )
        )

        assertEquals(1, response.acceptedCount)
        assertEquals(2, response.alreadyExistsCount)
        assertEquals(1, response.updatedCount)
        assertEquals(0, response.rejectedCount)
        assertEquals(4, response.processedCount)
    }

    @Test
    fun missingNewCountsRemainBackwardCompatible() {
        val response = TransactionSyncJson.uploadResponseFromJson(
            JSONObject(
                """
                {
                  "acceptedCount": 2,
                  "serverTime": 1000
                }
                """.trimIndent()
            )
        )

        assertEquals(2, response.acceptedCount)
        assertEquals(0, response.alreadyExistsCount)
        assertEquals(0, response.updatedCount)
        assertEquals(0, response.rejectedCount)
        assertEquals(2, response.processedCount)
    }
}
