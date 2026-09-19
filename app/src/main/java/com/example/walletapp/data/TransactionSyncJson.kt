package com.example.walletapp.data

import org.json.JSONArray
import org.json.JSONObject

/**
 * JSON serialization boundary for the Sync protocol.
 *
 * This file contains transport serialization only.
 * It does not modify TransactionStore or financial logic.
 */
object TransactionSyncJson {

    fun transactionToJson(
        transaction: Transaction
    ): JSONObject {

        return JSONObject().apply {

            put("id", transaction.id)
            put("title", transaction.title)
            put("subtitle", transaction.subtitle)
            put("amount", transaction.amount)
            put("type", transaction.type.name)
            put("iconKey", transaction.iconKey)

            put(
                "tags",
                JSONArray(transaction.tags)
            )

            putNullable(
                "accountId",
                transaction.accountId
            )

            putNullable(
                "category",
                transaction.category
            )

            putNullable(
                "dateEpochMillis",
                transaction.dateEpochMillis
            )

            put("note", transaction.note)
            put("isRecurring", transaction.isRecurring)

            put("kind", transaction.kind.name)

            putNullable(
                "sourceAccountId",
                transaction.sourceAccountId
            )

            putNullable(
                "destinationAccountId",
                transaction.destinationAccountId
            )

            put("description", transaction.description)

            putNullable(
                "receiptUri",
                transaction.receiptUri
            )

            put(
                "requiresCompletion",
                transaction.requiresCompletion
            )

            put("createdAt", transaction.createdAt)
            put("updatedAt", transaction.updatedAt)

            put("source", transaction.source.name)
            put("syncStatus", transaction.syncStatus.name)

            putNullable(
                "sourceId",
                transaction.sourceId
            )
        }
    }

    fun transactionFromJson(
        json: JSONObject
    ): Transaction {

        val tagsJson =
            json.optJSONArray("tags")

        val tags =
            buildList {

                if (tagsJson != null) {

                    for (
                        index in
                            0 until tagsJson.length()
                    ) {

                        add(
                            tagsJson.optString(
                                index
                            )
                        )
                    }
                }
            }

        return Transaction(

            id =
                json.getString("id"),

            title =
                json.optString("title"),

            subtitle =
                json.optString("subtitle"),

            amount =
                json.optLong("amount"),

            type =
                enumValue(
                    json.optString("type"),
                    TransactionType.EXPENSE
                ),

            iconKey =
                json.optString("iconKey"),

            tags =
                tags,

            accountId =
                json.optString(
                    "accountId",
                    ""
                ).takeIf {
                    it.isNotBlank()
                },

            category =
                json.optString(
                    "category",
                    ""
                ).takeIf {
                    it.isNotBlank()
                },

            dateEpochMillis =
                if (
                    json.has("dateEpochMillis") &&
                    !json.isNull("dateEpochMillis")
                ) {
                    json.optLong(
                        "dateEpochMillis"
                    )
                } else {
                    null
                },

            note =
                json.optString("note"),

            isRecurring =
                json.optBoolean(
                    "isRecurring",
                    false
                ),

            kind =
                enumValue(
                    json.optString("kind"),
                    TransactionKind.NORMAL
                ),

            sourceAccountId =
                json.optString(
                    "sourceAccountId",
                    ""
                ).takeIf {
                    it.isNotBlank()
                },

            destinationAccountId =
                json.optString(
                    "destinationAccountId",
                    ""
                ).takeIf {
                    it.isNotBlank()
                },

            description =
                json.optString("description"),

            receiptUri =
                json.optString(
                    "receiptUri",
                    ""
                ).takeIf {
                    it.isNotBlank()
                },

            requiresCompletion =
                json.optBoolean(
                    "requiresCompletion",
                    false
                ),

            createdAt =
                json.optLong(
                    "createdAt",
                    0L
                ),

            updatedAt =
                json.optLong(
                    "updatedAt",
                    0L
                ),

            source =
                enumValue(
                    json.optString("source"),
                    TransactionSource.LEGACY
                ),

            syncStatus =
                enumValue(
                    json.optString("syncStatus"),
                    SyncStatus.LOCAL_ONLY
                ),

            sourceId =
                json.optString(
                    "sourceId",
                    ""
                ).takeIf {
                    it.isNotBlank()
                }
        )
    }

    fun uploadRequestToJson(
        request: TransactionSyncUploadRequest
    ): JSONObject {

        return JSONObject().apply {

            put(
                "protocolVersion",
                request.client.protocolVersion
            )

            put(
                "deviceId",
                request.client.deviceId
            )

            put(
                "generatedAt",
                request.generatedAt
            )

            put(
                "transactions",
                JSONArray().apply {

                    request.transactions.forEach {
                        put(
                            transactionToJson(it)
                        )
                    }
                }
            )
        }
    }

        fun uploadResponseFromJson(
        json: JSONObject
    ): TransactionSyncUploadResponse {

        require(
            json.has("acceptedCount")
        ) {
            "Upload response is missing acceptedCount."
        }

        require(
            json.has("serverTime")
        ) {
            "Upload response is missing serverTime."
        }

        val acceptedRaw =
            json.get("acceptedCount")

        require(
            acceptedRaw is Number
        ) {
            "Upload response acceptedCount must be numeric."
        }

        val serverTimeRaw =
            json.get("serverTime")

        require(
            serverTimeRaw is Number
        ) {
            "Upload response serverTime must be numeric."
        }

        val acceptedCount =
            (acceptedRaw as Number)
                .toLong()

        val serverTime =
            (serverTimeRaw as Number)
                .toLong()

        require(
            acceptedCount >= 0L
        ) {
            "Upload response acceptedCount is invalid."
        }

        require(
            serverTime >= 0L
        ) {
            "Upload response serverTime is invalid."
        }

        return TransactionSyncUploadResponse(
            acceptedCount =
                acceptedCount.toInt(),
            serverTime =
                serverTime
        )
    }

    fun downloadRequestToJson(
        request: TransactionSyncDownloadRequest
    ): JSONObject {

        return JSONObject().apply {

            put(
                "protocolVersion",
                request.client.protocolVersion
            )

            put(
                "deviceId",
                request.client.deviceId
            )

            put(
                "updatedAfter",
                request.updatedAfter
            )

            put(
                "updatedAfterId",
                request.updatedAfterId
            )
        }
    }

        fun downloadResponseFromJson(
        json: JSONObject
    ): TransactionSyncDownloadResponse {

        require(
            json.has("transactions")
        ) {
            "Download response is missing transactions."
        }

        require(
            json.has("nextUpdatedAfter")
        ) {
            "Download response is missing nextUpdatedAfter."
        }

        require(
            json.has("serverTime")
        ) {
            "Download response is missing serverTime."
        }

        val transactionsRaw =
            json.get("transactions")

        require(
            transactionsRaw is JSONArray
        ) {
            "Download response transactions must be an array."
        }

        val transactionsJson =
            transactionsRaw as JSONArray

        require(
            transactionsJson.length() <=
                TransactionSyncProtocol
                    .MAX_DOWNLOAD_BATCH_SIZE
        ) {
            "Download response exceeds maximum batch size."
        }

        val transactions =
            mutableListOf<Transaction>()

        for (
            index in
                0 until transactionsJson.length()
        ) {

            val item =
                transactionsJson.opt(
                    index
                )

            require(
                item is JSONObject
            ) {
                "Download response contains an invalid transaction at index $index."
            }

            val transaction =
                transactionFromJson(
                    item as JSONObject
                )

            require(
                transaction.id.isNotBlank()
            ) {
                "Download response contains a transaction with empty id."
            }

            require(
                transaction.updatedAt >= 0L
            ) {
                "Download response contains invalid updatedAt."
            }

            transactions.add(
                transaction
            )
        }

        val nextUpdatedAfterRaw =
            json.get("nextUpdatedAfter")

        require(
            nextUpdatedAfterRaw is Number
        ) {
            "Download response nextUpdatedAfter must be numeric."
        }

        val serverTimeRaw =
            json.get("serverTime")

        require(
            serverTimeRaw is Number
        ) {
            "Download response serverTime must be numeric."
        }

        val nextUpdatedAfter =
            (nextUpdatedAfterRaw as Number)
                .toLong()

        val serverTime =
            (serverTimeRaw as Number)
                .toLong()

        val nextUpdatedAfterId =
            json.optString(
                "nextUpdatedAfterId",
                ""
            )

        require(
            nextUpdatedAfter >= 0L
        ) {
            "Download response nextUpdatedAfter is invalid."
        }

        require(
            serverTime >= 0L
        ) {
            "Download response serverTime is invalid."
        }

        val cursor =
            TransactionSyncCursor(
                updatedAt =
                    nextUpdatedAfter,
                id =
                    nextUpdatedAfterId
            )

        require(
            cursor.isValid()
        ) {
            "Download response returned an invalid cursor."
        }

        return TransactionSyncDownloadResponse(
            transactions =
                transactions,
            nextUpdatedAfter =
                nextUpdatedAfter,
            nextUpdatedAfterId =
                nextUpdatedAfterId,
            serverTime =
                serverTime
        )
    }

    private fun JSONObject.putNullable(
        key: String,
        value: Any?
    ) {

        if (value == null) {
            put(
                key,
                JSONObject.NULL
            )
        } else {
            put(
                key,
                value
            )
        }
    }

    private inline fun <reified T : Enum<T>>
        enumValue(
            value: String,
            fallback: T
        ): T {

        return try {

            enumValues<T>().first {
                it.name == value
            }

        } catch (_: Throwable) {

            fallback
        }
    }
}

