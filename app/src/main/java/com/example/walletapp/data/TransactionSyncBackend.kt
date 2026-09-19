package com.example.walletapp.data

import kotlinx.coroutines.CancellationException

import java.io.IOException
import java.net.HttpURLConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

interface TransactionSyncBackend {

    suspend fun upload(
        request: TransactionSyncUploadRequest
    ): Result<TransactionSyncUploadResponse>

    suspend fun download(
        request: TransactionSyncDownloadRequest
    ): Result<TransactionSyncDownloadResponse>
}

/**
 * Safe offline backend.
 *
 * This remains the default backend until a real backend is
 * explicitly installed through TransactionSyncBackendProvider.
 */
object EmptyTransactionSyncBackend :
    TransactionSyncBackend {

    override suspend fun upload(
        request: TransactionSyncUploadRequest
    ): Result<TransactionSyncUploadResponse> {

        return Result.failure(
            UnsupportedOperationException(
                "No sync backend is configured."
            )
        )
    }

    override suspend fun download(
        request: TransactionSyncDownloadRequest
    ): Result<TransactionSyncDownloadResponse> {

        return Result.success(
            TransactionSyncDownloadResponse(
                transactions = emptyList(),
                nextUpdatedAfter =
                    request.updatedAfter,
                nextUpdatedAfterId =
                    request.updatedAfterId,
                serverTime =
                    System.currentTimeMillis()
            )
        )
    }
}

/**
 * HTTPS transport boundary.
 *
 * Production uses HttpURLConnection.
 * Tests may provide an isolated transport implementation.
 */
/**
 * HTTP status exception.
 *
 * Preserves HTTP status for authentication and
 * server-side error classification.
 */
class TransactionSyncHttpStatusException(
    val statusCode: Int,
    val responseBody: String
) : IOException(
    "Sync server returned HTTP " +
        statusCode
)
interface TransactionSyncHttpTransport {

    fun post(
        url: String,
        body: String,
        connectTimeoutMillis: Int,
        readTimeoutMillis: Int,
        headers: Map<String, String> = emptyMap()
    ): String
}

/**
 * Production HTTPS transport.
 */
object DefaultTransactionSyncHttpTransport :
    TransactionSyncHttpTransport {

    override fun post(
        url: String,
        body: String,
        connectTimeoutMillis: Int,
        readTimeoutMillis: Int,
        headers: Map<String, String>
    ): String {

        val connection =
            URL(url)
                .openConnection()
                as HttpURLConnection

        try {

            connection.requestMethod = "POST"

            connection.connectTimeout =
                connectTimeoutMillis

            connection.readTimeout =
                readTimeoutMillis

            connection.doOutput = true

            connection.setRequestProperty(
                "Content-Type",
                "application/json; charset=utf-8"
            )

            connection.setRequestProperty(
                "Accept",
                "application/json"
            )

            connection.setRequestProperty(
                "Cache-Control",
                "no-cache"
            )

            headers.forEach { (name, value) ->

                if (
                    name.isNotBlank() &&
                    value.isNotBlank()
                ) {
                    connection.setRequestProperty(
                        name,
                        value
                    )
                }
            }

            connection.outputStream.use { output ->

                output.write(
                    body.toByteArray(
                        Charsets.UTF_8
                    )
                )

                output.flush()
            }

            val status =
                connection.responseCode

            val responseStream =
                if (status in 200..299) {
                    connection.inputStream
                } else {
                    connection.errorStream
                }

            val responseBody =
                responseStream
                    ?.bufferedReader(
                        Charsets.UTF_8
                    )
                    ?.use {
                        it.readText()
                    }
                    ?: ""

            if (status !in 200..299) {

                throw TransactionSyncHttpStatusException(
                    statusCode = status,
                    responseBody =
                        responseBody.take(500)
                )
            }

            if (responseBody.isBlank()) {

                throw IOException(
                    "Sync server returned an empty response."
                )
            }

            return responseBody

        } finally {

            connection.disconnect()
        }
    }
}
/**
 * HTTPS REST implementation of the Sync backend.
 *
 * The backend is NOT installed automatically.
 */
class HttpTransactionSyncBackend(
    baseUrl: String,
    private val accessTokenProvider: () -> String? = {
        null
    },
    private val connectTimeoutMillis: Int = 15000,
    private val readTimeoutMillis: Int = 30000,
    private val transport: TransactionSyncHttpTransport =
        DefaultTransactionSyncHttpTransport,
) : TransactionSyncBackend {

    private val normalizedBaseUrl =
        baseUrl.trimEnd('/')

    init {

        require(
            normalizedBaseUrl.isNotBlank()
        ) {
            "Sync backend URL is required."
        }

        require(
            normalizedBaseUrl.startsWith(
                "https://"
            )
        ) {
            "Sync backend must use HTTPS."
        }

        require(
            connectTimeoutMillis > 0
        ) {
            "Connect timeout must be positive."
        }

        require(
            readTimeoutMillis > 0
        ) {
            "Read timeout must be positive."
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

        return try {

            val responseJson =
                transactionSyncRetry {

                    withContext(Dispatchers.IO) {

                        executePost(
                            path =
                                "/api/v1/sync/upload",
                            body =
                                TransactionSyncJson
                                    .uploadRequestToJson(
                                        request
                                    )
                                    .toString(),
                            extraHeaders =
                                mapOf(
                                    "Idempotency-Key" to
                                        idempotencyKey
                                )
                        )
                    }
                }

            Result.success(
                TransactionSyncJson
                    .uploadResponseFromJson(
                        responseJson
                    )
            )

        } catch (
            error: TransactionSyncHttpStatusException
        ) {

            Result.failure(
                mapAuthError(error)
                    ?: error
            )

        } catch (error: CancellationException) {

            throw error

        } catch (error: Throwable) {

            Result.failure(error)
        }
    }

override suspend fun download(
        request: TransactionSyncDownloadRequest
    ): Result<TransactionSyncDownloadResponse> {

        return try {

            val responseJson =
                transactionSyncRetry {

                    withContext(Dispatchers.IO) {
                        executePost(
                            path =
                                "/api/v1/sync/download",
                            body =
                                TransactionSyncJson
                                    .downloadRequestToJson(
                                        request
                                    )
                                    .toString()
                        )
                    }
                }

            Result.success(
                TransactionSyncJson
                    .downloadResponseFromJson(
                        responseJson
                    )
            )

        } catch (
            error: TransactionSyncHttpStatusException
        ) {

            Result.failure(
                mapAuthError(error)
                    ?: error
            )

        } catch (error: CancellationException) {

            throw error

        } catch (error: Throwable) {

            Result.failure(error)
        }
    }
    private fun mapAuthError(
        error: TransactionSyncHttpStatusException
    ): TransactionSyncAuthException? {

        return when (error.statusCode) {

            401 ->
                TransactionSyncAuthException(
                    code =
                        TransactionSyncAuthErrorCode
                            .UNAUTHENTICATED,
                    httpStatus = 401,
                    message =
                        "Sync authentication is invalid or expired."
                )

            403 ->
                TransactionSyncAuthException(
                    code =
                        TransactionSyncAuthErrorCode
                            .FORBIDDEN,
                    httpStatus = 403,
                    message =
                        "Sync access is forbidden."
                )

            else ->
                null
        }
    }

    private fun authHeaders(): Map<String, String> {

        val token =
            accessTokenProvider()
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: throw IllegalStateException(
                    "Sync authentication token is not available."
                )

        return TransactionSyncAuthHeaders.build(
            accessToken = token
        )
    }

    private fun executePost(
        path: String,
        body: String,
        extraHeaders: Map<String, String> = emptyMap()
    ): org.json.JSONObject {

        val url =
            normalizedBaseUrl +
                path

        require(
            url.startsWith("https://")
        ) {
            "Sync backend must use HTTPS."
        }

        val responseBody =
            transport.post(
                url = url,
                body = body,
                connectTimeoutMillis =
                    connectTimeoutMillis,
                readTimeoutMillis =
                    readTimeoutMillis,
                headers = authHeaders() + extraHeaders
            )

        return org.json.JSONObject(
            responseBody
        )
    }
}



