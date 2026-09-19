package com.example.walletapp.data

import android.content.Context

class TransactionSyncService(
    private val context: Context,
    private val coordinator:
        TransactionSyncCoordinator =
        TransactionSyncCoordinator(
            context =
                context.applicationContext
        )
) {

    data class ServiceResult(
        val uploaded: Int,
        val downloaded: Int,
        val failedUploads: Int,
        val nextCursor:
            TransactionSyncCursor
    ) {

        val isSuccessful: Boolean
            get() =
                failedUploads == 0
    }

    @Volatile
    private var currentState:
        TransactionSyncState =
        TransactionSyncState.Idle

    fun state():
        TransactionSyncState =
        currentState

    suspend fun sync(
        updatedAfter: Long = 0L
    ): ServiceResult {

        val pending =
            pendingCount()

        currentState =
            TransactionSyncState.Syncing(
                pendingCount = pending
            )

        return try {

            val storedCursor =
                TransactionSyncIdentityStore
                    .cursor(context)

            val requestedCursor =
                maxCursor(
                    TransactionSyncCursor(
                        updatedAt =
                            updatedAfter,
                        id = ""
                    ),
                    storedCursor
                )

            val result =
                coordinator.sync(
                    updatedAfter =
                        requestedCursor.updatedAt,
                    updatedAfterId =
                        requestedCursor.id
                )

            val serviceResult =
                ServiceResult(
                    uploaded =
                        result.uploaded,
                    downloaded =
                        result.downloaded,
                    failedUploads =
                        result.failedUploads,
                    nextCursor =
                        result.nextCursor
                )

            if (
                serviceResult.isSuccessful
            ) {

                TransactionSyncIdentityStore
                    .saveCursor(
                        context = context,
                        value =
                            serviceResult.nextCursor
                    )

                currentState =
                    TransactionSyncState.Success(
                        uploaded =
                            serviceResult.uploaded,
                        downloaded =
                            serviceResult.downloaded,
                        completedAt =
                            System.currentTimeMillis()
                    )

            } else {

                currentState =
                    TransactionSyncState.Failed(
                        message =
                            "Some transactions failed to synchronize.",
                        failedUploads =
                            serviceResult.failedUploads,
                        failureKind = TransactionSyncFailureKind.UPLOAD,
                        occurredAt =
                            System.currentTimeMillis()
                    )
            }

            serviceResult

        } catch (


            error: kotlinx.coroutines.CancellationException


        ) {


            throw error


        } catch (


            error: Throwable


        ) {

            currentState =
                TransactionSyncState.Failed(
                    message = userMessage(error),
                    failedUploads =
                        0,
                    failureKind = failureKind(error),
                    occurredAt =
                        System.currentTimeMillis()
                )

            throw error
        }
    }
    /**

         * Converts technical Sync failures into safe user-facing messages.

         */

        private fun userMessage(
        error: Throwable
    ): String {

        return when (error) {

            is TransactionSyncAuthException -> {

                when (error.code) {

                    TransactionSyncAuthErrorCode.UNAUTHENTICATED ->
                        "احراز هویت همگام‌سازی نامعتبر یا منقضی شده است."

                    TransactionSyncAuthErrorCode.FORBIDDEN ->
                        "دسترسی به همگام‌سازی مجاز نیست."
                }
            }

            is TransactionSyncHttpStatusException -> {

                when {

                    error.statusCode == 408 ->
                        "درخواست همگام‌سازی زمان‌بر شد."

                    error.statusCode == 409 ->
                        "درخواست همگام‌سازی با داده موجود در سرور تعارض دارد."

                    error.statusCode == 429 ->
                        "تعداد درخواست‌های همگام‌سازی بیش از حد مجاز است. کمی بعد دوباره تلاش کنید."

                    error.statusCode in 400..499 ->
                        "درخواست همگام‌سازی توسط سرور پذیرفته نشد."

                    error.statusCode >= 500 ->
                        "سرور همگام‌سازی موقتاً با مشکل مواجه است."

                    else ->
                        "خطای ارتباط با سرور همگام‌سازی."
                }
            }

            is java.net.SocketTimeoutException ->
                "ارتباط با سرور همگام‌سازی زمان‌بر شد."

            is java.io.IOException ->
                "ارتباط با سرور همگام‌سازی برقرار نشد."

            is IllegalArgumentException ->
                "اطلاعات همگام‌سازی معتبر نیست."

            else ->
                "همگام‌سازی با خطای غیرمنتظره مواجه شد."
        }
    }

    private fun failureKind(

        error: Throwable

    ): TransactionSyncFailureKind {


        return when (error) {


            is TransactionSyncAuthException ->

                TransactionSyncFailureKind.AUTHENTICATION


            is TransactionSyncHttpStatusException -> {


                when {


                    error.statusCode == 409 ->

                        TransactionSyncFailureKind.CONFLICT


                    error.statusCode == 429 ->

                        TransactionSyncFailureKind.SERVER


                    error.statusCode in 400..499 ->

                        TransactionSyncFailureKind.VALIDATION


                    error.statusCode >= 500 ->

                        TransactionSyncFailureKind.SERVER


                    else ->

                        TransactionSyncFailureKind.UNKNOWN

                }

            }


            is java.net.SocketTimeoutException ->

                TransactionSyncFailureKind.NETWORK


            is java.io.IOException ->

                TransactionSyncFailureKind.NETWORK


            is IllegalArgumentException ->

                TransactionSyncFailureKind.VALIDATION


            else ->

                TransactionSyncFailureKind.UNKNOWN

        }

    }

    fun pendingCount(): Int =
        TransactionSyncRepository
            .pendingTransactions()
            .size

    fun hasPendingChanges(): Boolean =
        pendingCount() > 0

    private fun maxCursor(
        a: TransactionSyncCursor,
        b: TransactionSyncCursor
    ): TransactionSyncCursor {

        return if (
            compareCursor(a, b) >= 0
        ) {
            a
        } else {
            b
        }
    }

    private fun compareCursor(
        a: TransactionSyncCursor,
        b: TransactionSyncCursor
    ): Int {

        val timeCompare =
            a.updatedAt.compareTo(
                b.updatedAt
            )

        if (timeCompare != 0) {
            return timeCompare
        }

        return a.id.compareTo(
            b.id
        )
    }
}





