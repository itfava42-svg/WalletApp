package com.example.walletapp.loan

import android.content.Context
import com.example.walletapp.data.LoanEntry
import com.example.walletapp.data.LoanPayment
import com.example.walletapp.data.LoanStore
import com.example.walletapp.data.Transaction
import com.example.walletapp.data.TransactionStore
import com.example.walletapp.data.TransactionType
import java.util.UUID

object LoanPaymentManager {

    /**
     * Links an existing SMS transaction to an installment in the loan store
     * used by the current loan-management screen. No transaction is created
     * or updated, so its original amount remains intact.
     */
    fun linkExistingTransactionToInstallment(
        context: Context,
        loan: LoanEntry,
        installmentNumber: Int,
        transactionId: String,
    ): LoanPayment? {
        TransactionStore.attachContext(context)
        LoanStore.attachContext(context)

        val transaction =
            TransactionStore.transactions.firstOrNull { it.id == transactionId }
                ?: return null

        if (
            transaction.type != TransactionType.EXPENSE ||
            installmentNumber !in 1..loan.totalInstallments ||
            loan.payments.any { it.installmentNumber == installmentNumber }
        ) {
            return null
        }

        val payment =
            LoanPayment(
                id = UUID.randomUUID().toString(),
                installmentNumber = installmentNumber,
                amount = transaction.amount,
                dateLabel = "امروز",
                transactionId = transaction.id,
            )

        LoanStore.addPayment(loan.id, payment)
        return payment
    }

    /**
     * Creates a new transaction for a manual installment payment.
     *
     * This method is kept for existing manual loan-payment behavior.
     */
    fun payInstallment(
        context: Context,
        loan: Loan,
        installment: LoanInstallment
    ): LoanInstallment {

        TransactionStore.attachContext(context)

        val transaction =
            Transaction(
                id = UUID.randomUUID().toString(),
                title = "پرداخت قسط ${loan.name}",
                subtitle = "قسط شماره ${installment.number}",
                amount = installment.amount,
                type = TransactionType.EXPENSE,
                iconKey = "وام",
                tags = listOf("وام"),
                description = "پرداخت قسط ${loan.name}"
            )

        TransactionStore.addManual(transaction)

        return LoanInstallmentStore.markPaid(
            installment = installment,
            transactionId = transaction.id,
            paidDate = "امروز",
        )
    }

    /**
     * Links an EXISTING transaction to an installment.
     *
     * This method is intended for transactions that already exist,
     * such as transactions imported from bank SMS.
     *
     * IMPORTANT:
     * No new transaction is created here.
     *
     * The amount of the existing transaction is preserved.
     * Therefore, even if the SMS amount differs from the
     * installment amount, the original transaction amount
     * remains unchanged.
     */
    fun linkExistingTransactionToInstallment(
        context: Context,
        loan: Loan,
        installment: LoanInstallment,
        transactionId: String,
    ): LoanInstallment? {

        TransactionStore.attachContext(context)

        val transaction =
            TransactionStore.transactions.firstOrNull {
                it.id == transactionId
            }
                ?: return null

        if (transaction.type != TransactionType.EXPENSE) {
            return null
        }

        LoanInstallmentStore.attachContext(context)

        return LoanInstallmentStore.markPaid(
            installment = installment,
            transactionId = transaction.id,
            paidDate = "امروز",
        )
    }
}
