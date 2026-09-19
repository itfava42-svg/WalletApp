package com.example.walletapp.data

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import java.io.IOException
import kotlin.math.min
import kotlin.random.Random

/**
 * Retry policy for Sync network operations.
 *
 * Rules:
 * - 401 / 403 are never retried.
 * - Other 4xx errors are never retried.
 * - 5xx errors are retryable.
 * - IO/network failures are retryable.
 * - malformed server responses are not classified here;
 *   they are handled by the caller as ordinary failures.
 * - coroutine cancellation is always propagated.
 */
data class TransactionSyncRetryPolicy(
    val maxAttempts: Int = 3,
    val initialDelayMillis: Long = 500L,
    val maxDelayMillis: Long = 5000L,
    val jitterMillis: Long = 250L
) {

    init {
        require(maxAttempts >= 1) {
            "maxAttempts must be at least 1."
        }

        require(initialDelayMillis >= 0L) {
            "Initial retry delay must not be negative."
        }

        require(maxDelayMillis >= initialDelayMillis) {
            "Maximum retry delay must be >= initial delay."
        }

        require(jitterMillis >= 0L) {
            "Jitter must not be negative."
        }
    }
}

/**
 * Returns true only for transient failures that are safe
 * to retry at the HTTP transport boundary.
 */
fun isTransactionSyncRetryable(
    error: Throwable
): Boolean {

    if (error is CancellationException) {
        return false
    }

    if (error is TransactionSyncAuthException) {
        return false
    }

    if (error is TransactionSyncHttpStatusException) {

        return error.statusCode >= 500
    }

    return error is IOException
}

/**
 * Executes a suspend operation with bounded exponential backoff.
 */
suspend fun <T> transactionSyncRetry(
    policy: TransactionSyncRetryPolicy =
        TransactionSyncRetryPolicy(),
    operation: suspend () -> T
): T {

    var attempt = 1

    while (true) {

        try {

            return operation()

        } catch (error: CancellationException) {

            throw error

        } catch (error: Throwable) {

            val retryable =
                isTransactionSyncRetryable(
                    error
                )

            if (
                !retryable ||
                attempt >= policy.maxAttempts
            ) {
                throw error
            }

            val exponential =
                policy.initialDelayMillis *
                    (1L shl
                        min(
                            attempt - 1,
                            20
                        ))

            val bounded =
                min(
                    exponential,
                    policy.maxDelayMillis
                )

            val jitter =
                if (
                    policy.jitterMillis > 0L
                ) {
                    Random.nextLong(
                        policy.jitterMillis + 1L
                    )
                } else {
                    0L
                }

            delay(
                bounded + jitter
            )

            attempt++
        }
    }
}
