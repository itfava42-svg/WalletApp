package com.example.walletapp.data

import android.content.Context
import java.util.UUID

object LoanPaymentManager {

    /**
     * Links an existing transaction to an installment.
     *
     * Used for transactions that already exist, such as transactions
     * created from bank SMS. The original transaction is not changed.
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
                dateLabel =
                    transaction.dateEpochMillis?.let {
                        JalaliCalendar.fromEpochMillisSlashed(it)
                    } ?: "امروز",
                transactionId = transaction.id,
                accountId = transaction.accountId?.takeIf { it.isNotBlank() },
            )

        val added =
            LoanStore.addPayment(
                loan.id,
                payment
            )

        if (!added) {
            return null
        }

        return payment
    }

    /**
     * Creates the transaction and loan-payment record for a manual
     * installment payment.
     *
     * Both persistence operations belong to the domain boundary so that
     * the UI does not coordinate TransactionStore and LoanStore directly.
     *
     * If the loan payment cannot be recorded after the transaction has
     * been created, the newly-created transaction is rolled back.
     */
    fun recordManualInstallmentPayment(
        context: Context,
        loan: LoanEntry,
        installmentNumber: Int,
        accountId: String,
        paymentDateEpochMillis: Long = System.currentTimeMillis(),
    ): Boolean {
        TransactionStore.attachContext(context)
        LoanStore.attachContext(context)

        if (
            installmentNumber !in 1..loan.totalInstallments ||
            accountId.isBlank()
        ) {
            return false
        }

        val currentLoan =
            LoanStore.getById(loan.id)
                ?: return false

        if (
            currentLoan.payments.any {
                it.installmentNumber == installmentNumber
            }
        ) {
            return false
        }

        val transaction =
            Transaction(
                id = UUID.randomUUID().toString(),
                title = "پرداخت قسط ${currentLoan.title}",
                subtitle = "قسط شماره $installmentNumber",
                amount = currentLoan.installmentAmount,
                type = TransactionType.EXPENSE,
                iconKey = "وام",
                category = "وام",
                dateEpochMillis = paymentDateEpochMillis,
                note = "",
                description = "پرداخت قسط ${currentLoan.title}",
                accountId = accountId
            )

        TransactionStore.addManual(transaction)

        val today =
            JalaliCalendar.fromEpochMillisSlashed(
                paymentDateEpochMillis
            )

        val payment =
            LoanPayment(
                id = UUID.randomUUID().toString(),
                installmentNumber = installmentNumber,
                amount = currentLoan.installmentAmount,
                dateLabel = today,
                transactionId = transaction.id,
                accountId = accountId,
            )

        val added =
            LoanStore.addPayment(
                loanId = currentLoan.id,
                payment = payment
            )

        if (!added) {
            TransactionStore.remove(transaction.id)
            return false
        }

        return true
    }
    /**
     * Records a historical installment payment without creating a financial transaction.
     * This is intended for installments that were paid before the loan was entered into WalletApp.
     */
    fun recordHistoricalInstallmentPayment(
        context: Context,
        loan: LoanEntry,
        installmentNumber: Int,
        amount: Long,
        dateLabel: String,
        accountId: String? = null,
    ): Boolean {
        LoanStore.attachContext(context)
        if (
            installmentNumber !in 1..loan.totalInstallments ||
            amount <= 0L ||
            dateLabel.isBlank()
        ) {
            return false
        }
        val currentLoan = LoanStore.getById(loan.id) ?: return false
        if (currentLoan.payments.any { it.installmentNumber == installmentNumber }) {
            return false
        }
        return LoanStore.addPayment(
            loanId = currentLoan.id,
            payment = LoanPayment(
                id = UUID.randomUUID().toString(),
                installmentNumber = installmentNumber,
                amount = amount,
                dateLabel = dateLabel.trim(),
                transactionId = null,
                accountId = accountId?.takeIf { it.isNotBlank() },
            )
        )
    }

}