package com.example.walletapp.data

/**
 * Domain boundary for check mutations.
 *
 * CheckStore remains the authoritative owner of check state,
 * persistence, and check invariants.
 */
object CheckManager {

    fun add(check: CheckEntry) {
        CheckStore.add(check)
    }

    fun update(check: CheckEntry) {
        CheckStore.update(check)
    }

    fun remove(id: String) {
        CheckStore.remove(id)
    }
}
