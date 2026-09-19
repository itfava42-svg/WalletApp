package com.example.walletapp.data

/**
 * Server-side API contract for WalletApp transaction synchronization.
 *
 * This file defines the protocol boundary only.
 * It does not perform network operations.
 *
 * Production backend remains disabled.
 */
object TransactionSyncApiContract {

    const val API_VERSION = 1

    const val UPLOAD_PATH =
        "/api/v1/sync/upload"

    const val DOWNLOAD_PATH =
        "/api/v1/sync/download"

    const val AUTH_HEADER =
        "Authorization"

    const val CONTENT_TYPE =
        "application/json"

    const val BEARER_PREFIX =
        "Bearer "

    const val IDEMPOTENCY_HEADER =
        "Idempotency-Key"

    const val MAX_UPLOAD_BATCH =
        TransactionSyncProtocol.MAX_UPLOAD_BATCH_SIZE

    const val MAX_DOWNLOAD_BATCH =
        TransactionSyncProtocol.MAX_DOWNLOAD_BATCH_SIZE
}

/**
 * Authentication information sent to the Sync backend.
 */
data class TransactionSyncAuth(
    val accessToken: String
) {

    init {
        require(accessToken.isNotBlank()) {
            "Sync access token is required."
        }
    }

    fun authorizationHeader(): String =
        TransactionSyncApiContract.BEARER_PREFIX +
            accessToken
}

/**
 * Identifies the client making the Sync request.
 */
data class TransactionSyncDevice(
    val deviceId: String,
    val protocolVersion: Int =
        TransactionSyncProtocol.VERSION
) {

    init {
        require(deviceId.isNotBlank()) {
            "Sync device ID is required."
        }

        require(protocolVersion > 0) {
            "Sync protocol version must be positive."
        }
    }
}

/**
 * Upload idempotency information.
 *
 * The server must not create duplicate transactions when
 * the same transaction is uploaded more than once.
 */
data class TransactionSyncIdempotency(
    val transactionId: String,
    val sourceId: String? = null
) {

    init {
        require(transactionId.isNotBlank()) {
            "Transaction ID is required."
        }

        if (sourceId != null) {
            require(sourceId.isNotBlank()) {
                "Source ID must not be blank."
            }
        }
    }
}

/**
 * Complete server-side upload envelope.
 */
data class TransactionSyncUploadEnvelope(
    val device: TransactionSyncDevice,
    val auth: TransactionSyncAuth,
    val generatedAt: Long,
    val transactions: List<Transaction>
) {

    init {
        require(generatedAt >= 0L) {
            "Generated time must not be negative."
        }

        require(
            transactions.size <=
                TransactionSyncApiContract
                    .MAX_UPLOAD_BATCH
        ) {
            "Upload batch exceeds server contract limit."
        }
    }
}

/**
 * Complete server-side download envelope.
 */
data class TransactionSyncDownloadEnvelope(
    val device: TransactionSyncDevice,
    val auth: TransactionSyncAuth,
    val cursor: TransactionSyncCursor
) {

    init {
        require(cursor.updatedAt >= 0L) {
            "Download cursor must not be negative."
        }
    }
}

/**
 * Server result for one transaction.
 */
enum class TransactionSyncServerResult {
    ACCEPTED,
    ALREADY_EXISTS,
    UPDATED,
    REJECTED,
}

/**
 * Standard server error categories.
 */
enum class TransactionSyncErrorCode {

    UNAUTHORIZED,

    FORBIDDEN,

    INVALID_REQUEST,

    INVALID_TRANSACTION,

    DUPLICATE_TRANSACTION,

    CURSOR_INVALID,

    BATCH_TOO_LARGE,

    RATE_LIMITED,

    SERVER_ERROR,

    TEMPORARY_UNAVAILABLE,
}

/**
 * Standard API error envelope.
 */
data class TransactionSyncApiError(
    val code: TransactionSyncErrorCode,
    val message: String,
    val retryable: Boolean = false
) {

    init {
        require(message.isNotBlank()) {
            "Sync API error message is required."
        }
    }
}

/**
 * Server-side response for upload.
 */
data class TransactionSyncServerUploadResult(
    val acceptedCount: Int,
    val alreadyExistsCount: Int,
    val updatedCount: Int,
    val rejectedCount: Int,
    val serverTime: Long
) {

    init {

        require(acceptedCount >= 0) {
            "Accepted count must not be negative."
        }

        require(alreadyExistsCount >= 0) {
            "Already-exists count must not be negative."
        }

        require(updatedCount >= 0) {
            "Updated count must not be negative."
        }

        require(rejectedCount >= 0) {
            "Rejected count must not be negative."
        }

        require(serverTime >= 0L) {
            "Server time must not be negative."
        }
    }

    val processedCount: Int
        get() =
            acceptedCount +
                alreadyExistsCount +
                updatedCount +
                rejectedCount
}

/**
 * Server-side response for download.
 */
data class TransactionSyncServerDownloadResult(
    val transactions: List<Transaction>,
    val nextCursor: TransactionSyncCursor,
    val serverTime: Long
) {

    init {

        require(
            transactions.size <=
                TransactionSyncApiContract
                    .MAX_DOWNLOAD_BATCH
        ) {
            "Download batch exceeds server contract limit."
        }

        require(serverTime >= 0L) {
            "Server time must not be negative."
        }

        require(
            nextCursor.updatedAt >= 0L
        ) {
            "Next cursor must not be negative."
        }
    }
}

/**
 * Rules that the real backend must implement.
 */
object TransactionSyncServerRules {

    /**
     * A transaction is idempotent when its transaction ID matches,
     * or when its deterministic source ID matches.
     */
    fun hasSameIdentity(
        existing: Transaction,
        incoming: Transaction
    ): Boolean {

        if (existing.id == incoming.id) {
            return true
        }

        val existingSourceId =
            existing.sourceId
                ?.takeIf { it.isNotBlank() }

        val incomingSourceId =
            incoming.sourceId
                ?.takeIf { it.isNotBlank() }

        return existingSourceId != null &&
            incomingSourceId != null &&
            existingSourceId == incomingSourceId
    }

    /**
     * Server must never move a transaction backwards in time.
     */
    fun shouldAcceptUpdate(
        existing: Transaction,
        incoming: Transaction
    ): Boolean {

        if (!hasSameIdentity(existing, incoming)) {
            return true
        }

        return incoming.updatedAt >=
            existing.updatedAt
    }

    /**
     * Determines whether a cursor is valid.
     */
    fun isValidCursor(
        cursor: TransactionSyncCursor
    ): Boolean =
        cursor.updatedAt >= 0L
}
