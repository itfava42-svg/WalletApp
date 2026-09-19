package com.example.walletapp.data

/**
 * Identifies where a transaction originally came from.
 */
enum class TransactionSource {
    MANUAL,
    SMS,
    TRANSFER,
    LEGACY,
}

/**
 * Local synchronization state.
 */
enum class SyncStatus {
    LOCAL_ONLY,
    PENDING,
    SYNCED,
    FAILED,
}