package com.example.walletapp.data

enum class AccountType {
    BANK,
    CASH,
    CARD,
    SAVINGS,
    WALLET
}

data class Account(
    val id: String,
    val name: String,
    val type: AccountType,
    val bankName: String = "",
    val accountNumber: String = "",
    val cardLast4: String = "",
    val balance: Long = 0L,
    val isDefault: Boolean = false,
    val isActive: Boolean = true,
)
