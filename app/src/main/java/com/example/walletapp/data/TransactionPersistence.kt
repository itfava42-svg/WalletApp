package com.example.walletapp.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Persists transactions to SharedPreferences as a JSON array, so they
 * survive the app process being killed (by the user, by Android's
 * low-memory killer, etc.) â€” which happens routinely, especially for the
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
            obj.put("description", t.description)
            if (t.receiptUri != null) {
                obj.put("receiptUri", t.receiptUri)
            } else {
                obj.put("receiptUri", JSONObject.NULL)
            }
            obj.put("requiresCompletion", t.requiresCompletion)
            array.put(obj)
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TRANSACTIONS, array.toString())
            .apply()
    }

    fun load(context: Context): List<Transaction> {
        val json = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_TRANSACTIONS, null) ?: return emptyList()

        return runCatching {
            val array = JSONArray(json)
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                Transaction(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    subtitle = obj.getString("subtitle"),
                    amount = obj.getLong("amount"),
                    type = TransactionType.valueOf(obj.getString("type")),
                    iconKey = obj.getString("iconKey"),
                    tags = obj.optJSONArray("tags")?.let { arr ->
                        (0 until arr.length()).map { arr.getString(it) }
                    } ?: emptyList(),
                    description = obj.optString("description", ""),
                    receiptUri =
                        if (obj.has("receiptUri") && !obj.isNull("receiptUri")) {
                            obj.getString("receiptUri")
                        } else {
                            null
                        },
                    requiresCompletion = obj.optBoolean("requiresCompletion", false),
                )
            }
        }.getOrDefault(emptyList())
    }
}