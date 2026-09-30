package com.example.walletapp.data

import android.content.Context

class TransactionSyncCoordinator(
    private val context: Context,
    private val backendProvider:
        () -> TransactionSyncBackend =
        {
            TransactionSyncBackendProvider.get()
        }
) {

    data class SyncResult(
        val uploaded: Int,
        val downloaded: Int,
        val failedUploads: Int,
        val nextCursor:
            TransactionSyncCursor
    )

    suspend fun sync(
        updatedAfter: Long = 0L,
        updatedAfterId: String = ""
    ): SyncResult {

        val batch =
            TransactionSyncEngine
                .createUploadBatch()

        var uploaded = 0
        var failedUploads = 0

        // ====================================================
        // UPLOAD
        // ====================================================

        val uploadChunks =
            batch.transactions
                .chunked(
                    TransactionSyncProtocol
                        .MAX_UPLOAD_BATCH_SIZE
                )

        for (
            chunk in uploadChunks
        ) {

            val uploadRequest =
                TransactionSyncUploadRequest(
                    client =
                        TransactionSyncClient(
                            deviceId =
                                TransactionSyncIdentityStore
                                    .deviceId(context),
                            protocolVersion =
                                TransactionSyncProtocol
                                    .VERSION
                        ),
                    generatedAt =
                        batch.generatedAt,
                    transactions =
                        chunk
                )

            val uploadResult =
                backendProvider()
                    .upload(
                        uploadRequest
                    )

            if (
                uploadResult.isSuccess
            ) {

                val response =
                    uploadResult.getOrThrow()

                if (
                    response.processedCount !=
                        chunk.size ||
                    response.rejectedCount > 0
                ) {

                    chunk.forEach {

                        if (
                            TransactionSyncEngine
                                .markUploadFailure(it)
                        ) {
                            failedUploads++
                        }
                    }

                } else {

                    chunk.forEach {

                        if (
                            TransactionSyncEngine
                                .markUploadSuccess(it)
                        ) {
                            uploaded++
                        }
                    }
                }

            } else {

                chunk.forEach {

                    if (
                        TransactionSyncEngine
                            .markUploadFailure(it)
                    ) {
                        failedUploads++
                    }
                }
            }
        }

        // ====================================================
        // DOWNLOAD
        // ====================================================

        var currentCursor =
            TransactionSyncCursor(
                updatedAt =
                    updatedAfter,
                id =
                    updatedAfterId
            )

        var downloaded = 0
        var downloadPages = 0

        while (true) {

            downloadPages++

            require(
                downloadPages <=
                    TransactionSyncProtocol
                        .MAX_DOWNLOAD_PAGES_PER_SYNC
            ) {
                "Download exceeded maximum page count."
            }

            val downloadRequest =
                TransactionSyncDownloadRequest(
                    client =
                        TransactionSyncClient(
                            deviceId =
                                TransactionSyncIdentityStore
                                    .deviceId(context),
                            protocolVersion =
                                TransactionSyncProtocol
                                    .VERSION
                        ),
                    updatedAfter =
                        currentCursor.updatedAt,
                    updatedAfterId =
                        currentCursor.id
                )

            val downloadResult =
                backendProvider()
                    .download(
                        downloadRequest
                    )

            if (
                downloadResult.isFailure
            ) {

                throw downloadResult
                    .exceptionOrNull()
                    ?: IllegalStateException(
                        "Transaction download failed."
                    )
            }

            val response =
                downloadResult.getOrThrow()

            val nextCursor =
                response.nextCursor()

            require(
                compareCursor(
                    nextCursor,
                    currentCursor
                ) >= 0
            ) {
                "Server returned a backward sync cursor."
            }

            val remoteTransactions =
                response.transactions

            if (
                remoteTransactions.isEmpty()
            ) {
                currentCursor =
                    nextCursor
                break
            }

            // ------------------------------------------------
            // APPLY REMOTE PAGE
            // ------------------------------------------------

            TransactionSyncEngine
                .applyRemoteBatch(
                    remoteTransactions
                )

            downloaded +=
                remoteTransactions.size

            // ------------------------------------------------
            // CURSOR MUST ADVANCE
            // ------------------------------------------------

            require(
                compareCursor(
                    nextCursor,
                    currentCursor
                ) > 0
            ) {
                "Server returned a non-advancing sync cursor."
            }

            currentCursor =
                nextCursor

            // ------------------------------------------------
            // SAFETY:
            // A valid backend page cannot exceed protocol max.
            // If it is smaller than max, the next request is
            // still made. Only an empty page ends the loop.
            // ------------------------------------------------
        }

        return SyncResult(
            uploaded =
                uploaded,
            downloaded =
                downloaded,
            failedUploads =
                failedUploads,
            nextCursor =
                currentCursor
        )
    }

    private fun compareCursor(
        a: TransactionSyncCursor,
        b: TransactionSyncCursor
    ): Int {

        val timeCompare =
            a.updatedAt.compareTo(
                b.updatedAt
            )

        if (
            timeCompare != 0
        ) {
            return timeCompare
        }

        return a.id.compareTo(
            b.id
        )
    }
}
