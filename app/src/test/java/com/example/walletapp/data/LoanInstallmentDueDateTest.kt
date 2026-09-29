package com.example.walletapp.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LoanInstallmentDueDateTest {

    private fun loan(
        trackingMode: LoanTrackingMode = LoanTrackingMode.NEW,
        historicalPaidInstallments: Int = 0,
        trackingStartYear: Int? = null,
        trackingStartMonth: Int? = null,
        trackingStartDay: Int? = null
    ) = LoanEntry(
        id = "loan-1",
        title = "وام آزمایشی",
        type = LoanType.PERSONAL,
        totalAmount = 120_000_000L,
        interestRatePercent = 0.0,
        installmentAmount = 10_000_000L,
        totalInstallments = 12,
        intervalMonths = 1,
        startYear = 1403,
        startMonth = 1,
        startDay = 1,
        firstInstallmentYear = 1403,
        firstInstallmentMonth = 2,
        firstInstallmentDay = 15,
        guaranteeNote = "",
        reminderEnabled = true,
        payments = emptyList(),
        trackingMode = trackingMode,
        historicalPaidInstallments = historicalPaidInstallments,
        trackingStartYear = trackingStartYear,
        trackingStartMonth = trackingStartMonth,
        trackingStartDay = trackingStartDay
    )

    @Test
    fun existingLoanContinuesFromTrackingStartAfterHistoricalInstallments() {
        val result = loan(
            trackingMode = LoanTrackingMode.EXISTING,
            historicalPaidInstallments = 5,
            trackingStartYear = 1405,
            trackingStartMonth = 1,
            trackingStartDay = 15
        ).installmentDueDateParts(6)

        assertEquals(Triple(1405, 1, 15), result)
    }

    @Test
    fun existingLoanAdvancesByIntervalForLaterInstallments() {
        val result = loan(
            trackingMode = LoanTrackingMode.EXISTING,
            historicalPaidInstallments = 5,
            trackingStartYear = 1405,
            trackingStartMonth = 1,
            trackingStartDay = 15
        ).installmentDueDateParts(7)

        assertEquals(Triple(1405, 2, 15), result)
    }

    @Test
    fun newLoanUsesFirstInstallmentDate() {
        val result = loan().installmentDueDateParts(2)

        assertEquals(Triple(1403, 3, 15), result)
    }

    @Test
    fun missingExistingTrackingDateFallsBackToFirstInstallmentDate() {
        val result = loan(
            trackingMode = LoanTrackingMode.EXISTING,
            historicalPaidInstallments = 5
        ).installmentDueDateParts(6)

        assertEquals(Triple(1403, 7, 15), result)
    }

    @Test
    fun outOfRangeInstallmentHasNoDueDate() {
        assertNull(loan().installmentDueDateParts(13))
        assertNull(loan().installmentDueDateParts(0))
    }
}
