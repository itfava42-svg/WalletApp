package com.example.walletapp.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Persists transactions to SharedPreferences as a JSON array, so they
 * survive the app process being killed (by the user, by Android's
     * low-memory killer, etc.) — which happens routinely, especially for the
 * short-lived process Android spins up just to deliver an SMS broadcast.
 * Uses org.json (built into Android) rather than adding a new dependency.
 */
object TransactionPersistence {
    private const val PREFS = "wallet_app_prefs"
    private const val KEY_TRANSACTIONS = "transactions_json"

    fun save(context: Context, transactions: List<Transaction>) {
        val array = JSONArray()
        for (t in transactions) {
            val obj = JSONObject()
            obj.put("id", t.id)
            obj.put("title", t.title)
            obj.put("subtitle", t.subtitle)
            obj.put("amount", t.amount)
            obj.put("type", t.type.name)
            obj.put("iconKey", t.iconKey)
            obj.put("tags", JSONArray(t.tags))

            // Core financial metadata.
            // These fields were added after the original transaction model.
            // Keeping them in the same JSON object preserves backward compatibility.
            if (t.accountId != null) {
                obj.put("accountId", t.accountId)
            } else {
                obj.put("accountId", JSONObject.NULL)
            }

            if (t.category != null) {
                obj.put("category", t.category)
            } else {
                obj.put("category", JSONObject.NULL)
            }

            if (t.dateEpochMillis != null) {
                obj.put("dateEpochMillis", t.dateEpochMillis)
            } else {
                obj.put("dateEpochMillis", JSONObject.NULL)
            }

            obj.put("note", t.note)
            obj.put("isRecurring", t.isRecurring)

            // Transfer metadata.
            obj.put("kind", t.kind.name)

            if (t.sourceAccountId != null) {
                obj.put("sourceAccountId", t.sourceAccountId)
            } else {
                obj.put("sourceAccountId", JSONObject.NULL)
            }

            if (t.destinationAccountId != null) {
                obj.put("destinationAccountId", t.destinationAccountId)
            } else {
                obj.put("destinationAccountId", JSONObject.NULL)
            }

            obj.put("description", t.description)
            if (t.receiptUri != null) {
                obj.put("receiptUri", t.receiptUri)
            } else {
                obj.put("receiptUri", JSONObject.NULL)
            }
            obj.put("requiresCompletion", t.requiresCompletion)

            // Sync metadata.
            obj.put("createdAt", t.createdAt)
            obj.put("updatedAt", t.updatedAt)
            obj.put("source", t.source.name)
            obj.put("syncStatus", t.syncStatus.name)
        if (t.sourceId != null) {
            obj.put("sourceId", t.sourceId)
        } else {
            obj.put("sourceId", JSONObject.NULL)
        }
            array.put(obj)
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TRANSACTIONS, array.toString())
            .apply()
    }

    fun load(context: Context): List<Transaction> {
        val json =
            context
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_TRANSACTIONS, null)
                ?: return emptyList()

        return runCatching {

            val array = JSONArray(json)
            val result = mutableListOf<Transaction>()

            for (i in 0 until array.length()) {

                val transaction =
                    runCatching {

                        val obj = array.getJSONObject(i)

                        Transaction(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            subtitle = obj.getString("subtitle"),
                            amount = obj.getLong("amount"),
                            type =
                                TransactionType.valueOf(
                                    obj.getString("type")
                                ),
                            iconKey = obj.getString("iconKey"),

                            tags =
                                obj.optJSONArray("tags")
                                    ?.let { arr ->
                                        (0 until arr.length())
                                            .map { index ->
                                                arr.getString(index)
                                            }
                                    }
                                    ?: emptyList(),

                            accountId =
                                if (
                                    obj.has("accountId") &&
                                    !obj.isNull("accountId")
                                ) {
                                    obj.optString("accountId")
                                        .takeIf { it.isNotBlank() }
                                } else {
                                    null
                                },

                            category =
                                if (
                                    obj.has("category") &&
                                    !obj.isNull("category")
                                ) {
                                    obj.optString("category")
                                        .takeIf { it.isNotBlank() }
                                } else {
                                    null
                                },

                            dateEpochMillis =
                                if (
                                    obj.has("dateEpochMillis") &&
                                    !obj.isNull("dateEpochMillis")
                                ) {
                                    obj.optLong("dateEpochMillis")
                                } else {
                                    null
                                },

                            note = obj.optString("note", ""),
                            isRecurring =
                                obj.optBoolean(
                                    "isRecurring",
                                    false
                                ),

                            kind =
                                runCatching {
                                    TransactionKind.valueOf(
                                        obj.optString(
                                            "kind",
                                            TransactionKind.NORMAL.name
                                        )
                                    )
                                }.getOrDefault(
                                    TransactionKind.NORMAL
                                ),

                            sourceAccountId =
                                if (
                                    obj.has("sourceAccountId") &&
                                    !obj.isNull("sourceAccountId")
                                ) {
                                    obj.optString("sourceAccountId")
                                        .takeIf { it.isNotBlank() }
                                } else {
                                    null
                                },

                            destinationAccountId =
                                if (
                                    obj.has("destinationAccountId") &&
                                    !obj.isNull("destinationAccountId")
                                ) {
                                    obj.optString("destinationAccountId")
                                        .takeIf { it.isNotBlank() }
                                } else {
                                    null
                                },

                            description =
                                obj.optString(
                                    "description",
                                    ""
                                ),

                            receiptUri =
                                if (
                                    obj.has("receiptUri") &&
                                    !obj.isNull("receiptUri")
                                ) {
                                    obj.getString("receiptUri")
                                } else {
                                    null
                                },

                            requiresCompletion =
                                obj.optBoolean(
                                    "requiresCompletion",
                                    false
                                ),

                            // Sync metadata.
                            // Backward-compatible migration for old transactions.
                            createdAt =
                                obj.optLong(
                                    "createdAt",
                                    obj.optLong(
                                        "dateEpochMillis",
                                        System.currentTimeMillis()
                                    )
                                ),

                            updatedAt =
                                obj.optLong(
                                    "updatedAt",
                                    obj.optLong(
                                        "createdAt",
                                        obj.optLong(
                                            "dateEpochMillis",
                                            System.currentTimeMillis()
                                        )
                                    )
                                ),

                            source =
                                runCatching {
                                    TransactionSource.valueOf(
                                        obj.optString(
                                            "source",
                                            TransactionSource.LEGACY.name
                                        )
                                    )
                                }.getOrDefault(
                                    TransactionSource.LEGACY
                                ),

                            syncStatus =
                                runCatching {
                                    SyncStatus.valueOf(
                                        obj.optString(
                                            "syncStatus",
                                            SyncStatus.LOCAL_ONLY.name
                                        )
                                    )
                                }.getOrDefault(
                                    SyncStatus.LOCAL_ONLY
                                ),
            sourceId =
                obj.optString(
                    "sourceId",
                    ""
                ).takeIf {
                    it.isNotBlank()
                },
                        )
                    }.getOrNull()

                if (transaction != null) {
                    result.add(transaction)
                }
            }

            result

        }.getOrDefault(emptyList())
    }
}




