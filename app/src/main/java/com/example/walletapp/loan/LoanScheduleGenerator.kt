package com.example.walletapp.loan

object LoanScheduleGenerator {

    fun generate(
        loan: Loan
    ): List<LoanInstallment> {

        if (loan.installmentCount <= 0) {
            return emptyList()
        }

        val result = mutableListOf<LoanInstallment>()

        for (number in 1..loan.installmentCount) {

            result.add(
                LoanInstallment(
                    loanId = loan.id,
                    number = number,
                    dueDate = calculateDate(
                        loan.firstInstallmentDate,
                        number - 1,
                        loan.installmentInterval
                    ),
                    amount = loan.monthlyPayment
                )
            )
        }

        return result
    }

    private fun calculateDate(
        start: String,
        monthsToAdd: Int,
        interval: Int
    ): String {

        val parts = start
            .replace("۰", "0")
            .replace("۱", "1")
            .replace("۲", "2")
            .replace("۳", "3")
            .replace("۴", "4")
            .replace("۵", "5")
            .replace("۶", "6")
            .replace("۷", "7")
            .replace("۸", "8")
            .replace("۹", "9")
            .split("/")

        if (parts.size != 3) {
            return start
        }

        val year = parts[0].toIntOrNull() ?: return start
        val month = parts[1].toIntOrNull() ?: return start
        val day = parts[2].toIntOrNull() ?: return start

        val totalMonths =
            (year * 12) + (month - 1) + (monthsToAdd * interval)

        val newYear = totalMonths / 12
        val newMonth = (totalMonths % 12) + 1

        val maxDay =
            if (newMonth <= 6) {
                31
            } else {
                30
            }

        val newDay = day.coerceAtMost(maxDay)

        return "$newYear/${newMonth.toString().padStart(2, '0')}/${newDay.toString().padStart(2, '0')}"
    }
}
