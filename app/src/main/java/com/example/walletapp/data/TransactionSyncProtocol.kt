package com.example.walletapp.data

data class TransactionSyncClient(
    val deviceId: String,
    val protocolVersion: Int
) {
    init {
        require(deviceId.isNotBlank()) {
            "Device ID is required."
        }

        require(protocolVersion > 0) {
            "Protocol version must be positive."
        }
    }
}

data class TransactionSyncCursor(
    val updatedAt: Long = 0L,
    val id: String = ""
) {
    init {
        require(updatedAt >= 0L) {
            "Cursor timestamp must not be negative."
        }
    }

    fun isValid(): Boolean =
        updatedAt >= 0L

    companion object {
        val START =
            TransactionSyncCursor(
                updatedAt = 0L,
                id = ""
            )
    }
}

data class TransactionSyncUploadRequest(
    val client: TransactionSyncClient,
    val generatedAt: Long,
    val transactions: List<Transaction>
) {
    init {
        require(generatedAt >= 0L) {
            "Generated time must not be negative."
        }

        require(
            transactions.size <=
                TransactionSyncProtocol.MAX_UPLOAD_BATCH_SIZE
        ) {
            "Upload batch exceeds maximum size."
        }

        require(
            TransactionSyncValidation.validateBatch(
                transactions
            )
        ) {
            "Upload batch contains invalid transactions."
        }
    }
}

data class TransactionSyncDownloadRequest(
    val client: TransactionSyncClient,
    val updatedAfter: Long = 0L,
    val updatedAfterId: String = ""
) {
    init {
        require(updatedAfter >= 0L) {
            "Download cursor must not be negative."
        }
    }

    constructor(
        client: TransactionSyncClient,
        cursor: TransactionSyncCursor
    ) : this(
        client = client,
        updatedAfter = cursor.updatedAt,
        updatedAfterId = cursor.id
    )

    fun cursor(): TransactionSyncCursor =
        TransactionSyncCursor(
            updatedAt = updatedAfter,
            id = updatedAfterId
        )
}

data class TransactionSyncUploadResponse(
    val acceptedCount: Int,
    val serverTime: Long
) {
    init {
        require(acceptedCount >= 0) {
            "Accepted count must not be negative."
        }

        require(serverTime >= 0L) {
            "Server time must not be negative."
        }
    }
}

data class TransactionSyncDownloadResponse(
    val transactions: List<Transaction>,
    val nextUpdatedAfter: Long,
    val nextUpdatedAfterId: String = "",
    val serverTime: Long
) {
    init {
        require(nextUpdatedAfter >= 0L) {
            "Next cursor timestamp must not be negative."
        }

        require(serverTime >= 0L) {
            "Server time must not be negative."
        }

        require(
            transactions.size <=
                TransactionSyncProtocol.MAX_DOWNLOAD_BATCH_SIZE
        ) {
            "Download batch exceeds maximum size."
        }
    }

    fun nextCursor(): TransactionSyncCursor =
        TransactionSyncCursor(
            updatedAt = nextUpdatedAfter,
            id = nextUpdatedAfterId
        )
}

object TransactionSyncProtocol {

    const val VERSION = 2

    const val MAX_UPLOAD_BATCH_SIZE = 500

    const val MAX_DOWNLOAD_BATCH_SIZE = 500
    const val MAX_DOWNLOAD_PAGES_PER_SYNC = 100
}

