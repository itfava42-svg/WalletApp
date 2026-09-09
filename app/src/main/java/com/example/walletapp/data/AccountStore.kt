package com.example.walletapp.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

object AccountStore {

    val accounts = mutableStateListOf<Account>()

    private const val PREFS_NAME = "wallet_accounts"
    private const val KEY_ACCOUNTS = "accounts"

    private var appContext: Context? = null
    private var loadedFromDisk = false

    fun attachContext(context: Context) {

        if (appContext == null) {
            appContext = context.applicationContext
        }

        if (!loadedFromDisk) {
            loadedFromDisk = true
            load()
        }
    }

    private fun load() {

        val context = appContext ?: return

        val json = context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_ACCOUNTS, null)
            ?: return

        try {

            val array = JSONArray(json)

            accounts.clear()

            for (i in 0 until array.length()) {

                val item = array.getJSONObject(i)

                val typeName = item.optString(
                    "type",
                    AccountType.BANK.name
                )

                val type = try {
                    AccountType.valueOf(typeName)
                } catch (_: Exception) {
                    AccountType.BANK
                }

                accounts.add(
                    Account(
                        id = item.optString("id"),
                        name = item.optString("name"),
                        type = type,
                        bankName = item.optString("bankName"),
                        accountNumber = item.optString("accountNumber"),
                        cardLast4 = item.optString("cardLast4"),
                        openingBalance =
    if (item.has("openingBalance")) {
        item.optLong("openingBalance", 0L)
    } else {
        // Backward-compatible migration from the old balance field.
        item.optLong("balance", 0L)
    },
                        isDefault = item.optBoolean("isDefault", false),
                        isActive = item.optBoolean("isActive", true),
                    )
                )
            }

        } catch (_: Exception) {
            accounts.clear()
        }
    }

    private fun persist() {

        val context = appContext ?: return

        val array = JSONArray()

        accounts.forEach { account ->

            val item = JSONObject()

            item.put("id", account.id)
            item.put("name", account.name)
            item.put("type", account.type.name)
            item.put("bankName", account.bankName)
            item.put("accountNumber", account.accountNumber)
            item.put("cardLast4", account.cardLast4)
            item.put("openingBalance", account.openingBalance)
            item.put("isDefault", account.isDefault)
            item.put("isActive", account.isActive)

            array.put(item)
        }

        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ACCOUNTS, array.toString())
            .apply()
    }

    fun add(
        name: String,
        type: AccountType,
        bankName: String = "",
        accountNumber: String = "",
        cardLast4: String = "",
        openingBalance: Long = 0L,
        isDefault: Boolean = false,
    ) {

        val shouldBeDefault = isDefault || accounts.none { it.isDefault }

        if (shouldBeDefault) {
            for (i in accounts.indices) {
                accounts[i] = accounts[i].copy(isDefault = false)
            }
        }

        accounts.add(
            Account(
                id = UUID.randomUUID().toString(),
                name = name,
                type = type,
                bankName = bankName,
                accountNumber = accountNumber,
                cardLast4 = cardLast4,
                openingBalance = openingBalance,
                isDefault = shouldBeDefault,
            )
        )

        persist()
    }

    fun update(account: Account) {

        val index = accounts.indexOfFirst {
            it.id == account.id
        }

        if (index < 0) return

        if (account.isDefault) {

            for (i in accounts.indices) {
                if (accounts[i].id != account.id) {
                    accounts[i] = accounts[i].copy(isDefault = false)
                }
            }
        }

        accounts[index] = account

        persist()
    }

    fun delete(id: String) {

        val wasDefault = accounts
            .firstOrNull { it.id == id }
            ?.isDefault == true

        accounts.removeAll {
            it.id == id
        }

        if (wasDefault && accounts.isNotEmpty()) {
            accounts[0] = accounts[0].copy(isDefault = true)
        }

        persist()
    }

    fun setDefault(id: String) {

        for (i in accounts.indices) {
            accounts[i] = accounts[i].copy(
                isDefault = accounts[i].id == id
            )
        }

        persist()
    }
}

