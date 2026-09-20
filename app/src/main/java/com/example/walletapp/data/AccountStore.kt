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
                        accountNumber = normalizeDigits(
                            item.optString("accountNumber")
                        ),
                        cardLast4 = normalizeDigits(
                            item.optString("cardLast4")
                        ),
                        openingBalance =
    if (item.has("openingBalance")) {
        item.optLong("openingBalance", 0L)
    } else {
        // Backward-compatible migration from the old balance field.
        item.optLong("balance", 0L)
    },
                        isDefault = item.optBoolean("isDefault", false) && item.optBoolean("isActive", true),
                        isActive = item.optBoolean("isActive", true),
                    )
                )
            }

        } catch (_: Exception) {
            accounts.clear()
        }
    }

    /**
     * Converts Persian and Arabic-Indic digits to ASCII digits.
     *
     * Account/card identifiers are stored canonically so SMS matching
     * remains independent of the digit system used by the user.
     */
    private fun normalizeDigits(raw: String): String {
        return buildString {
            raw.forEach { char ->
                when (char) {
                    in '\u06F0'..'\u06F9' ->
                        append(
                            ('0'.code +
                                (char.code - '\u06F0'.code))
                                .toChar()
                        )

                    in '\u0660'..'\u0669' ->
                        append(
                            ('0'.code +
                                (char.code - '\u0660'.code))
                                .toChar()
                        )

                    else -> append(char)
                }
            }
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
                accountNumber = normalizeDigits(accountNumber.trim()),
                cardLast4 = normalizeDigits(cardLast4.trim()),
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

        accounts[index] =
            account.copy(
                accountNumber = normalizeDigits(account.accountNumber.trim()),
                cardLast4 = normalizeDigits(account.cardLast4.trim()),
            )

        persist()
    }

    /**
     * Deletes an account only when it has no transaction history.
     *
     * Historical transactions must never become orphaned silently.
     *
     * Returns true when the account was physically deleted.
     * Returns false when the account does not exist or has linked transactions.
     */
    fun delete(id: String): Boolean {

        val account = accounts
            .firstOrNull { it.id == id }
            ?: return false

        val hasLinkedTransactions =
            TransactionStore.transactions.any { transaction ->

                transaction.accountId == id ||
                    transaction.sourceAccountId == id ||
                    transaction.destinationAccountId == id
            }

        if (hasLinkedTransactions) {
            return false
        }

        val wasDefault = account.isDefault

        accounts.removeAll {
            it.id == id
        }

        if (wasDefault && accounts.isNotEmpty()) {
            accounts[0] = accounts[0].copy(
                isDefault = true
            )
        }

        persist()

        return true
    }

    fun setDefault(id: String) {

        // Only an existing active account can become default.
        val target = accounts.firstOrNull {
            it.id == id && it.isActive
        } ?: return

        for (i in accounts.indices) {
            accounts[i] = accounts[i].copy(
                isDefault = accounts[i].id == target.id
            )
        }

        persist()
    }

    /**
     * Enables or disables an account without deleting it.
     *
     * Historical transactions remain untouched.
     *
     * A disabled account can never remain the default account.
     * When disabling the current default, the first active account
     * becomes the new default when one exists.
     */
    fun setActive(
        id: String,
        active: Boolean
    ): Boolean {

        val index = accounts.indexOfFirst {
            it.id == id
        }

        if (index < 0) {
            return false
        }

        val current = accounts[index]

        if (current.isActive == active) {
            return true
        }

        if (!active && current.isDefault) {

            accounts[index] = current.copy(
                isActive = false,
                isDefault = false
            )

            val replacementIndex = accounts.indexOfFirst {
                it.id != id && it.isActive
            }

            if (replacementIndex >= 0) {
                accounts[replacementIndex] =
                    accounts[replacementIndex].copy(
                        isDefault = true
                    )
            }

        } else {

            accounts[index] = current.copy(
                isActive = active
            )
        }

        persist()

        return true
    }
}






