package com.example.walletapp.sms

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class SmsCompletionCoordinatorTest {

    @After
    fun tearDown() {
        SmsCompletionCoordinator.reset()
    }

    @Test
    fun duplicatePendingTransactionIsDeliveredOnlyOnce() {
        assertTrue(SmsCompletionCoordinator.deliver("sms-1"))
        assertTrue(SmsCompletionCoordinator.deliver("sms-1"))
        assertEquals(1, SmsCompletionCoordinator.pendingCount())
        assertEquals("sms-1", SmsCompletionCoordinator.peekPendingTransactionId())
    }

    @Test
    fun pendingTransactionsKeepFifoOrderAndHostReceivesFirst() {
        SmsCompletionCoordinator.deliver("sms-1")
        SmsCompletionCoordinator.deliver("sms-2")

        val latch = CountDownLatch(1)
        var received: String? = null

        SmsCompletionCoordinator.registerHost { id ->
            received = id
            latch.countDown()
        }

        assertTrue(latch.await(2, TimeUnit.SECONDS))
        assertEquals("sms-1", received)
        assertEquals(1, SmsCompletionCoordinator.pendingCount())
        assertEquals("sms-2", SmsCompletionCoordinator.peekPendingTransactionId())
    }

    @Test
    fun registeredHostReceivesEachNewTransaction() {
        val latch = CountDownLatch(2)
        val received = mutableListOf<String>()

        SmsCompletionCoordinator.registerHost { id ->
            synchronized(received) {
                received.add(id)
            }
            latch.countDown()
        }

        assertTrue(SmsCompletionCoordinator.deliver("sms-1"))
        assertTrue(SmsCompletionCoordinator.deliver("sms-2"))

        assertTrue(latch.await(2, TimeUnit.SECONDS))
        assertEquals(listOf("sms-1", "sms-2"), synchronized(received) { received.toList() })
        assertEquals(0, SmsCompletionCoordinator.pendingCount())
    }

    @Test
    fun blankTransactionIdIsRejected() {
        assertTrue(!SmsCompletionCoordinator.deliver(""))
        assertEquals(0, SmsCompletionCoordinator.pendingCount())
    }
}
