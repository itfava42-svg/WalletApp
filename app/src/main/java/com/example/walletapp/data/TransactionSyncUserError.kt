package com.example.walletapp.data

import kotlinx.coroutines.CancellationException
import java.io.IOException

/**
 * User-facing categories for transaction synchronization failures.
 *
 * This layer deliberately separates technical transport/backend
 * failures from messages that can safely be shown by the UI.
 */
enum class TransactionSyncUserErrorCode {

    AUTHENTICATION,
    FORBIDDEN,
    NETWORK,
    TIMEOUT,
    SERVER,
    RATE_LIMITED,
    INVALID_REQUEST,
    INVALID_TRANSACTION,
    DUPLICATE_TRANSACTION,
    CURSOR_INVALID,
    BATCH_TOO_LARGE,
    CONFLICT,
    INVALID_RESPONSE,
    CANCELLED,
    UNKNOWN,
}

/**
 * Safe error representation for Sync UI.
 *
 * technicalMessage is retained for diagnostics only and must not
 * be displayed directly as the user-facing message.
 */
data class TransactionSyncUserError(
    val code: TransactionSyncUserErrorCode,
    val message: String,
    val retryable: Boolean,
    val technicalMessage: String? = null,
)

/**
 * Converts technical Sync failures into a stable UI-level error.
 */
fun Throwable.toTransactionSyncUserError():
    TransactionSyncUserError {

    if (this is CancellationException) {

        return TransactionSyncUserError(
            code =
                TransactionSyncUserErrorCode
                    .CANCELLED,
            message =
                "همگام‌سازی لغو شد.",
            retryable = false,
            technicalMessage = message,
        )
    }

    if (this is TransactionSyncAuthException) {

        return when (code) {

            TransactionSyncAuthErrorCode
                .UNAUTHENTICATED ->

                TransactionSyncUserError(
                    code =
                        TransactionSyncUserErrorCode
                            .AUTHENTICATION,
                    message =
                        "احراز هویت همگام‌سازی معتبر نیست یا منقضی شده است.",
                    retryable = false,
                    technicalMessage = message,
                )

            TransactionSyncAuthErrorCode
                .FORBIDDEN ->

                TransactionSyncUserError(
                    code =
                        TransactionSyncUserErrorCode
                            .FORBIDDEN,
                    message =
                        "دسترسی به سرویس همگام‌سازی مجاز نیست.",
                    retryable = false,
                    technicalMessage = message,
                )
        }
    }

    if (this is TransactionSyncHttpStatusException) {

        return when {

            statusCode == 408 ||
                statusCode == 504 ->

                TransactionSyncUserError(
                    code =
                        TransactionSyncUserErrorCode
                            .TIMEOUT,
                    message =
                        "ارتباط با سرور همگام‌سازی به پایان رسید. دوباره تلاش کنید.",
                    retryable = true,
                    technicalMessage = message,
                )

            statusCode == 409 ->

                TransactionSyncUserError(
                    code =
                        TransactionSyncUserErrorCode
                            .CONFLICT,
                    message =
                        "در همگام‌سازی یک تعارض ایجاد شده است.",
                    retryable = false,
                    technicalMessage = message,
                )

            statusCode == 429 ->

                TransactionSyncUserError(
                    code =
                        TransactionSyncUserErrorCode
                            .RATE_LIMITED,
                    message =
                        "تعداد درخواست‌ها زیاد است. کمی بعد دوباره تلاش کنید.",
                    retryable = true,
                    technicalMessage = message,
                )

            statusCode in 400..499 ->

                TransactionSyncUserError(
                    code =
                        TransactionSyncUserErrorCode
                            .INVALID_REQUEST,
                    message =
                        "درخواست همگام‌سازی توسط سرور پذیرفته نشد.",
                    retryable = false,
                    technicalMessage = message,
                )

            statusCode >= 500 ->

                TransactionSyncUserError(
                    code =
                        TransactionSyncUserErrorCode
                            .SERVER,
                    message =
                        "سرور همگام‌سازی موقتاً با مشکل مواجه است. دوباره تلاش کنید.",
                    retryable = true,
                    technicalMessage = message,
                )

            else ->

                TransactionSyncUserError(
                    code =
                        TransactionSyncUserErrorCode
                            .UNKNOWN,
                    message =
                        "خطایی در همگام‌سازی رخ داد.",
                    retryable = false,
                    technicalMessage = message,
                )
        }
    }

    if (this is IOException) {

        return TransactionSyncUserError(
            code =
                TransactionSyncUserErrorCode
                    .NETWORK,
            message =
                "ارتباط با اینترنت یا سرور همگام‌سازی برقرار نشد.",
            retryable = true,
            technicalMessage = message,
        )
    }

    val text =
        message
            ?.lowercase()
            ?: ""

    if (
        text.contains("invalid transaction") ||
        text.contains("transaction id")
    ) {

        return TransactionSyncUserError(
            code =
                TransactionSyncUserErrorCode
                    .INVALID_TRANSACTION,
            message =
                "یکی از تراکنش‌ها برای همگام‌سازی معتبر نیست.",
            retryable = false,
            technicalMessage = message,
        )
    }

    if (
        text.contains("duplicate transaction") ||
        text.contains("duplicate")
    ) {

        return TransactionSyncUserError(
            code =
                TransactionSyncUserErrorCode
                    .DUPLICATE_TRANSACTION,
            message =
                "این تراکنش قبلاً در همگام‌سازی ثبت شده است.",
            retryable = false,
            technicalMessage = message,
        )
    }

    if (
        text.contains("cursor") &&
        text.contains("invalid")
    ) {

        return TransactionSyncUserError(
            code =
                TransactionSyncUserErrorCode
                    .CURSOR_INVALID,
            message =
                "نشانگر همگام‌سازی معتبر نیست.",
            retryable = false,
            technicalMessage = message,
        )
    }

    if (
        text.contains("batch") &&
        (
            text.contains("large") ||
                text.contains("maximum") ||
                text.contains("limit")
        )
    ) {

        return TransactionSyncUserError(
            code =
                TransactionSyncUserErrorCode
                    .BATCH_TOO_LARGE,
            message =
                "تعداد تراکنش‌های این مرحله بیش از حد مجاز است.",
            retryable = false,
            technicalMessage = message,
        )
    }

    return TransactionSyncUserError(
        code =
            TransactionSyncUserErrorCode
                .UNKNOWN,
        message =
            "خطایی در همگام‌سازی رخ داد. دوباره تلاش کنید.",
        retryable = false,
        technicalMessage = message,
    )
}
