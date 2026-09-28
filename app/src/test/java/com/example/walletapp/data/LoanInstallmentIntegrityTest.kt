package com.example.walletapp.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LoanInstallmentIntegrityTest {

    private fun loan(
        historicalPaid: Int = 0,
        payments: List<LoanPayment> = emptyList(),
    ) = LoanEntry(
        id = "loan-1",
        title = "وام آزمایشی",
        type = LoanType.PERSONAL,
        totalAmount = 100_000_000L,
        interestRatePercent = 0.0,
        installmentAmount = 5_000_000L,
        totalInstallments = 24,
        intervalMonths = 1,
        startYear = 1404,
        startMonth = 1,
        startDay = 1,
        firstInstallmentYear = 1404,
        firstInstallmentMonth = 2,
        firstInstallmentDay = 1,
        guaranteeNote = "",
        reminderEnabled = false,
        payments = payments,
        historicalPaidInstallments = historicalPaid,
    )

    @Test
    fun historicalInstallmentIsAlreadyPaid() {
        val loan = loan(historicalPaid = 5)

        assertTrue(loan.isInstallmentPaid(1))
        assertTrue(loan.isInstallmentPaid(5))
        assertFalse(loan.isInstallmentPaid(6))
    }

    @Test
    fun recordedInstallmentIsAlreadyPaid() {
        val loan = loan(
            payments = listOf(
                LoanPayment(
                    id = "payment-7",
                    installmentNumber = 7,
                    amount = 5_000_000L,
                    dateLabel = "1405/01/01",
                )
            )
        )

        assertTrue(loan.isInstallmentPaid(7))
        assertFalse(loan.isInstallmentPaid(8))
    }

    @Test
    fun outOfRangeInstallmentIsNotPaid() {
        val loan = loan(historicalPaid = 5)

        assertFalse(loan.isInstallmentPaid(0))
        assertFalse(loan.isInstallmentPaid(25))
    }
}
