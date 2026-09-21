package com.example.walletapp.data

/**
 * Domain boundary for loan mutations.
 *
 * LoanStore remains the authoritative owner of loan state,
 * persistence, and loan invariants.
 */
object LoanManager {

    fun add(loan: LoanEntry) {
        LoanStore.add(loan)
    }

    fun update(loan: LoanEntry) {
        LoanStore.update(loan)
    }

    fun remove(id: String) {
        LoanStore.remove(id)
    }
}
