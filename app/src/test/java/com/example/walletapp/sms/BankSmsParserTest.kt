package com.example.walletapp.sms

import com.example.walletapp.data.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BankSmsParserTest {

    @Test
    fun parsesPersianExpenseWithBankAndAmount() {
        val result = BankSmsParser.parse(
            sender = "9830000",
            body = "بانک ملت برداشت مبلغ 200,000 ریال از حساب"
        )

        assertEquals("بانک ملت", result?.bankName)
        assertEquals(200_000L, result?.amount)
        assertEquals(TransactionType.EXPENSE, result?.type)
        assertFalse(result?.isTransfer ?: true)
    }

    @Test
    fun parsesPersianIncomeWithCurrencyAndIgnoresBalanceForAmount() {
        val result = BankSmsParser.parse(
            sender = "9830000",
            body = "بانک ملت واریز مبلغ 200,000 ریال، موجودی 5,000,000"
        )

        assertEquals(200_000L, result?.amount)
        assertEquals(TransactionType.INCOME, result?.type)
        assertEquals(5_000_000L, result?.balance)
    }

    @Test
    fun extractsCardLast4WithoutUsingCardNumberAsAmount() {
        val result = BankSmsParser.parse(
            sender = "9830000",
            body = "بانک ملت خرید 200,000 ریال کارت 6037-9912-1234-5678"
        )

        assertEquals(200_000L, result?.amount)
        assertEquals("6037991212345678", result?.cardNumber)
        assertEquals("5678", result?.cardLast4)
        assertEquals(TransactionType.EXPENSE, result?.type)
    }

    @Test
    fun unknownBankWithExplicitSignedAmountIsStillRecognized() {
        val result = BankSmsParser.parse(
            sender = "unknown",
            body = "تراکنش +200,000 ریال"
        )

        assertEquals(BankSmsParser.UNKNOWN_BANK, result?.bankName)
        assertEquals(200_000L, result?.amount)
        assertEquals(TransactionType.INCOME, result?.type)
    }

    @Test
    fun transferIsDetectedAndKeepsTransferFlag() {
        val result = BankSmsParser.parse(
            sender = "unknown",
            body = "انتقال وجه مبلغ 200,000 ریال"
        )

        assertTrue(result?.isTransfer ?: false)
        assertEquals(200_000L, result?.amount)
        assertEquals(TransactionType.EXPENSE, result?.type)
    }

    @Test
    fun nonTransactionalSecuritySmsIsRejected() {
        val result = BankSmsParser.parse(
            sender = "unknown",
            body = "رمز پویا: 123456"
        )

        assertNull(result)
    }
}
