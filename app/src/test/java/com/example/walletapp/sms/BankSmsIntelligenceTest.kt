package com.example.walletapp.sms

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BankSmsIntelligenceTest {

    @Test
    fun recognizesPurchaseAndRequestsReviewWhenEvidenceIsLimited() {
        val result = BankSmsIntelligence.analyze("\\u062e\\u0631\\u06cc\\u062f 200,000 \\u0631\\u06cc\\u0627\\u0644")
        assertEquals(FinancialSmsCategory.PURCHASE, result.category)
        assertTrue(result.confidence in 0.0f..1.0f)
        assertTrue(result.matchedSignals.isNotEmpty())
    }

    @Test
    fun classifiesOtpAsSecurityNotFinancialTransaction() {
        val result = BankSmsIntelligence.analyze("\\u0631\\u0645\\u0632 \\u067e\\u0648\\u06cc\\u0627: 123456")
        assertEquals(FinancialSmsCategory.SECURITY_OR_OTP, result.category)
        assertTrue(result.requiresUserReview)
    }

    @Test
    fun unknownMessageIsNotForcedIntoTransactionCategory() {
        val result = BankSmsIntelligence.analyze("Hello, your service is ready")
        assertEquals(FinancialSmsCategory.UNKNOWN, result.category)
        assertTrue(result.requiresUserReview)
    }

    @Test
    fun conflictingPurchaseAndRefundSignalsRequireReview() {
        val result = BankSmsIntelligence.analyze("\\u062e\\u0631\\u06cc\\u062f \\u0648 \\u0628\\u0627\\u0632\\u06af\\u0634\\u062a \\u0648\\u062c\\u0647 200000")
        assertEquals(FinancialSmsCategory.UNKNOWN, result.category)
        assertTrue(result.requiresUserReview)
    }

    @Test
    fun recognizesInstallmentMessage() {
        val result = BankSmsIntelligence.analyze("\\u067e\\u0631\\u062f\\u0627\\u062e\\u062a \\u0642\\u0633\\u0637 \\u0648\\u0627\\u0645 100000")
        assertEquals(FinancialSmsCategory.LOAN_INSTALLMENT, result.category)
        assertTrue(result.requiresUserReview)
    }
}
