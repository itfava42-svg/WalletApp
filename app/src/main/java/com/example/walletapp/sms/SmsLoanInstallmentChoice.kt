package com.example.walletapp.sms

data class SmsLoanInstallmentChoice(
    val number: Int,
    val dueDate: String,
    val amount: Long,
)