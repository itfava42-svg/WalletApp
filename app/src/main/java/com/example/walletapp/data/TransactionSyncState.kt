package com.example.walletapp.data

/**
 * Current application-level synchronization state.
 *
 * This state is independent from individual transaction SyncStatus.
 */
enum class TransactionSyncFailureKind {

    UPLOAD,
    DOWNLOAD,
    AUTHENTICATION,
    NETWORK,
    CONFLICT,
    VALIDATION,
    SERVER,
    UNKNOWN,
}
sealed class TransactionSyncState {

    data object Idle : TransactionSyncState()

    data class Syncing(
        val pendingCount: Int
    ) : TransactionSyncState()

    data class Success(
        val uploaded: Int,
        val downloaded: Int,
        val completedAt: Long
    ) : TransactionSyncState()

    data class Failed(
        val message: String,
        val failedUploads: Int,
        val failureKind: TransactionSyncFailureKind = TransactionSyncFailureKind.UNKNOWN,
        val occurredAt: Long
    ) : TransactionSyncState()
}

