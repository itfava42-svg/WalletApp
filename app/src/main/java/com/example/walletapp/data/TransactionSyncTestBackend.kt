package com.example.walletapp.data

class TransactionSyncTestBackend :
    TransactionSyncBackend {

    private val remoteTransactions =
        mutableListOf<Transaction>()

    /**
     * Server-side idempotency records.
     *
     * Key -> canonical request fingerprint + response.
     *
     * The real HTTP server is expected to perform this operation
     * using the Idempotency-Key request header.
     *
     * This TestBackend derives the same deterministic key from the
     * request because the current TransactionSyncBackend interface
     * does not expose HTTP headers.
     */
    private data class IdempotencyRecord(
        val requestFingerprint: String,
        val response: TransactionSyncUploadResponse
    )

    private val idempotencyRecords =
        mutableMapOf<String, IdempotencyRecord>()

    private fun requestFingerprint(
        request: TransactionSyncUploadRequest
    ): String {

        return buildString {

            append(request.client.deviceId)
            append("|")
            append(request.client.protocolVersion)
            append("|")
            append(request.generatedAt)
            append("|")

            request.transactions
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
    }

    override suspend fun upload(
        request: TransactionSyncUploadRequest
    ): Result<TransactionSyncUploadResponse> {

        val idempotencyKey =
            TransactionSyncIdempotencyKey
                .forUpload(
                    client = request.client,
                    generatedAt = request.generatedAt,
                    transactions = request.transactions
                )

        val fingerprint =
            requestFingerprint(request)

        val existing =
            idempotencyRecords[idempotencyKey]

        if (existing != null) {

            if (
                existing.requestFingerprint !=
                    fingerprint
            ) {

                return Result.failure(
                    IllegalStateException(
                        "Idempotency key was reused for a different request."
                    )
                )
            }

            return Result.success(
                existing.response
            )
        }

        request.transactions.forEach { incoming ->

            val incomingSourceId =
                incoming.sourceId
                    ?.takeIf {
                        it.isNotBlank()
                    }

            val index =
                remoteTransactions
                    .indexOfFirst { existingTransaction ->

                        existingTransaction.id ==
                            incoming.id ||

                            (
                                incomingSourceId != null &&
                                    !existingTransaction.sourceId.isNullOrBlank() &&
                                    existingTransaction.sourceId ==
                                        incomingSourceId
                            )
                    }

            if (index >= 0) {

                val existingTransaction =
                    remoteTransactions[index]

                if (
                    incoming.updatedAt >=
                        existingTransaction.updatedAt
                ) {

                    remoteTransactions[index] =
                        incoming.copy(
                            syncStatus =
                                SyncStatus.SYNCED
                        )
                }

            } else {

                remoteTransactions.add(
                    incoming.copy(
                        syncStatus =
                            SyncStatus.SYNCED
                    )
                )
            }
        }

        val response =
            TransactionSyncUploadResponse(
                acceptedCount =
                    request.transactions.size,
                serverTime =
                    System.currentTimeMillis()
            )

        idempotencyRecords[idempotencyKey] =
            IdempotencyRecord(
                requestFingerprint =
                    fingerprint,
                response =
                    response
            )

        return Result.success(response)
    }

    override suspend fun download(
        request: TransactionSyncDownloadRequest
    ): Result<TransactionSyncDownloadResponse> {

        val cursor =
            request.cursor()

        val transactions =
            remoteTransactions
                .filter { transaction ->

                    transaction.updatedAt >
                        cursor.updatedAt ||

                        (
                            transaction.updatedAt ==
                                cursor.updatedAt &&
                                transaction.id >
                                    cursor.id
                        )
                }
                .sortedWith(
                    compareBy<Transaction> {
                        it.updatedAt
                    }.thenBy {
                        it.id
                    }
                )
                .take(
                    TransactionSyncProtocol
                        .MAX_DOWNLOAD_BATCH_SIZE
                )
                .map {
                    it.copy(
                        syncStatus =
                            SyncStatus.SYNCED
                    )
                }

        val nextCursor =
            transactions
                .maxWithOrNull(
                    compareBy<Transaction> {
                        it.updatedAt
                    }.thenBy {
                        it.id
                    }
                )
                ?.let {
                    TransactionSyncCursor(
                        updatedAt =
                            it.updatedAt,
                        id =
                            it.id
                    )
                }
                ?: cursor

        return Result.success(
            TransactionSyncDownloadResponse(
                transactions =
                    transactions,
                nextUpdatedAfter =
                    nextCursor.updatedAt,
                nextUpdatedAfterId =
                    nextCursor.id,
                serverTime =
                    System.currentTimeMillis()
            )
        )
    }

    fun remoteSnapshot():
        List<Transaction> {
        return remoteTransactions.toList()
    }

    fun idempotencyRecordCount(): Int =
        idempotencyRecords.size

    fun clear() {
        remoteTransactions.clear()
        idempotencyRecords.clear()
    }
}
