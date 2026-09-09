package com.example.walletapp.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import org.json.JSONArray
import org.json.JSONObject

enum class CheckType(val label: String) {
    RECEIVED("\u0686\u06a9 \u062f\u0631\u06cc\u0627\u0641\u062a\u06cc"),
    ISSUED("\u0686\u06a9 \u067e\u0631\u062f\u0627\u062e\u062a\u06cc")
}

enum class CheckStatus(val label: String) {
    PENDING("\u062f\u0631 \u0627\u0646\u062a\u0638\u0627\u0631"),
    RECEIVED("\u0648\u0635\u0648\u0644 \u0634\u062f\u0647"),
    PAID("\u067e\u0631\u062f\u0627\u062e\u062a \u0634\u062f\u0647"),
    RETURNED("\u0628\u0631\u06af\u0634\u062a\u06cc"),
    CANCELLED("\u0644\u063a\u0648 \u0634\u062f\u0647")
}

data class CheckEntry(
    val id: String,
    val title: String,
    val type: CheckType,
    val checkNumber: String,
    val bank: String,
    val amount: Long,
    val personName: String,
    val issueYear: Int,
    val issueMonth: Int,
    val issueDay: Int,
    val dueYear: Int,
    val dueMonth: Int,
    val dueDay: Int,
    val status: CheckStatus,
    val description: String
)

object CheckStore {

    val checks = mutableStateListOf<CheckEntry>()

    private var context: Context? = null
    private var loaded = false

    private const val PREFS = "wallet_app_prefs"
    private const val KEY = "checks_json"

    fun attachContext(ctx: Context) {

        if (context == null) {
            context = ctx.applicationContext
        }

        if (!loaded) {
            loaded = true
            load()
        }
    }

    private fun load() {

        val ctx = context ?: return

        val json = ctx
            .getSharedPreferences(
                PREFS,
                Context.MODE_PRIVATE
            )
            .getString(KEY, null)
            ?: return

        runCatching {

            val array = JSONArray(json)

            checks.clear()

            for (i in 0 until array.length()) {

                val item = array.getJSONObject(i)

                checks.add(
                    CheckEntry(
                        id = item.getString("id"),
                        title = item.getString("title"),
                        type = CheckType.valueOf(
                            item.getString("type")
                        ),
                        checkNumber =
                            item.optString(
                                "checkNumber",
                                ""
                            ),
                        bank =
                            item.optString(
                                "bank",
                                ""
                            ),
                        amount =
                            item.getLong("amount"),
                        personName =
                            item.optString(
                                "personName",
                                ""
                            ),
                        issueYear =
                            item.getInt("issueYear"),
                        issueMonth =
                            item.getInt("issueMonth"),
                        issueDay =
                            item.getInt("issueDay"),
                        dueYear =
                            item.getInt("dueYear"),
                        dueMonth =
                            item.getInt("dueMonth"),
                        dueDay =
                            item.getInt("dueDay"),
                        status =
                            CheckStatus.valueOf(
                                item.getString("status")
                            ),
                        description =
                            item.optString(
                                "description",
                                ""
                            )
                    )
                )
            }
        }
    }

    private fun save() {

        val ctx = context ?: return

        val array = JSONArray()

        checks.forEach { check ->

            val item = JSONObject()

            item.put("id", check.id)
            item.put("title", check.title)
            item.put("type", check.type.name)
            item.put("checkNumber", check.checkNumber)
            item.put("bank", check.bank)
            item.put("amount", check.amount)
            item.put("personName", check.personName)
            item.put("issueYear", check.issueYear)
            item.put("issueMonth", check.issueMonth)
            item.put("issueDay", check.issueDay)
            item.put("dueYear", check.dueYear)
            item.put("dueMonth", check.dueMonth)
            item.put("dueDay", check.dueDay)
            item.put("status", check.status.name)
            item.put("description", check.description)

            array.put(item)
        }

        ctx.getSharedPreferences(
            PREFS,
            Context.MODE_PRIVATE
        )
            .edit()
            .putString(KEY, array.toString())
            .apply()
    }

    fun add(check: CheckEntry) {
        checks.add(0, check)
        save()
    }

    fun update(check: CheckEntry) {

        val index =
            checks.indexOfFirst {
                it.id == check.id
            }

        if (index >= 0) {
            checks[index] = check
            save()
        }
    }

    fun remove(id: String) {

        checks.removeAll {
            it.id == id
        }

        save()
    }

    fun getById(id: String): CheckEntry? {
        return checks.firstOrNull {
            it.id == id
        }
    }
}
