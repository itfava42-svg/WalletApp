package com.example.walletapp.loan

import android.content.Context

object LoanStore {

    private val loans = mutableListOf<Loan>()

    private var appContext: Context? = null
    private var loadedFromDisk = false

    fun attachContext(context: Context) {

        if (appContext == null) {
            appContext = context.applicationContext
        }

        if (!loadedFromDisk) {

            loadedFromDisk = true

            appContext?.let { ctx ->

                val loaded =
                    LoanPersistence.load(ctx)

                if (loaded.isNotEmpty()) {

                    loans.clear()
                    loans.addAll(loaded)

                }
            }
        }
    }

    private fun persist() {

        appContext?.let { ctx ->

            LoanPersistence.save(
                ctx,
                loans
            )
        }
    }

    fun addLoan(loan: Loan) {

        loans.add(loan)

        persist()
    }

    fun getLoans(): List<Loan> {

        return loans.toList()
    }

    fun getActiveLoans(): List<Loan> {

        return loans.filter {
            it.isActive
        }
    }

    fun findById(id: String): Loan? {

        return loans.firstOrNull {
            it.id == id
        }
    }

    fun updateLoan(loan: Loan) {

        val index =
            loans.indexOfFirst {
                it.id == loan.id
            }

        if (index >= 0) {

            loans[index] =
                loan.copy(
                    updatedAt =
                        System.currentTimeMillis()
                )

            persist()
        }
    }

    fun deleteLoan(id: String) {

        loans.removeAll {
            it.id == id
        }

        persist()
    }

    fun clear() {

        loans.clear()

        persist()
    }
}
