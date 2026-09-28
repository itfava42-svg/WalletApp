package com.example.walletapp.data

import java.security.MessageDigest

/**
 * Deterministic idempotency key for one upload batch.
 *
 * The key contains no authentication token and no raw
 * financial payload.
 */
object TransactionSyncIdempotencyKey {

    fun forUpload(
        client: TransactionSyncClient,
        generatedAt: Long,
        transactions: List<Transaction>
    ): String {

        require(client.deviceId.isNotBlank()) {
            "Device ID is required."
        }

        require(client.protocolVersion > 0) {
            "Protocol version must be positive."
        }

        require(generatedAt >= 0L) {
            "Generated time must not be negative."
        }

        require(transactions.isNotEmpty()) {
            "Upload transactions are required."
        }

        val canonical =
            buildString {

                append("v1|")
                append(client.deviceId)
                append("|")
                append(client.protocolVersion)
                append("|")

                transactions
                    .sortedWith(
                        compareBy<Transaction> {
                            it.id
                        }.thenBy {
                            it.updatedAt
                        }
                    )
                    .forEach { transaction ->

                        append(transaction.id)
                        append(":")
                        append(transaction.updatedAt)
                        append(";")
                    }
            }

        return sha256(canonical)
    }

    private fun sha256(
        value: String
    ): String {

        val digest =
            MessageDigest.getInstance(
                "SHA-256"
            )

        return digest
            .digest(
                value.toByteArray(
                    Charsets.UTF_8
                )
            )
            .joinToString("") {
                "%02x".format(it)
            }
    }
}
