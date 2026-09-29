package com.example.walletapp.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoricalLoanReminderIntegrityTest {

    @Test
    fun historicalPaidInstallmentIsNotConsideredOutstanding() {
        val loan = LoanEntry(
            id = "loan-1",
            title = "وام قبلی",
            type = LoanType.PERSONAL,
            totalAmount = 12_000_000L,
            interestRatePercent = 0.0,
            installmentAmount = 1_000_000L,
            totalInstallments = 12,
            intervalMonths = 1,
            startYear = 1403,
            startMonth = 1,
            startDay = 1,
            firstInstallmentYear = 1403,
            firstInstallmentMonth = 2,
            firstInstallmentDay = 1,
            guaranteeNote = "",
            reminderEnabled = true,
            payments = emptyList(),
            trackingMode = LoanTrackingMode.EXISTING,
            historicalPaidInstallments = 3,
            trackingStartYear = 1403,
            trackingStartMonth = 5,
            trackingStartDay = 1,
        )

        assertTrue(loan.isInstallmentPaid(1))
        assertTrue(loan.isInstallmentPaid(2))
        assertTrue(loan.isInstallmentPaid(3))
        assertFalse(loan.isInstallmentPaid(4))
    }

    @Test
    fun recordedPaymentIsAlsoExcludedFromOutstandingInstallments() {
        val loan = LoanEntry(
            id = "loan-2",
            title = "وام جاری",
            type = LoanType.PERSONAL,
            totalAmount = 5_000_000L,
            interestRatePercent = 0.0,
            installmentAmount = 1_000_000L,
            totalInstallments = 5,
            intervalMonths = 1,
            startYear = 1405,
            startMonth = 1,
            startDay = 1,
            firstInstallmentYear = 1405,
            firstInstallmentMonth = 2,
            firstInstallmentDay = 1,
            guaranteeNote = "",
            reminderEnabled = true,
            payments = listOf(
                LoanPayment(
                    id = "payment-4",
                    installmentNumber = 4,
                    amount = 1_000_000L,
                    dateLabel = "1405/04/01",
                    transactionId = "tx-4",
                )
            ),
            historicalPaidInstallments = 0,
        )

        assertTrue(loan.isInstallmentPaid(4))
        assertFalse(loan.isInstallmentPaid(3))
    }
}
