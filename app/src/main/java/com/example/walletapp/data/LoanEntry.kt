package com.example.walletapp.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import org.json.JSONArray
import org.json.JSONObject
import com.example.walletapp.reminders.LoanReminderSynchronizer

enum class LoanType(val label: String) {
    MORTGAGE("مسکن"),
    CAR("خودرو"),
    PERSONAL("شخصی"),
    BUSINESS("کسب‌وکار"),
    OTHER("سایر")
}

enum class LoanTrackingMode(val label: String) {
    NEW("وام جدید"),
    EXISTING("وام قبلی")
}

data class LoanPayment(
    val id: String,
    val installmentNumber: Int,
    val amount: Long,
    val dateLabel: String,
    val transactionId: String? = null,
)

data class LoanEntry(
    val id: String,
    val title: String,
    val type: LoanType,
    val totalAmount: Long,
    val interestRatePercent: Double,
    val installmentAmount: Long,
    val totalInstallments: Int,
    val intervalMonths: Int,
    val startYear: Int,
    val startMonth: Int,
    val startDay: Int,
    val firstInstallmentYear: Int?,
    val firstInstallmentMonth: Int?,
    val firstInstallmentDay: Int?,
    val guaranteeNote: String,
    val reminderEnabled: Boolean,
    val payments: List<LoanPayment>,
    val trackingMode: LoanTrackingMode = LoanTrackingMode.NEW,
    val historicalPaidInstallments: Int = 0,
    val trackingStartYear: Int? = null,
    val trackingStartMonth: Int? = null,
    val trackingStartDay: Int? = null
)

object LoanStore {

    val loans = mutableStateListOf<LoanEntry>()

    private var context: Context? = null
    private var loaded = false

    private const val PREFS = "wallet_app_prefs"
    private const val KEY = "loans_json"

    fun attachContext(ctx: Context) {
        if (context == null) {
            context = ctx.applicationContext
        }

        if (!loaded) {
            loaded = true
            load()
        }
    }

    private fun load() {
        val ctx = context ?: return

        val json = ctx
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, null)
            ?: return

        runCatching {
            val array = JSONArray(json)
            loans.clear()

            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)

                val paymentsArray = item.optJSONArray("payments") ?: JSONArray()
                val payments = mutableListOf<LoanPayment>()

                for (j in 0 until paymentsArray.length()) {
                    val payment = paymentsArray.getJSONObject(j)

                    payments.add(
                        LoanPayment(
                            id = payment.getString("id"),
                            installmentNumber = payment.optInt("installmentNumber", 0),
                            amount = payment.getLong("amount"),
                            dateLabel = payment.getString("dateLabel"),
                            transactionId = payment.optString("transactionId").takeIf { it.isNotBlank() },
                        )
                    )
                }

                loans.add(
                    LoanEntry(
                        id = item.getString("id"),
                        title = item.getString("title"),
                        type = LoanType.valueOf(item.getString("type")),
                        totalAmount = item.getLong("totalAmount"),
                        interestRatePercent =
                            item.optDouble("interestRatePercent", 0.0),
                        installmentAmount =
                            item.getLong("installmentAmount"),
                        totalInstallments =
                            item.getInt("totalInstallments"),
                        intervalMonths =
                            item.optInt("intervalMonths", 1),
                        startYear =
                            item.getInt("startYear"),
                        startMonth =
                            item.getInt("startMonth"),
                        startDay =
                            item.getInt("startDay"),
                        firstInstallmentYear =
                            if (item.isNull("firstInstallmentYear"))
                                null
                            else
                                item.optInt("firstInstallmentYear"),
                        firstInstallmentMonth =
                            if (item.isNull("firstInstallmentMonth"))
                                null
                            else
                                item.optInt("firstInstallmentMonth"),
                        firstInstallmentDay =
                            if (item.isNull("firstInstallmentDay"))
                                null
                            else
                                item.optInt("firstInstallmentDay"),
                        guaranteeNote =
                            item.optString("guaranteeNote", ""),
                        reminderEnabled =
                            item.optBoolean("reminderEnabled", false),
                        payments = payments,
                        trackingMode =
                            runCatching {
                                LoanTrackingMode.valueOf(
                                    item.optString(
                                        "trackingMode",
                                        LoanTrackingMode.NEW.name
                                    )
                                )
                            }.getOrDefault(LoanTrackingMode.NEW),
                        historicalPaidInstallments =
                            item.optInt(
                                "historicalPaidInstallments",
                                0
                            ).coerceAtLeast(0),
                        trackingStartYear =
                            if (item.isNull("trackingStartYear"))
                                null
                            else
                                item.optInt("trackingStartYear"),
                        trackingStartMonth =
                            if (item.isNull("trackingStartMonth"))
                                null
                            else
                                item.optInt("trackingStartMonth"),
                        trackingStartDay =
                            if (item.isNull("trackingStartDay"))
                                null
                            else
                                item.optInt("trackingStartDay")
                    )
                )
            }
        }
    }

    private fun save() {
        val ctx = context ?: return

        val array = JSONArray()

        loans.forEach { loan ->
            val item = JSONObject()

            item.put("id", loan.id)
            item.put("title", loan.title)
            item.put("type", loan.type.name)
            item.put("totalAmount", loan.totalAmount)
            item.put(
                "interestRatePercent",
                loan.interestRatePercent
            )
            item.put(
                "installmentAmount",
                loan.installmentAmount
            )
            item.put(
                "totalInstallments",
                loan.totalInstallments
            )
            item.put(
                "intervalMonths",
                loan.intervalMonths
            )
            item.put("startYear", loan.startYear)
            item.put("startMonth", loan.startMonth)
            item.put("startDay", loan.startDay)

            if (loan.firstInstallmentYear == null) {
                item.put("firstInstallmentYear", JSONObject.NULL)
            } else {
                item.put(
                    "firstInstallmentYear",
                    loan.firstInstallmentYear
                )
            }

            if (loan.firstInstallmentMonth == null) {
                item.put("firstInstallmentMonth", JSONObject.NULL)
            } else {
                item.put(
                    "firstInstallmentMonth",
                    loan.firstInstallmentMonth
                )
            }

            if (loan.firstInstallmentDay == null) {
                item.put("firstInstallmentDay", JSONObject.NULL)
            } else {
                item.put(
                    "firstInstallmentDay",
                    loan.firstInstallmentDay
                )
            }

            item.put("guaranteeNote", loan.guaranteeNote)
            item.put("reminderEnabled", loan.reminderEnabled)
            item.put("trackingMode", loan.trackingMode.name)
            item.put(
                "historicalPaidInstallments",
                loan.historicalPaidInstallments
            )

            if (loan.trackingStartYear == null) {
                item.put("trackingStartYear", JSONObject.NULL)
            } else {
                item.put("trackingStartYear", loan.trackingStartYear)
            }

            if (loan.trackingStartMonth == null) {
                item.put("trackingStartMonth", JSONObject.NULL)
            } else {
                item.put("trackingStartMonth", loan.trackingStartMonth)
            }

            if (loan.trackingStartDay == null) {
                item.put("trackingStartDay", JSONObject.NULL)
            } else {
                item.put("trackingStartDay", loan.trackingStartDay)
            }

            val paymentsArray = JSONArray()

            loan.payments.forEach { payment ->
                val paymentObject = JSONObject()

                paymentObject.put("id", payment.id)
                paymentObject.put("installmentNumber", payment.installmentNumber)
                paymentObject.put("amount", payment.amount)
                paymentObject.put("dateLabel", payment.dateLabel)
                paymentObject.put("transactionId", payment.transactionId ?: JSONObject.NULL)

                paymentsArray.put(paymentObject)
            }

            item.put("payments", paymentsArray)

            array.put(item)
        }

        ctx.getSharedPreferences(
            PREFS,
            Context.MODE_PRIVATE
        )
            .edit()
            .putString(KEY, array.toString())
            .apply()
    }

    fun add(loan: LoanEntry) {
        loans.add(0, loan)
        save()
    }

    fun update(loan: LoanEntry) {
        val index = loans.indexOfFirst { it.id == loan.id }

        if (index >= 0) {
            loans[index] = loan
            save()
        }
    }

    fun remove(id: String) {
        loans.removeAll { it.id == id }
        save()
    }

    fun getById(id: String): LoanEntry? {
        return loans.firstOrNull { it.id == id }
    }

    fun addPayment(
        loanId: String,
        payment: LoanPayment
    ): Boolean {
        val loan = getById(loanId) ?: return false

        if (
            loan.payments.any {
                it.installmentNumber == payment.installmentNumber
            }
        ) {
            return false
        }

        if (
            !payment.transactionId.isNullOrBlank() &&
            loan.payments.any {
                it.transactionId == payment.transactionId
            }
        ) {
            return false
        }

        update(
            loan.copy(
                payments = loan.payments + payment
            )
        )

        val updatedLoan = getById(loanId) ?: return false

        return updatedLoan.payments.any {
            it.id == payment.id
        }
    }

    fun removePayment(
        loanId: String,
        paymentId: String
    ) {
        val loan = getById(loanId) ?: return

        update(
            loan.copy(
                payments = loan.payments.filterNot {
                    it.id == paymentId
                }
            )
        )
    }
}
