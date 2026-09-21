package com.example.walletapp.data

import android.content.Context

/**
 * Domain boundary for account mutations.
 *
 * AccountStore remains the authoritative owner of account state,
 * persistence, and account invariants.
 *
 * This class only keeps UI code from directly coordinating mutations.
 */
object AccountManager {

    fun attachContext(context: Context) {
        AccountStore.attachContext(context)
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
        AccountStore.add(
            name = name,
            type = type,
            bankName = bankName,
            accountNumber = accountNumber,
            cardLast4 = cardLast4,
            openingBalance = openingBalance,
            isDefault = isDefault,
        )
    }

    fun update(account: Account) {
        AccountStore.update(account)
    }

    fun delete(id: String): Boolean {
        return AccountStore.delete(id)
    }

    fun setDefault(id: String) {
        AccountStore.setDefault(id)
    }

    fun setActive(
        id: String,
        active: Boolean,
    ): Boolean {
        return AccountStore.setActive(
            id = id,
            active = active,
        )
    }
}
