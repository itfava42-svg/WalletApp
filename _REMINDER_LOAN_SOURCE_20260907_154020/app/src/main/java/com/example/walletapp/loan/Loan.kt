package com.example.walletapp.loan

import java.util.UUID

data class Loan(

    val id: String = UUID.randomUUID().toString(),

    val name: String,

    val bank: String = "",

    val amount: Long,

    val interestRate: Double,

    val installmentCount: Int,

    val installmentInterval: Int = 1,

    val monthlyPayment: Long,

    val receiveDate: String,

    val firstInstallmentDate: String,

    val guarantee: String = "",

    val description: String = "",

    val isActive: Boolean = true,

    val createdAt: Long = System.currentTimeMillis(),

    val updatedAt: Long = System.currentTimeMillis()
)
