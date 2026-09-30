package com.example.walletapp.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExistingLoanDueDateIntegrityTest {

    @Test
    fun existingLoanContinuesFromTrackingStartAfterHistoricalPayments() {
        val loan = LoanEntry(
            id = "existing-loan",
            title = "وام آزمایشی",
            type = LoanType.PERSONAL,
            totalAmount = 10_000_000L,
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
            trackingStartDay = 1
        )

        assertEquals(Triple(1403, 5, 1), loan.installmentDueDateParts(4))
        assertEquals(Triple(1403, 6, 1), loan.installmentDueDateParts(5))
        assertEquals(Triple(1403, 11, 1), loan.installmentDueDateParts(10))
    }

    @Test
    fun existingLoanRespectsThreeMonthInterval() {
        val loan = LoanEntry(
            id = "existing-loan-3m",
            title = "وام آزمایشی",
            type = LoanType.PERSONAL,
            totalAmount = 10_000_000L,
            interestRatePercent = 0.0,
            installmentAmount = 1_000_000L,
            totalInstallments = 8,
            intervalMonths = 3,
            startYear = 1404,
            startMonth = 1,
            startDay = 1,
            firstInstallmentYear = 1404,
            firstInstallmentMonth = 2,
            firstInstallmentDay = 15,
            guaranteeNote = "",
            reminderEnabled = true,
            payments = emptyList(),
            trackingMode = LoanTrackingMode.EXISTING,
            historicalPaidInstallments = 2,
            trackingStartYear = 1404,
            trackingStartMonth = 10,
            trackingStartDay = 15
        )

        assertEquals(Triple(1404, 10, 15), loan.installmentDueDateParts(3))
        assertEquals(Triple(1405, 1, 15), loan.installmentDueDateParts(4))
        assertEquals(Triple(1405, 4, 15), loan.installmentDueDateParts(5))
    }

    @Test
    fun newLoanUsesFirstInstallmentSchedule() {
        val loan = LoanEntry(
            id = "new-loan",
            title = "وام آزمایشی",
            type = LoanType.PERSONAL,
            totalAmount = 10_000_000L,
            interestRatePercent = 0.0,
            installmentAmount = 1_000_000L,
            totalInstallments = 6,
            intervalMonths = 2,
            startYear = 1405,
            startMonth = 1,
            startDay = 1,
            firstInstallmentYear = 1405,
            firstInstallmentMonth = 2,
            firstInstallmentDay = 30,
            guaranteeNote = "",
            reminderEnabled = true,
            payments = emptyList(),
            trackingMode = LoanTrackingMode.NEW,
            historicalPaidInstallments = 0
        )

        assertEquals(Triple(1405, 2, 30), loan.installmentDueDateParts(1))
        assertEquals(Triple(1405, 4, 30), loan.installmentDueDateParts(2))
        assertEquals(Triple(1405, 6, 30), loan.installmentDueDateParts(3))
    }

    @Test
    fun invalidInstallmentNumberReturnsNull() {
        val loan = LoanEntry(
            id = "invalid-loan",
            title = "وام آزمایشی",
            type = LoanType.PERSONAL,
            totalAmount = 10_000_000L,
            interestRatePercent = 0.0,
            installmentAmount = 1_000_000L,
            totalInstallments = 6,
            intervalMonths = 1,
            startYear = 1405,
            startMonth = 1,
            startDay = 1,
            firstInstallmentYear = 1405,
            firstInstallmentMonth = 2,
            firstInstallmentDay = 1,
            guaranteeNote = "",
            reminderEnabled = true,
            payments = emptyList(),
            trackingMode = LoanTrackingMode.EXISTING,
            historicalPaidInstallments = 2,
            trackingStartYear = 1405,
            trackingStartMonth = 3,
            trackingStartDay = 1
        )

        assertNull(loan.installmentDueDateParts(0))
        assertNull(loan.installmentDueDateParts(7))
    }
}
