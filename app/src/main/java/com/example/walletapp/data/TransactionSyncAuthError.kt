package com.example.walletapp.data

/**
 * Authentication failures returned by the Sync server.
 */
enum class TransactionSyncAuthErrorCode {
    UNAUTHENTICATED,
    FORBIDDEN
}

class TransactionSyncAuthException(
    val code: TransactionSyncAuthErrorCode,
    val httpStatus: Int,
    message: String
) : Exception(message)
