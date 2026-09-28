package com.example.walletapp.data

import java.io.IOException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionSyncRetryPolicyTest {

    @Test
    fun retryableFailures_areClassifiedCorrectly() {
        assertTrue(isTransactionSyncRetryable(IOException("network")))
        assertTrue(
            isTransactionSyncRetryable(
                TransactionSyncHttpStatusException(500, "")
            )
        )
        assertFalse(
            isTransactionSyncRetryable(
                TransactionSyncHttpStatusException(400, "")
            )
        )
    }

    @Test
    fun retryPolicy_retriesTransientIoFailureUntilSuccess() = runBlocking {
        var attempts = 0

        val result = transactionSyncRetry(
            policy = TransactionSyncRetryPolicy(
                maxAttempts = 3,
                initialDelayMillis = 0L,
                maxDelayMillis = 0L,
                jitterMillis = 0L
            )
        ) {
            attempts++
            if (attempts < 3) throw IOException("temporary failure")
            "success"
        }

        assertEquals("success", result)
        assertEquals(3, attempts)
    }

    @Test
    fun retryPolicy_stopsAfterMaximumAttempts() = runBlocking {
        var attempts = 0

        try {
            transactionSyncRetry(
                policy = TransactionSyncRetryPolicy(
                    maxAttempts = 3,
                    initialDelayMillis = 0L,
                    maxDelayMillis = 0L,
                    jitterMillis = 0L
                )
            ) {
                attempts++
                throw IOException("persistent failure")
            }
            throw AssertionError("Expected retry operation to fail.")
        } catch (_: IOException) {
            assertEquals(3, attempts)
        }
    }
}
