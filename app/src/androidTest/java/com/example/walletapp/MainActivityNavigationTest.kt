package com.example.walletapp

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityNavigationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun mainActivityStartsOnDashboard() {
        composeTestRule
            .onNodeWithContentDescription("خانه")
            .assertExists()
    }

    @Test
    fun bottomNavigationOpensTransactions() {
        composeTestRule
            .onNodeWithContentDescription("تراکنش‌ها")
            .performClick()

        composeTestRule
            .onNodeWithText("تراکنش‌ها")
            .assertExists()
    }

    @Test
    fun bottomNavigationOpensAccounts() {
        composeTestRule
            .onNodeWithContentDescription("حساب‌ها")
            .performClick()

        composeTestRule
            .onNodeWithText("حساب‌ها")
            .assertExists()
    }

    @Test
    fun bottomNavigationOpensTools() {
        composeTestRule
            .onNodeWithContentDescription("ابزارها")
            .performClick()

        composeTestRule
            .onNodeWithText("ابزارها")
            .assertExists()
    }

    @Test
    fun addTransactionNavigationOpensAddTransaction() {
        composeTestRule
            .onNodeWithContentDescription("ثبت تراکنش")
            .performClick()

        composeTestRule
            .onNodeWithText("ثبت تراکنش جدید")
            .assertExists()
    }

    @Test
    fun transactionsBackReturnsToDashboard() {
        composeTestRule
            .onNodeWithContentDescription("تراکنش‌ها")
            .performClick()

        composeTestRule
            .onNodeWithText("تراکنش‌ها")
            .assertExists()

        composeTestRule.activity
            .onBackPressedDispatcher
            .onBackPressed()

        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithContentDescription("خانه")
            .assertExists()
    }

    @Test
    fun addTransactionBackReturnsToDashboard() {
        composeTestRule
            .onNodeWithContentDescription("ثبت تراکنش")
            .performClick()

        composeTestRule
            .onNodeWithText("ثبت تراکنش جدید")
            .assertExists()

        composeTestRule.activity
            .onBackPressedDispatcher
            .onBackPressed()

        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithContentDescription("خانه")
            .assertExists()
    }
}