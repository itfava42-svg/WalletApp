package com.example.walletapp.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object LoanLegacyMigration {

    private const val PREFS = "wallet_app_prefs"
    private const val MIGRATION_KEY = "loan_legacy_migration_v1_done"

    private const val LEGACY_LOANS_FILE = "loans.json"
    private const val LEGACY_INSTALLMENTS_FILE = "loan_installments.json"

    fun migrate(context: Context): Boolean {

        val appContext = context.applicationContext

        val prefs =
            appContext.getSharedPreferences(
                PREFS,
                Context.MODE_PRIVATE
            )

        if (prefs.getBoolean(MIGRATION_KEY, false)) {
            return false
        }

        val legacyLoans =
            try {
                readLegacyLoans(appContext)
            } catch (_: Exception) {
                return false
            }

        if (legacyLoans.isEmpty()) {
            prefs.edit()
                .putBoolean(MIGRATION_KEY, true)
                .apply()

            return false
        }

        val legacyInstallments =
            try {
                readLegacyInstallments(appContext)
            } catch (_: Exception) {
                emptyList()
            }

        return try {

            var changed = false
            var allProcessed = true

            for (legacyLoan in legacyLoans) {

                val start =
                    parseJalaliDate(legacyLoan.receiveDate)
                        ?: parseJalaliDate(
                            legacyLoan.firstInstallmentDate
                        )

                val firstInstallment =
                    parseJalaliDate(
                        legacyLoan.firstInstallmentDate
                    )

                if (start == null || firstInstallment == null) {
                    allProcessed = false
                    continue
                }

                var currentLoan =
                    LoanStore.getById(legacyLoan.id)

                if (currentLoan == null) {

                    currentLoan =
                        LoanEntry(
                            id = legacyLoan.id,
                            title = legacyLoan.name,
                            type = LoanType.OTHER,
                            totalAmount = legacyLoan.amount,
                            interestRatePercent =
                                legacyLoan.interestRate,
                            installmentAmount =
                                legacyLoan.monthlyPayment,
                            totalInstallments =
                                legacyLoan.installmentCount,
                            intervalMonths =
                                legacyLoan.installmentInterval,
                            startYear = start.first,
                            startMonth = start.second,
                            startDay = start.third,
                            firstInstallmentYear =
                                firstInstallment.first,
                            firstInstallmentMonth =
                                firstInstallment.second,
                            firstInstallmentDay =
                                firstInstallment.third,
                            guaranteeNote =
                                legacyLoan.guarantee,
                            reminderEnabled = false,
                            payments = emptyList()
                        )

                    LoanStore.add(currentLoan)

                    val addedLoan =
                        LoanStore.getById(legacyLoan.id)

                    if (addedLoan == null) {
                        allProcessed = false
                        continue
                    }

                    currentLoan = addedLoan
                    changed = true
                }

                if (currentLoan == null) {
                    allProcessed = false
                    continue
                }

                var loanForPayments: LoanEntry = currentLoan

                val paymentsForLoan =
                    legacyInstallments
                        .asSequence()
                        .filter {
                            it.loanId == legacyLoan.id &&
                                it.isPaid
                        }
                        .sortedBy {
                            it.number
                        }
                        .toList()

                for (installment in paymentsForLoan) {

                    val existingPayment =
                        loanForPayments.payments.any {
                            it.installmentNumber ==
                                installment.number
                        } ||
                        (
                            !installment.transactionId
                                .isNullOrBlank() &&
                                loanForPayments.payments.any {
                                    it.transactionId ==
                                        installment.transactionId
                                }
                        )

                    if (existingPayment) {
                        continue
                    }

                    val paymentDate =
                        installment.paidDate
                            ?.takeIf {
                                it.isNotBlank()
                            }
                            ?: installment.dueDate

                    val payment =
                        LoanPayment(
                            id = installment.id,
                            installmentNumber =
                                installment.number,
                            amount = installment.amount,
                            dateLabel = paymentDate,
                            transactionId =
                                installment.transactionId
                        )

                    val added =
                        LoanStore.addPayment(
                            legacyLoan.id,
                            payment
                        )

                    if (added) {
                        changed = true

                        val refreshedLoan =
                            LoanStore.getById(
                                legacyLoan.id
                            )

                        if (refreshedLoan == null) {
                            allProcessed = false
                            break
                        }

                        currentLoan = refreshedLoan
                        loanForPayments = refreshedLoan

                    } else {

                        val verified =
                            LoanStore.getById(
                                legacyLoan.id
                            )?.payments?.any {
                                it.installmentNumber ==
                                    installment.number
                            } == true

                        if (!verified) {
                            allProcessed = false
                            break
                        }

                        val refreshedLoan =
                            LoanStore.getById(
                                legacyLoan.id
                            )

                        if (refreshedLoan == null) {
                            allProcessed = false
                            break
                        }

                        currentLoan = refreshedLoan
                        loanForPayments = refreshedLoan
                    }
                }
            }

            if (allProcessed) {
                prefs.edit()
                    .putBoolean(MIGRATION_KEY, true)
                    .apply()
            }

            changed

        } catch (_: Exception) {
            false
        }
    }

    private fun readLegacyLoans(
        context: Context
    ): List<LegacyLoanRecord> {

        val text =
            context.openFileInput(
                LEGACY_LOANS_FILE
            ).bufferedReader().use {
                it.readText()
            }

        val array = JSONArray(text)
        val result = mutableListOf<LegacyLoanRecord>()

        for (i in 0 until array.length()) {

            val obj =
                array.getJSONObject(i)

            result +=
                LegacyLoanRecord(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    amount = obj.getLong("amount"),
                    interestRate =
                        obj.getDouble("interestRate"),
                    installmentCount =
                        obj.getInt("installmentCount"),
                    installmentInterval =
                        obj.getInt(
                            "installmentInterval"
                        ),
                    monthlyPayment =
                        obj.getLong("monthlyPayment"),
                    firstInstallmentDate =
                        obj.getString(
                            "firstInstallmentDate"
                        ),
                    receiveDate =
                        obj.getString(
                            "receiveDate"
                        ),
                    guarantee = "",
                )
        }

        return result
    }

    private fun readLegacyInstallments(
        context: Context
    ): List<LegacyInstallmentRecord> {

        val text =
            context.openFileInput(
                LEGACY_INSTALLMENTS_FILE
            ).bufferedReader().use {
                it.readText()
            }

        val array = JSONArray(text)
        val result =
            mutableListOf<LegacyInstallmentRecord>()

        for (i in 0 until array.length()) {

            val obj =
                array.getJSONObject(i)

            result +=
                LegacyInstallmentRecord(
                    id = obj.getString("id"),
                    loanId =
                        obj.getString("loanId"),
                    number =
                        obj.getInt("number"),
                    dueDate =
                        obj.getString("dueDate"),
                    amount =
                        obj.getLong("amount"),
                    isPaid =
                        obj.optBoolean(
                            "isPaid",
                            false
                        ),
                    paidDate =
                        obj.optString(
                            "paidDate"
                        ).takeIf {
                            it.isNotBlank()
                        },
                    transactionId =
                        obj.optString(
                            "transactionId"
                        ).takeIf {
                            it.isNotBlank()
                        }
                )
        }

        return result
    }

    private fun parseJalaliDate(
        value: String?
    ): Triple<Int, Int, Int>? {

        val raw =
            value?.trim().orEmpty()

        if (raw.isEmpty()) {
            return null
        }

        val parts =
            raw.split("/")

        if (parts.size != 3) {
            return null
        }

        val year =
            parts[0]
                .trim()
                .toIntOrNull()
                ?: return null

        val month =
            parts[1]
                .trim()
                .toIntOrNull()
                ?: return null

        val day =
            parts[2]
                .trim()
                .toIntOrNull()
                ?: return null

        if (year <= 0) {
            return null
        }

        if (month !in 1..12) {
            return null
        }

        if (day !in 1..31) {
            return null
        }

        return Triple(
            year,
            month,
            day
        )
    }

    private data class LegacyLoanRecord(
        val id: String,
        val name: String,
        val amount: Long,
        val interestRate: Double,
        val installmentCount: Int,
        val installmentInterval: Int,
        val monthlyPayment: Long,
        val firstInstallmentDate: String,
        val receiveDate: String,
        val guarantee: String
    )

    private data class LegacyInstallmentRecord(
        val id: String,
        val loanId: String,
        val number: Int,
        val dueDate: String,
        val amount: Long,
        val isPaid: Boolean,
        val paidDate: String?,
        val transactionId: String?
    )
}





