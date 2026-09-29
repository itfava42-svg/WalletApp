package com.example.walletapp.data

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class TransactionStoreSmsIdempotencyTest {

    @After
    fun tearDown() {
        TransactionStore.transactions.clear()
    }

    @Test
    fun sameSmsSourceIdDoesNotCreateDuplicateTransaction() {
        val first = TransactionStore.addFromBankSms(
            bankName = "بانک ملت",
            amount = 125_000L,
            merchant = "فروشگاه",
            type = TransactionType.EXPENSE,
            smsDate = 1_000L,
            sourceId = "sms-source-1",
        )

        val second = TransactionStore.addFromBankSms(
            bankName = "بانک ملت",
            amount = 125_000L,
            merchant = "فروشگاه",
            type = TransactionType.EXPENSE,
            smsDate = 1_000L,
            sourceId = "sms-source-1",
        )

        assertEquals(1, TransactionStore.transactions.size)
        assertEquals(first.id, second.id)
        assertSame(first, TransactionStore.transactions.first())
    }

    @Test
    fun differentSmsSourceIdsRemainIndependentTransactions() {
        TransactionStore.addFromBankSms(
            bankName = "بانک ملت",
            amount = 125_000L,
            merchant = "فروشگاه",
            type = TransactionType.EXPENSE,
            smsDate = 1_000L,
            sourceId = "sms-source-1",
        )

        TransactionStore.addFromBankSms(
            bankName = "بانک ملت",
            amount = 125_000L,
            merchant = "فروشگاه",
            type = TransactionType.EXPENSE,
            smsDate = 2_000L,
            sourceId = "sms-source-2",
        )

        assertEquals(2, TransactionStore.transactions.size)
    }

    @Test
    fun blankSourceIdDoesNotEnableIdempotency() {
        val first = TransactionStore.addFromBankSms(
            bankName = "بانک ملت",
            amount = 125_000L,
            merchant = "فروشگاه",
            type = TransactionType.EXPENSE,
            sourceId = " ",
        )

        val second = TransactionStore.addFromBankSms(
            bankName = "بانک ملت",
            amount = 125_000L,
            merchant = "فروشگاه",
            type = TransactionType.EXPENSE,
            sourceId = " ",
        )

        assertEquals(2, TransactionStore.transactions.size)
        assertEquals(false, first.id == second.id)
    }
}
