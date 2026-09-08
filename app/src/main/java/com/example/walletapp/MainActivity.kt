package com.example.walletapp

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.walletapp.data.TransactionStore
import com.example.walletapp.data.LoanStore
import com.example.walletapp.data.CheckStore
import com.example.walletapp.ui.screens.AddTransactionScreen
import com.example.walletapp.ui.screens.BudgetScreen
import com.example.walletapp.ui.screens.DashboardScreen
import com.example.walletapp.ui.screens.SettingsListScreen
import com.example.walletapp.ui.screens.SmsLearningScreen
import com.example.walletapp.ui.screens.ThemeBuilderScreen
import com.example.walletapp.ui.screens.ReminderScreen
import com.example.walletapp.ui.screens.AddReminderScreen
import com.example.walletapp.ui.screens.LoanManagementScreen
import com.example.walletapp.ui.screens.CheckManagementScreen
import com.example.walletapp.data.Reminder
import com.example.walletapp.ui.screens.ToolsScreen
import com.example.walletapp.ui.screens.OperationsScreen
import com.example.walletapp.ui.screens.AccountsScreen
import com.example.walletapp.ui.theme.WalletAppTheme

private sealed class AppScreen {
    data object Dashboard : AppScreen()
    data object Accounts : AppScreen()
    data object AddTransaction : AppScreen()
    data object Tools : AppScreen()
        data object Operations : AppScreen()
data object Budget : AppScreen()
    data object SettingsList : AppScreen()
    data object SmsLearning : AppScreen()
    data object ThemeBuilder : AppScreen()
    data object Reminder : AppScreen()
    data object AddReminder : AppScreen()
    data object LoanManagement : AppScreen()
    data object CheckManagement : AppScreen()
}

class MainActivity : ComponentActivity() {

    private val prefsName = "wallet_app_prefs"
    private val keyPermissionsRequested = "sms_overlay_permissions_requested"

    private val requestSmsPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Regardless of the SMS permission result, also ask for the overlay
        // ("display over other apps") permission أ¢â‚¬â€‌ it can't be requested
        // through a normal runtime dialog, only via a Settings screen.
        maybeRequestOverlayPermission()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        TransactionStore.attachContext(applicationContext)
        LoanStore.attachContext(applicationContext)
        CheckStore.attachContext(applicationContext)

        setContent {
            // App content is Persian, so force RTL layout direction regardless
            // of device locale.
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                WalletAppTheme {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Dashboard) }
                        var selectedReminder by remember { mutableStateOf<Reminder?>(null) }
                        var selectedNavIndex by remember { mutableStateOf(0) }

                        fun onNavSelect(index: Int) {
                            selectedNavIndex = index

                            currentScreen = when (index) {
                                0 -> AppScreen.Dashboard
                                1 -> AppScreen.Accounts
                                3 -> AppScreen.Tools
                                4 -> AppScreen.Budget
                                else -> AppScreen.Dashboard
                            }
                        }

                        when (currentScreen) {
                            is AppScreen.Accounts -> AccountsScreen(
                                selectedNavIndex = selectedNavIndex,
                                onNavSelect = ::onNavSelect,
                            )

                            is AppScreen.Dashboard -> DashboardScreen(
                                onAddTransactionClick = { currentScreen = AppScreen.AddTransaction },
                                onOpenBudgetClick = {
                                    selectedNavIndex = 4
                                    currentScreen = AppScreen.Budget
                                },
                                selectedNavIndex = selectedNavIndex,
                                onNavSelect = ::onNavSelect,
                            )
                            is AppScreen.AddTransaction -> AddTransactionScreen(
                                onBack = { currentScreen = AppScreen.Dashboard },
                                onSave = { transaction ->
                                    TransactionStore.addManual(transaction)
                                    currentScreen = AppScreen.Dashboard
                                },
                            )
                            is AppScreen.Tools -> ToolsScreen(
                                selectedNavIndex = selectedNavIndex,
                                onNavSelect = ::onNavSelect,
                                onOpenSettingsList = { currentScreen = AppScreen.SettingsList },
                                onOpenReminder = { currentScreen = AppScreen.Reminder },
                                onOpenLoanManagement = {
                                    currentScreen = AppScreen.LoanManagement
                                },
                                onOpenCheckManagement = {
                                    currentScreen = AppScreen.CheckManagement
                                },
            onOpenOperations = { currentScreen = AppScreen.Operations },
                            )
                            is AppScreen.Budget -> BudgetScreen(
                                selectedNavIndex = selectedNavIndex,
                                onNavSelect = ::onNavSelect,
                            )
                            is AppScreen.Reminder -> ReminderScreen(
                                onBack = { currentScreen = AppScreen.Tools },
                                onAddReminder = {
                                    selectedReminder = null
                                    currentScreen = AppScreen.AddReminder
                                },
                                onEditReminder = { reminder ->
                                    selectedReminder = reminder
                                    currentScreen = AppScreen.AddReminder
                                },
                            )

                            is AppScreen.AddReminder -> {
                                AddReminderScreen(
                                    existingReminder = selectedReminder,
                                    onBack = {
                                        selectedReminder = null
                                        currentScreen = AppScreen.Reminder
                                    },
                                    onSaved = {
                                        selectedReminder = null
                                        currentScreen = AppScreen.Reminder
                                    },
                                )
                            }

        is AppScreen.Operations -> OperationsScreen(
            onBack = { currentScreen = AppScreen.Tools },
            onOpenCheckManagement = {
                currentScreen = AppScreen.CheckManagement
            },
            onOpenLoanManagement = {
                currentScreen = AppScreen.LoanManagement
            },
        )
                            is AppScreen.SettingsList -> SettingsListScreen(
                                onBack = { currentScreen = AppScreen.Tools },
                                onOpenThemeBuilder = { currentScreen = AppScreen.ThemeBuilder },
        onOpenSmsLearning = {
                                    currentScreen = AppScreen.SmsLearning
                                },
                            )

                            is AppScreen.SmsLearning -> SmsLearningScreen(
                                onBack = {
                                    currentScreen = AppScreen.SettingsList
                                },
                            )

                            is AppScreen.ThemeBuilder -> ThemeBuilderScreen(
                                onBack = {
                                    currentScreen = AppScreen.SettingsList
                                },
                            )

                            is AppScreen.LoanManagement -> LoanManagementScreen(
                                onBack = { currentScreen = AppScreen.Tools },
                            )

                            is AppScreen.CheckManagement -> CheckManagementScreen(
                                onBack = { currentScreen = AppScreen.Tools },
                            )
                        }
                    }
                }
            }
        }

        requestFirstRunPermissionsIfNeeded()
    }

    private fun requestFirstRunPermissionsIfNeeded() {
        val prefs = getSharedPreferences(prefsName, MODE_PRIVATE)
        val alreadyRequested = prefs.getBoolean(keyPermissionsRequested, false)
        if (alreadyRequested) return

        prefs.edit().putBoolean(keyPermissionsRequested, true).apply()

        val permissionsToRequest = mutableListOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        requestSmsPermissions.launch(permissionsToRequest.toTypedArray())
    }

    private fun maybeRequestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName"),
            )
            startActivity(intent)
        }
    }
}











