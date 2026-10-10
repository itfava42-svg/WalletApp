package com.example.walletapp.sms

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SmsLearningStorePrivacyTest {

    @Test
    fun storedLearningPatternDoesNotRetainCardAccountOrAmountDigits() {
        val original = "خرید کارت 6037-9912-1234-5678 مبلغ ۱۲۳٬۴۵۶ ریال حساب 0045 6789"

        val stored = SmsLearningStore.normalizePatternForStorage(original)

        assertFalse(stored.contains("6037"))
        assertFalse(stored.contains("9912"))
        assertFalse(stored.contains("1234"))
        assertFalse(stored.contains("5678"))
        assertFalse(stored.contains("۱۲۳"))
        assertFalse(stored.contains("۴۵۶"))
        assertFalse(stored.contains("0045"))
        assertFalse(stored.contains("6789"))
        assertTrue(stored.contains("خرید"))
        assertTrue(stored.contains("مبلغ"))
        assertTrue(stored.contains("<NUM>"))
    }

    @Test
    fun persistedSensitiveIdentifiersKeepOnlyLastFourDigits() {
        assertTrue(SmsLearningStore.lastFourDigitsForStorage("شماره حساب ۱۲۳۴۵۶۷۸۹۰") == "7890")
        assertTrue(SmsLearningStore.lastFourDigitsForStorage("6037-9912-1234-5678") == "5678")
        assertTrue(SmsLearningStore.lastFourDigitsForStorage("**** ١٢٣٤") == "1234")
        assertTrue(SmsLearningStore.lastFourDigitsForStorage("123") == null)
        assertTrue(SmsLearningStore.lastFourDigitsForStorage(null) == null)
    }

    @Test
    fun storedLearningPatternHandlesArabicAndPersianDigits() {
        val stored = SmsLearningStore.normalizePatternForStorage("انتقال ١٢٣٤٥٦ واریز ۱۲۳۴۵۶")

        assertFalse(stored.contains("١٢٣٤٥٦"))
        assertFalse(stored.contains("۱۲۳۴۵۶"))
        assertTrue(stored.contains("انتقال"))
        assertTrue(stored.contains("واریز"))
    }
}
