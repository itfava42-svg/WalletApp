package com.example.walletapp.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExistingLoanDueDateIntegrityTest {

    @Test
    fun existingLoanContinuesFromTrackingStartAfterHistoricalPayments() {
        val loan = loan(
            totalInstallments = 12,
            intervalMonths = 1,
            historicalPaidInstallments = 3,
            trackingMode = LoanTrackingMode.EXISTING,
            trackingStartYear = 1403,
            trackingStartMonth = 5,
            trackingStartDay = 1,
        )

        assertEquals(Triple(1403, 5, 1), loan.installmentDueDateParts(4))
        assertEquals(Triple(1403, 6, 1), loan.installmentDueDateParts(5))
        assertEquals(Triple(1403, 11, 1), loan.installmentDueDateParts(10))
    }

    @Test
    fun existingLoanRespectsThreeMonthInterval() {
        val loan = loan(
            totalInstallments = 8,
            intervalMonths = 3,
            historicalPaidInstallments = 2,
            trackingMode = LoanTrackingMode.EXISTING,
            trackingStartYear = 1404,
            trackingStartMonth = 10,
            trackingStartDay = 15,
        )

        assertEquals(Triple(1404, 10, 15), loan.installmentDueDateParts(3))
        assertEquals(Triple(1405, 1, 15), loan.installmentDueDateParts(4))
        assertEquals(Triple(1405, 4, 15), loan.installmentDueDateParts(5))
    }

    @Test
    fun newLoanUsesFirstInstallmentSchedule() {
        val loan = loan(
            totalInstallments = 6,
            intervalMonths = 2,
            historicalPaidInstallments = 0,
            trackingMode = LoanTrackingMode.NEW,
            firstInstallmentYear = 1405,
            firstInstallmentMonth = 2,
            firstInstallmentDay = 30,
        )

        assertEquals(Triple(1405, 2, 30), loan.installmentDueDateParts(1))
        assertEquals(Triple(1405, 4, 30), loan.installmentDueDateParts(2))
        assertEquals(Triple(1405, 6, 30), loan.installmentDueDateParts(3))
    }

    @Test
    fun invalidInstallmentNumberReturnsNull() {
        val loan = loan(
            totalInstallments = 6,
            intervalMonths = 1,
            historicalPaidInstallments = 2,
            trackingMode = LoanTrackingMode.EXISTING,
            trackingStartYear = 1405,
            trackingStartMonth = 3,
            trackingStartDay = 1,
        )

        assertNull(loan.installmentDueDateParts(0))
        assertNull(loan.installmentDueDateParts(7))
    }

    private fun loan(
        totalInstallments: Int,
        intervalMonths: Int,
        historicalPaidInstallments: Int,
        trackingMode: LoanTrackingMode,
        firstInstallmentYear: Int = 1403,
        firstInstallmentMonth: Int = 2,
        firstInstallmentDay: Int = 1,
        trackingStartYear: Int? = null,
        trackingStartMonth: Int? = null,
        trackingStartDay: Int? = null,
    ) = LoanEntry(
        id = "loan-test",
        title = "وام آزمایشی",
        type = LoanType.PERSONAL,
        totalAmount = 10_000_000L,
        interestRatePercent = 0.0,
        installmentAmount = 1_000_000L,
        totalInstallments = totalInstallments,
        intervalMonths = intervalMonths,
        startYear = 1403,
        startMonth = 1,
        startDay = 1,
        firstInstallmentYear = firstInstallmentYear,
        firstInstallmentMonth = firstInstallmentMonth,
        firstInstallmentDay = firstInstallmentDay,
        guaranteeNote = "",
        reminderEnabled = true,
        payments = emptyList(),
        trackingMode = trackingMode,
        historicalPaidInstallments = historicalPaidInstallments,
        trackingStartYear = trackingStartYear,
        trackingStartMonth = trackingStartMonth,
        trackingStartDay = trackingStartDay,
    )
}
