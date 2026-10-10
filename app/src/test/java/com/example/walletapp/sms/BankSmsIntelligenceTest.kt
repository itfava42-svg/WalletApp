package com.example.walletapp.sms

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BankSmsIntelligenceTest {

    @Test
    fun recognizesPurchaseAndRequestsReviewWhenEvidenceIsLimited() {
        val result = BankSmsIntelligence.analyze("\u062e\u0631\u06cc\u062f 200,000 \u0631\u06cc\u0627\u0644")
        assertEquals(FinancialSmsCategory.PURCHASE, result.category)
        assertTrue(result.confidence in 0.0f..1.0f)
        assertTrue(result.matchedSignals.isNotEmpty())
        assertTrue(result.requiresUserReview)
    }

    @Test
    fun classifiesOtpAsSecurityNotFinancialTransaction() {
        val result = BankSmsIntelligence.analyze("\u0631\u0645\u0632 \u067e\u0648\u06cc\u0627: 123456")
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
        val result = BankSmsIntelligence.analyze("\u062e\u0631\u06cc\u062f \u0648 \u0628\u0627\u0632\u06af\u0634\u062a \u0648\u062c\u0647 200000")
        assertEquals(FinancialSmsCategory.UNKNOWN, result.category)
        assertTrue(result.requiresUserReview)
    }

    @Test
    fun recognizesInstallmentMessageAndKeepsItReviewable() {
        val result = BankSmsIntelligence.analyze("\u067e\u0631\u062f\u0627\u062e\u062a \u0642\u0633\u0637 \u0648\u0627\u0645 100000")
        assertEquals(FinancialSmsCategory.LOAN_INSTALLMENT, result.category)
        assertTrue(result.requiresUserReview)
    }

    @Test
    fun recognizesTransferWithoutTreatingItAsIncomeOrExpenseIntent() {
        val result = BankSmsIntelligence.analyze("\u0627\u0646\u062a\u0642\u0627\u0644 \u0648\u062c\u0647 250000")
        assertEquals(FinancialSmsCategory.TRANSFER, result.category)
        assertTrue(result.matchedSignals.isNotEmpty())
    }

    @Test
    fun recognizesDepositAndRefundSeparately() {
        assertEquals(
            FinancialSmsCategory.DEPOSIT,
            BankSmsIntelligence.analyze("\u0648\u0627\u0631\u06cc\u0632 300000").category,
        )
        assertEquals(
            FinancialSmsCategory.REFUND,
            BankSmsIntelligence.analyze("\u0628\u0627\u0632\u06af\u0634\u062a \u0648\u062c\u0647 300000").category,
        )
    }

    @Test
    fun recognizesChequeAndBalanceInquiry() {
        assertEquals(
            FinancialSmsCategory.CHEQUE,
            BankSmsIntelligence.analyze("\u0686\u06a9 \u0635\u06cc\u0627\u062f 123456").category,
        )
        assertEquals(
            FinancialSmsCategory.BALANCE_INQUIRY,
            BankSmsIntelligence.analyze("\u0645\u0627\u0646\u062f\u0647 \u062d\u0633\u0627\u0628 900000").category,
        )
    }

    @Test
    fun normalizesArabicYehKafAndZeroWidthNonJoiner() {
        val result = BankSmsIntelligence.analyze("\u062e\u0631\u064a\u062f\u200c 200000")
        assertEquals(FinancialSmsCategory.PURCHASE, result.category)
    }

    @Test
    fun conflictingIntentNeverReceivesHighConfidence() {
        val result = BankSmsIntelligence.analyze("\u062e\u0631\u06cc\u062f \u0648\u0627\u0631\u06cc\u0632 200000")
        assertEquals(FinancialSmsCategory.UNKNOWN, result.category)
        assertTrue(result.confidence < 0.80f)
        assertTrue(result.requiresUserReview)
    }
}
