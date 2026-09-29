package com.example.walletapp.sms

import com.example.walletapp.data.LoanEntry
import com.example.walletapp.data.LoanPayment
import com.example.walletapp.data.LoanTrackingMode
import com.example.walletapp.data.LoanType
import com.example.walletapp.data.Transaction
import com.example.walletapp.data.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SmsLoanReminderBoundaryTest {

    private fun loan(
        historicalPaidInstallments: Int = 2,
        payments: List<LoanPayment> = emptyList(),
        reminderEnabled: Boolean = true,
    ) = LoanEntry(
        id = "loan-1",
        title = "وام نمونه",
        type = LoanType.PERSONAL,
        totalAmount = 12_000_000L,
        interestRatePercent = 0.0,
        installmentAmount = 1_000_000L,
        totalInstallments = 12,
        intervalMonths = 1,
        startYear = 1404,
        startMonth = 1,
        startDay = 1,
        firstInstallmentYear = 1404,
        firstInstallmentMonth = 1,
        firstInstallmentDay = 1,
        guaranteeNote = "",
        reminderEnabled = reminderEnabled,
        payments = payments,
        trackingMode = LoanTrackingMode.EXISTING,
        historicalPaidInstallments = historicalPaidInstallments,
        trackingStartYear = 1405,
        trackingStartMonth = 1,
        trackingStartDay = 15,
    )

    @Test
    fun historicalInstallmentCannotBeSelectedAsUnpaid() {
        val loan = loan(historicalPaidInstallments = 2)

        assertTrue(loan.isInstallmentPaid(1))
        assertTrue(loan.isInstallmentPaid(2))
        assertFalse(loan.isInstallmentPaid(3))
    }

    @Test
    fun recordedSmsPaymentMakesInstallmentPaid() {
        val loan = loan(
            historicalPaidInstallments = 2,
            payments = listOf(
                LoanPayment(
                    id = "payment-3",
                    installmentNumber = 3,
                    amount = 1_000_000L,
                    dateLabel = "1405/01/15",
                    transactionId = "sms-transaction-3",
                )
            )
        )

        assertTrue(loan.isInstallmentPaid(3))
        assertFalse(loan.isInstallmentPaid(4))
    }

    @Test
    fun smsInstallmentCompletionMapsToExpenseAndLoanCategory() {
        val transaction = Transaction(
            id = "sms-installment",
            title = "بانک نمونه",
            subtitle = "تراکنش بانکی",
            amount = 1_000_000L,
            type = TransactionType.EXPENSE,
            iconKey = "بانک",
            requiresCompletion = true,
        )

        val request = SmsTransactionCompletionHandler.CompletionRequest(
            transaction = transaction,
            selectedType = TransactionType.EXPENSE,
            isTransfer = false,
            destinationAccountId = null,
            sender = "1000",
            rawBody = "پرداخت قسط",
            selectedBank = "بانک نمونه",
            selectedCategory = "قسط",
            tags = emptyList(),
            description = "پرداخت قسط وام",
            receiptUri = null,
            selectedAccountId = "account-1",
            smsAccountNumber = null,
            smsCardLast4 = null,
            learnBank = false,
            selectedLoan = loan(historicalPaidInstallments = 2),
            selectedInstallmentNumber = 3,
        )

        val updated =
            SmsTransactionCompletionHandler.buildUpdatedTransaction(
                request = request,
                resolvedAccountId = "account-1",
            )

        assertEquals(TransactionType.EXPENSE, updated.type)
        assertEquals("account-1", updated.accountId)
        assertEquals("قسط", updated.category)
        assertEquals(false, updated.requiresCompletion)
    }
}
