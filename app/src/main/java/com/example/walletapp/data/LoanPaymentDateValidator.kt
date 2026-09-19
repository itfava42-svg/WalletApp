package com.example.walletapp.data

import java.util.Calendar

object LoanPaymentDateValidator {

    /**
     * Checks the calendar date of a payment transaction against
     * the Jalali loan start date.
     *
     * Time-of-day is intentionally ignored.
     * A payment on the same Jalali day as the loan start is valid.
     */
    fun isPaymentDateValid(
        loan: LoanEntry,
        transactionDateEpochMillis: Long,
    ): Boolean {
        val (startGy, startGm, startGd) =
            JalaliCalendar.jalaliToGregorian(
                loan.startYear,
                loan.startMonth,
                loan.startDay
            )

        val transactionCalendar =
            Calendar.getInstance().apply {
                timeInMillis = transactionDateEpochMillis
            }

        val transactionDateKey =
            transactionCalendar.get(Calendar.YEAR) * 10000 +
                (transactionCalendar.get(Calendar.MONTH) + 1) * 100 +
                transactionCalendar.get(Calendar.DAY_OF_MONTH)

        val loanStartDateKey =
            startGy * 10000 +
                startGm * 100 +
                startGd

        return transactionDateKey >= loanStartDateKey
    }

    const val ERROR_MESSAGE =
        "تاریخ پرداخت قسط نمی‌تواند قبل از تاریخ دریافت وام باشد."
}
