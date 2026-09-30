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
            trackingMode = LoanTrackingMode.EXISTING,
            historicalPaidInstallments = 3,
            firstInstallment = Triple(1403, 2, 1),
            trackingStart = Triple(1403, 5, 1),
        )

        assertEquals(Triple(1403, 5, 1), loan.installmentDueDateParts(4))
        assertEquals(Triple(1403, 6, 1), loan.installmentDueDateParts(5))
        assertEquals(Triple(1403, 11, 1), loan.installmentDueDateParts(10))
    }

    @Test
    fun existingLoanRespectsInstallmentInterval() {
        val loan = loan(
            totalInstallments = 8,
            intervalMonths = 3,
            trackingMode = LoanTrackingMode.EXISTING,
            historicalPaidInstallments = 2,
            firstInstallment = Triple(1403, 1, 1),
            trackingStart = Triple(1404, 10, 15),
        )

        assertEquals(Triple(1404, 10, 15), loan.installmentDueDateParts(3))
        assertEquals(Triple(1405, 1, 15), loan.installmentDueDateParts(4))
        assertEquals(Triple(1405, 4, 15), loan.installmentDueDateParts(5))
    }

    @Test
    fun newLoanKeepsFirstInstallmentSchedule() {
        val loan = loan(
            totalInstallments = 6,
            intervalMonths = 2,
            trackingMode = LoanTrackingMode.NEW,
            historicalPaidInstallments = 0,
            firstInstallment = Triple(1405, 2, 30),
            trackingStart = null,
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
            trackingMode = LoanTrackingMode.EXISTING,
            historicalPaidInstallments = 2,
            firstInstallment = Triple(1405, 1, 1),
            trackingStart = Triple(1405, 3, 1),
        )

        assertNull(loan.installmentDueDateParts(0))
        assertNull(loan.installmentDueDateParts(7))
    }

    private fun loan(
        totalInstallments: Int,
        intervalMonths: Int,
        trackingMode: LoanTrackingMode,
        historicalPaidInstallments: Int,
        firstInstallment: Triple<Int, Int, Int>,
        trackingStart: Triple<Int, Int, Int>?,
    ) = LoanEntry(
        id = "loan-test",
        title = "تست",
        type = LoanType.PERSONAL,
        totalAmount = 10_000_000L,
        interestRatePercent = 0.0,
        installmentAmount = 1_000_000L,
        totalInstallments = totalInstallments,
        intervalMonths = intervalMonths,
        startYear = 1403,
        startMonth = 1,
        startDay = 1,
        firstInstallmentYear = firstInstallment.first,
        firstInstallmentMonth = firstInstallment.second,
        firstInstallmentDay = firstInstallment.third,
        guaranteeNote = "",
        reminderEnabled = true,
        payments = emptyList(),
        trackingMode = trackingMode,
        historicalPaidInstallments = historicalPaidInstallments,
        trackingStartYear = trackingStart?.first,
        trackingStartMonth = trackingStart?.second,
        trackingStartDay = trackingStart?.third,
    )
}
