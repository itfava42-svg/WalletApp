package com.example.walletapp.data

import org.json.JSONObject
import org.junit.Test
import org.junit.Assert.assertEquals

class TransactionSyncUploadResponseTest {

    @Test
    fun parsesAlreadyExistsAndUpdatedAsProcessedSuccess() {
        val response = TransactionSyncJson.uploadResponseFromJson(
            JSONObject().apply {
                put("acceptedCount", 1)
                put("alreadyExistsCount", 2)
                put("updatedCount", 1)
                put("rejectedCount", 0)
                put("serverTime", 1000)
            }
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
            JSONObject().apply {
                put("acceptedCount", 2)
                put("serverTime", 1000)
            }
        )

        assertEquals(2, response.acceptedCount)
        assertEquals(0, response.alreadyExistsCount)
        assertEquals(0, response.updatedCount)
        assertEquals(0, response.rejectedCount)
        assertEquals(2, response.processedCount)
    }
}
