package com.example.walletapp.sms

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BankSmsLibraryTest {

    @Test
    fun registryContainsRequiredProjectSenders() {
        assertTrue(BankSmsLibrary.isKnownSender("100080"))
        assertTrue(BankSmsLibrary.isKnownSender("9999920000"))
        assertTrue(BankSmsLibrary.isKnownSender("7007058"))
        assertTrue(BankSmsLibrary.isKnownSender("4040701701"))
        assertTrue(BankSmsLibrary.isKnownSender("3000941001"))
    }

    @Test
    fun senderTypeControlsTransactionEligibility() {
        assertTrue(BankSmsLibrary.lookup("100080").single().transactionAllowed)
        assertFalse(BankSmsLibrary.lookup("7007583").single().transactionAllowed)
        assertFalse(BankSmsLibrary.lookup("500015741").single().transactionAllowed)
        assertFalse(BankSmsLibrary.lookup("3000940").single().transactionAllowed)
    }

    @Test
    fun senderNormalizationSupportsIranianPrefixes() {
        assertEquals("بانک سامان", BankSmsLibrary.lookup("009820000").single().bankName)
        assertEquals("بانک سامان", BankSmsLibrary.lookup("+9820000").single().bankName)
    }

    @Test
    fun unknownSenderNeverBecomesKnown() {
        assertFalse(BankSmsLibrary.isKnownSender("123456789"))
    }

    @Test
    fun registryHasNoDuplicateNormalizedSenders() {
        val normalized = BankSmsLibrary.senders.map {
            it.sender.trim()
                .removePrefix("+98")
                .removePrefix("0098")
                .removePrefix("98")
                .removePrefix("0")
                .lowercase()
        }
        assertEquals(normalized.size, normalized.toSet().size)
    }
}
