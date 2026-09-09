package com.example.walletapp.loan

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Persists the payment state of loan installments.
 *
 * LoanScheduleGenerator creates the base schedule from Loan.
 * This store keeps only mutable payment information:
 * - isPaid
 * - paidDate
 * - transactionId
 *
 * Installments are identified by:
 * loanId + installment number
 *
 * This is stable even though LoanInstallment.id is generated
 * with a random UUID by LoanScheduleGenerator.
 */
object LoanInstallmentStore {

    private const val FILE_NAME = "loan_installments.json"

    private var appContext: Context? = null
    private var loadedFromDisk = false

    /**
     * Stable key for an installment.
     */
    private fun keyOf(installment: LoanInstallment): String {
        return "${installment.loanId}:${installment.number}"
    }

    private val installments =
        mutableMapOf<String, LoanInstallment>()

    fun attachContext(context: Context) {

        if (appContext == null) {
            appContext = context.applicationContext
        }

        if (!loadedFromDisk) {

            loadedFromDisk = true

            appContext?.let { load(it) }
        }
    }

    private fun persist() {

        val context = appContext ?: return

        val array = JSONArray()

        installments.values.forEach { installment ->

            val obj = JSONObject()

            obj.put(
                "id",
                installment.id
            )

            obj.put(
                "loanId",
                installment.loanId
            )

            obj.put(
                "number",
                installment.number
            )

            obj.put(
                "dueDate",
                installment.dueDate
            )

            obj.put(
                "amount",
                installment.amount
            )

            obj.put(
                "isPaid",
                installment.isPaid
            )

            if (installment.paidDate != null) {

                obj.put(
                    "paidDate",
                    installment.paidDate
                )
            }

            if (installment.transactionId != null) {

                obj.put(
                    "transactionId",
                    installment.transactionId
                )
            }

            array.put(obj)
        }

        context.openFileOutput(
            FILE_NAME,
            Context.MODE_PRIVATE
        ).use { output ->

            output.write(
                array.toString().toByteArray()
            )
        }
    }

    private fun load(context: Context) {

        installments.clear()

        try {

            val text =
                context
                    .openFileInput(FILE_NAME)
                    .bufferedReader()
                    .use { it.readText() }

            val array =
                JSONArray(text)

            for (i in 0 until array.length()) {

                val obj =
                    array.getJSONObject(i)

                val installment =
                    LoanInstallment(

                        id =
                            obj.getString("id"),

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
                            obj.optString("paidDate")
                                .takeIf {
                                    it.isNotBlank()
                                },

                        transactionId =
                            obj.optString("transactionId")
                                .takeIf {
                                    it.isNotBlank()
                                }
                    )

                installments[
                    keyOf(installment)
                ] = installment
            }

        } catch (_: Exception) {
            // File may not exist on first run.
        }
    }

    /**
     * Returns the persisted version of the installment,
     * or the generated installment when no persisted state exists.
     */
    fun get(
        installment: LoanInstallment
    ): LoanInstallment {

        return installments[
            keyOf(installment)
        ] ?: installment
    }

    /**
     * Returns the complete schedule of a loan with
     * persisted payment states applied.
     */
    fun getSchedule(
        loan: Loan
    ): List<LoanInstallment> {

        val generated =
            LoanScheduleGenerator.generate(loan)

        return generated.map { installment ->

            get(installment)
        }
    }

    /**
     * Marks an installment as paid and associates
     * the existing transaction with it.
     *
     * This method does NOT create a new transaction.
     */
    fun markPaid(
        installment: LoanInstallment,
        transactionId: String,
        paidDate: String = "امروز",
    ): LoanInstallment {

        val updated =
            installment.copy(

                isPaid = true,

                paidDate = paidDate,

                transactionId = transactionId
            )

        installments[
            keyOf(updated)
        ] = updated

        persist()

        return updated
    }

    /**
     * Marks an installment as unpaid.
     */
    fun markUnpaid(
        installment: LoanInstallment
    ): LoanInstallment {

        val updated =
            installment.copy(

                isPaid = false,

                paidDate = null,

                transactionId = null
            )

        installments[
            keyOf(updated)
        ] = updated

        persist()

        return updated
    }

    /**
     * Finds an installment by its actual UUID.
     *
     * Kept for compatibility with existing code.
     */
    fun findById(
        id: String
    ): LoanInstallment? {

        return installments.values.firstOrNull {

            it.id == id
        }
    }

    /**
     * Finds the installment associated with a transaction.
     */
    fun findByTransactionId(
        transactionId: String
    ): LoanInstallment? {

        return installments.values.firstOrNull {

            it.transactionId == transactionId
        }
    }

    /**
     * Returns all unpaid installments of one loan.
     */
    fun getUnpaidInstallments(
        loan: Loan
    ): List<LoanInstallment> {

        return getSchedule(loan)
            .filterNot {
                it.isPaid
            }
    }

    /**
     * Returns all unpaid installments of multiple loans.
     */
    fun getUnpaidInstallments(
        loans: List<Loan>
    ): List<LoanInstallment> {

        return loans.flatMap { loan ->

            getUnpaidInstallments(loan)
        }
    }

    /**
     * Clears all persisted installment states.
     */
    fun clear() {

        installments.clear()

        persist()
    }
}
