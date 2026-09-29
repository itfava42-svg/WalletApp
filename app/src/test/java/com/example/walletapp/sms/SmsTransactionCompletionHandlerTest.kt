package com.example.walletapp.sms

import com.example.walletapp.data.Transaction
import com.example.walletapp.data.TransactionKind
import com.example.walletapp.data.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SmsTransactionCompletionHandlerTest {

    private fun request(
        transaction: Transaction,
        selectedType: TransactionType = transaction.type,
        isTransfer: Boolean = false,
        destinationAccountId: String? = null,
        selectedCategory: String? = null,
    ): SmsTransactionCompletionHandler.CompletionRequest {
        return SmsTransactionCompletionHandler.CompletionRequest(
            transaction = transaction,
            selectedType = selectedType,
            isTransfer = isTransfer,
            destinationAccountId = destinationAccountId,
            sender = "1000",
            rawBody = "پیام بانکی",
            selectedBank = "بانک نمونه",
            selectedCategory = selectedCategory,
            tags = emptyList(),
            description = "",
            receiptUri = null,
            selectedAccountId = "source",
            smsAccountNumber = null,
            smsCardLast4 = null,
            learnBank = false,
            selectedLoan = null,
            selectedInstallmentNumber = null,
        )
    }

    @Test
    fun normalCompletionPreservesAccountAndCategory() {
        val transaction = Transaction(
            id = "sms-1",
            title = "بانک نمونه",
            subtitle = "تراکنش بانکی",
            amount = 250_000L,
            type = TransactionType.EXPENSE,
            iconKey = "بانک",
            requiresCompletion = true,
        )

        val updated = SmsTransactionCompletionHandler.buildUpdatedTransaction(
            request = request(
                transaction = transaction,
                selectedCategory = "خوراک",
            ),
            resolvedAccountId = "source",
        )

        assertEquals(TransactionType.EXPENSE, updated.type)
        assertEquals("source", updated.accountId)
        assertEquals("خوراک", updated.category)
        assertEquals(TransactionKind.NORMAL, updated.kind)
        assertNull(updated.sourceAccountId)
        assertNull(updated.destinationAccountId)
        assertEquals(false, updated.requiresCompletion)
    }

    @Test
    fun transferCompletionCreatesTransferWithoutCategory() {
        val transaction = Transaction(
            id = "sms-2",
            title = "بانک نمونه",
            subtitle = "تراکنش بانکی",
            amount = 500_000L,
            type = TransactionType.EXPENSE,
            iconKey = "بانک",
            category = "خوراک",
            requiresCompletion = true,
        )

        val updated = SmsTransactionCompletionHandler.buildUpdatedTransaction(
            request = request(
                transaction = transaction,
                isTransfer = true,
                destinationAccountId = "destination",
                selectedCategory = "خوراک",
            ),
            resolvedAccountId = "source",
        )

        assertEquals(TransactionKind.TRANSFER, updated.kind)
        assertNull(updated.accountId)
        assertNull(updated.category)
        assertEquals("source", updated.sourceAccountId)
        assertEquals("destination", updated.destinationAccountId)
        assertEquals("انتقال بین حساب‌ها", updated.title)
        assertEquals(false, updated.requiresCompletion)
    }

    @Test
    fun transferUsesResolvedSourceWhenExplicitAccountIsMissing() {
        val transaction = Transaction(
            id = "sms-3",
            title = "بانک نمونه",
            subtitle = "تراکنش بانکی",
            amount = 100_000L,
            type = TransactionType.EXPENSE,
            iconKey = "بانک",
            requiresCompletion = true,
        )

        val base = request(
            transaction = transaction,
            isTransfer = true,
            destinationAccountId = "destination",
        )

        val updated = SmsTransactionCompletionHandler.buildUpdatedTransaction(
            request = base.copy(selectedAccountId = null),
            resolvedAccountId = "resolved-source",
        )

        assertEquals("resolved-source", updated.sourceAccountId)
        assertEquals("destination", updated.destinationAccountId)
        assertNull(updated.category)
        assertEquals(TransactionKind.TRANSFER, updated.kind)
    }
}
