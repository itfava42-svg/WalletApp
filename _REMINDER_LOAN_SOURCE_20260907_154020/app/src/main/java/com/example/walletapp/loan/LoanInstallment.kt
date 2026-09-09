package com.example.walletapp.loan

import java.util.UUID

data class LoanInstallment(

    val id: String = UUID.randomUUID().toString(),

    val loanId: String,

    val number: Int,

    val dueDate: String,

    val amount: Long,

    val isPaid: Boolean = false,

    val paidDate: String? = null,

    val transactionId: String? = null
)
