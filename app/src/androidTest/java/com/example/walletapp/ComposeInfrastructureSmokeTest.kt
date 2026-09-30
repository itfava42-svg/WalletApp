package com.example.walletapp

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class ComposeInfrastructureSmokeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun composeTestInfrastructureStarts() {
        composeTestRule.setContent {
        }
    }
}