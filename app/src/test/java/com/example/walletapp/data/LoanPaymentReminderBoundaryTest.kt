package com.example.walletapp.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LoanPaymentReminderBoundaryTest {

    @Test
    fun recordedPaymentMakesInstallmentUnavailableForFutureReminderSync() {
        val loan = LoanEntry(
            id = "loan-reminder-1",
            title = "وام آزمایشی",
            type = LoanType.PERSONAL,
            totalAmount = 3_000_000L,
            interestRatePercent = 0.0,
            installmentAmount = 1_000_000L,
            totalInstallments = 3,
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
                    id = "payment-1",
                    installmentNumber = 1,
                    amount = 1_000_000L,
                    dateLabel = "1405/02/01",
                    transactionId = "sms-tx-1",
                )
            ),
        )

        assertTrue(loan.isInstallmentPaid(1))
        assertFalse(loan.isInstallmentPaid(2))
    }

    @Test
    fun historicalAndRecordedPaymentsBothStayExcludedFromReminderCandidates() {
        val loan = LoanEntry(
            id = "loan-reminder-2",
            title = "وام قبلی",
            type = LoanType.PERSONAL,
            totalAmount = 5_000_000L,
            interestRatePercent = 0.0,
            installmentAmount = 1_000_000L,
            totalInstallments = 5,
            intervalMonths = 1,
            startYear = 1404,
            startMonth = 1,
            startDay = 1,
            firstInstallmentYear = 1404,
            firstInstallmentMonth = 2,
            firstInstallmentDay = 1,
            guaranteeNote = "",
            reminderEnabled = true,
            trackingMode = LoanTrackingMode.EXISTING,
            historicalPaidInstallments = 2,
            trackingStartYear = 1404,
            trackingStartMonth = 3,
            trackingStartDay = 1,
            payments = listOf(
                LoanPayment(
                    id = "payment-4",
                    installmentNumber = 4,
                    amount = 1_000_000L,
                    dateLabel = "1404/04/01",
                    transactionId = "sms-tx-4",
                )
            ),
        )

        assertTrue(loan.isInstallmentPaid(1))
        assertTrue(loan.isInstallmentPaid(2))
        assertFalse(loan.isInstallmentPaid(3))
        assertTrue(loan.isInstallmentPaid(4))
        assertFalse(loan.isInstallmentPaid(5))
    }
}
