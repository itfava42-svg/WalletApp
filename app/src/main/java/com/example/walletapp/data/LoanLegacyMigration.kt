package com.example.walletapp.data

import android.content.Context
import com.example.walletapp.loan.LoanInstallmentStore
import com.example.walletapp.loan.LoanPersistence
import com.example.walletapp.loan.LoanStore as LegacyLoanStore

object LoanLegacyMigration {
    private const val PREFS = "wallet_app_prefs"
    private const val MIGRATION_KEY = "loan_legacy_migration_v1_done"

    fun migrate(context: Context): Boolean {
        val appContext = context.applicationContext
        val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        if (prefs.getBoolean(MIGRATION_KEY, false)) return false

        val legacyLoans = try {
            LoanPersistence.load(appContext)
        } catch (_: Exception) {
            return false
        }

        if (legacyLoans.isEmpty()) {
            prefs.edit().putBoolean(MIGRATION_KEY, true).apply()
            return false
        }

        return try {
            LegacyLoanStore.attachContext(appContext)
            LoanInstallmentStore.attachContext(appContext)

            var changed = false
            var allProcessed = true

            for (legacyLoan in legacyLoans) {
                if (LoanStore.getById(legacyLoan.id) != null) {
                    continue
                }

                val start =
                    parseJalaliDate(legacyLoan.receiveDate)
                        ?: parseJalaliDate(legacyLoan.firstInstallmentDate)

                val firstInstallment =
                    parseJalaliDate(legacyLoan.firstInstallmentDate)

                if (start == null || firstInstallment == null) {
                    allProcessed = false
                    continue
                }

                val activeLoan = LoanEntry(
                    id = legacyLoan.id,
                    title = legacyLoan.name,
                    type = LoanType.OTHER,
                    totalAmount = legacyLoan.amount,
                    interestRatePercent = legacyLoan.interestRate,
                    installmentAmount = legacyLoan.monthlyPayment,
                    totalInstallments = legacyLoan.installmentCount,
                    intervalMonths = legacyLoan.installmentInterval,
                    startYear = start.first,
                    startMonth = start.second,
                    startDay = start.third,
                    firstInstallmentYear = firstInstallment.first,
                    firstInstallmentMonth = firstInstallment.second,
                    firstInstallmentDay = firstInstallment.third,
                    guaranteeNote = legacyLoan.guarantee,
                    reminderEnabled = false,
                    payments = emptyList()
                )

                LoanStore.add(activeLoan)
                changed = true

                val schedule = LoanInstallmentStore.getSchedule(legacyLoan)

                schedule
                    .asSequence()
                    .filter { it.isPaid }
                    .sortedBy { it.number }
                    .forEach { installment ->
                        val paymentDate =
                            installment.paidDate
                                ?.takeIf { it.isNotBlank() }
                                ?: installment.dueDate

                        val payment = LoanPayment(
                            id = installment.id,
                            installmentNumber = installment.number,
                            amount = installment.amount,
                            dateLabel = paymentDate,
                            transactionId = installment.transactionId
                        )

                        LoanStore.addPayment(
                            legacyLoan.id,
                            payment
                        )
                    }
            }

            if (allProcessed) {
                prefs.edit().putBoolean(MIGRATION_KEY, true).apply()
            }

            changed
        } catch (_: Exception) {
            false
        }
    }

    private fun parseJalaliDate(value: String?): Triple<Int, Int, Int>? {
        val raw = value?.trim().orEmpty()
        if (raw.isEmpty()) return null

        val parts = raw.split("/")
        if (parts.size != 3) return null

        val year = parts[0].trim().toIntOrNull() ?: return null
        val month = parts[1].trim().toIntOrNull() ?: return null
        val day = parts[2].trim().toIntOrNull() ?: return null

        if (year <= 0) return null
        if (month !in 1..12) return null
        if (day !in 1..31) return null

        return Triple(year, month, day)
    }
}
