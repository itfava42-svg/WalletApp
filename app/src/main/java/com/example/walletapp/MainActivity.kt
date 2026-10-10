package com.example.walletapp


import android.util.Log
import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
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
import com.example.walletapp.data.TransactionSyncViewModel
import com.example.walletapp.data.LoanStore
import com.example.walletapp.data.LoanLegacyMigration
import com.example.walletapp.data.CheckStore
import com.example.walletapp.ui.screens.AddTransactionScreen
import com.example.walletapp.ui.screens.TransactionSyncScreen
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
import com.example.walletapp.data.ReminderStore
import com.example.walletapp.reminders.ReminderScheduler
import com.example.walletapp.ui.screens.ToolsScreen
import com.example.walletapp.ui.screens.ReportsScreen

import com.example.walletapp.ui.screens.OperationsScreen
import com.example.walletapp.ui.screens.TransferScreen
import com.example.walletapp.ui.screens.TransactionsScreen
import com.example.walletapp.ui.screens.AccountsScreen
import com.example.walletapp.ui.components.AppScreenContainer
import com.example.walletapp.ui.screens.AddBankAccountScreen
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import com.example.walletapp.data.AccountManager
import com.example.walletapp.data.AccountStore
import com.example.walletapp.data.AccountType
import com.example.walletapp.sms.SmsCompletionCoordinator
import com.example.walletapp.sms.SmsIgnoreManager
import com.example.walletapp.sms.SmsLearningManager
import com.example.walletapp.sms.SmsTransactionCompletionHandler
import com.example.walletapp.sms.SmsTransactionCompletionSheetHost
import com.example.walletapp.sms.SmsTransactionMetadataManager
import com.example.walletapp.sms.SmsTransactionQueue
import com.example.walletapp.ui.theme.WalletAppTheme

private sealed class AppScreen {
    data object Dashboard : AppScreen()
    data object Transactions : AppScreen()
    data object Accounts : AppScreen()
    data object AddTransaction : AppScreen()
    data object Tools : AppScreen()
        data object Operations : AppScreen()
    data object Reports : AppScreen()

    data object Transfer : AppScreen()
data object Budget : AppScreen()
    data object SettingsList : AppScreen()
    data object SmsLearning : AppScreen()
    data object ThemeBuilder : AppScreen()
    data object Reminder : AppScreen()
    data object AddReminder : AppScreen()
    data object LoanManagement : AppScreen()
    data object TransactionSync : AppScreen()
    data object CheckManagement : AppScreen()
}

class MainActivity : ComponentActivity() {

    /**
     * CI-195 — Restore active reminder alarms when the app returns to foreground.
     *
     * Android does not broadcast when exact-alarm access is revoked, and that
     * revocation cancels existing exact alarms. Re-running the scheduler on
     * resume restores inexact alarms when access is unavailable, or exact
     * alarms when the user has granted access again.
     */
    override fun onResume() {
        super.onResume()
        ReminderStore.attachContext(applicationContext)
        ReminderStore.reminders
            .filter { it.isActive && !it.isCompleted }
            .forEach { reminder ->
                ReminderScheduler.schedule(applicationContext, reminder)
            }
    }

    companion object {
        const val EXTRA_SMS_TRANSACTION_ID = "sms_transaction_id"
    }

    private val smsOpenTransactionId = mutableStateOf<String?>(null)

    private val prefsName = "wallet_app_prefs"
    private val keyPermissionsRequested = "sms_overlay_permissions_requested"

    private val requestSmsPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Regardless of the SMS permission result, also ask for the overlay
            // ("display over other apps") permission — it can't be requested
        // through a normal runtime dialog, only via a Settings screen.
        maybeRequestOverlayPermission()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        smsOpenTransactionId.value =
            intent.getStringExtra(EXTRA_SMS_TRANSACTION_ID)

        TransactionStore.attachContext(applicationContext)

// ============================================================
// ============================================================
        LoanStore.attachContext(applicationContext)
        LoanLegacyMigration.migrate(applicationContext)
        CheckStore.attachContext(applicationContext)
        AccountStore.attachContext(applicationContext)
        SmsTransactionMetadataManager.attachContext(applicationContext)
        SmsLearningManager.attachContext(applicationContext)
        SmsIgnoreManager.attachContext(applicationContext)

        setContent {
            // App content is Persian, so force RTL layout direction regardless
            // of device locale.
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                WalletAppTheme {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Dashboard) }
    var showAddBankAccountScreen by remember { mutableStateOf(false) }
                        var selectedReminder by remember { mutableStateOf<Reminder?>(null) }
                        var selectedNavIndex by remember { mutableStateOf(0) }

                        var smsSheetTransaction by remember {
                            mutableStateOf<com.example.walletapp.data.Transaction?>(null)
                        }

                        var selectedReceiptUri by remember {
                            mutableStateOf<Uri?>(null)
                        }

                        var pendingCameraUri by remember {
                            mutableStateOf<Uri?>(null)
                        }

                        val context = androidx.compose.ui.platform.LocalContext.current

                        LaunchedEffect(smsOpenTransactionId.value) {
                            val transactionId = smsOpenTransactionId.value
                                ?.takeIf { it.isNotBlank() }

                            if (transactionId != null) {
                                val transaction = TransactionStore.transactions.firstOrNull {
                                    it.id == transactionId && it.requiresCompletion
                                }

                                if (transaction != null) {
                                    smsSheetTransaction = transaction
                                    selectedReceiptUri = transaction.receiptUri
                                        ?.takeIf { it.isNotBlank() }
                                        ?.let(Uri::parse)
                                }

                                smsOpenTransactionId.value = null
                            }
                        }

                        val receiptPicker =
                            rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.OpenDocument()
                            ) { uri ->
                                if (uri != null) {
                                    runCatching {
                                        context.contentResolver.takePersistableUriPermission(
                                            uri,
                                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        )
                                    }

                                    selectedReceiptUri = uri
                                }
                            }

                        val cameraLauncher =
                            rememberLauncherForActivityResult(
                                ActivityResultContracts.TakePicture()
                            ) { success ->
                                val uri = pendingCameraUri

                                if (success && uri != null) {
                                    selectedReceiptUri = uri
                                }
                            }

                        val cameraPermissionLauncher =
                            rememberLauncherForActivityResult(
                                ActivityResultContracts.RequestPermission()
                            ) { granted ->
                                if (granted) {
                                    val directory =
                                        File(context.filesDir, "receipts").apply {
                                            mkdirs()
                                        }

                                    val file =
                                        File.createTempFile(
                                            "receipt_camera_",
                                            ".jpg",
                                            directory,
                                        )

                                    val uri =
                                        FileProvider.getUriForFile(
                                            context,
                                            ".fileprovider",
                                            file,
                                        )

                                    pendingCameraUri = uri
                                    cameraLauncher.launch(uri)
                                }
                            }
                        DisposableEffect(Unit) {
                            val pendingCoordinatorId = SmsCompletionCoordinator.registerHost { transactionId ->
                                android.util.Log.d(
                                    "SMS_SHEET_DIAG",
                                    "HOST_CALLBACK transactionId=$transactionId"
                                )
                                val transaction =
                                    TransactionStore.transactions
                                        .firstOrNull {
                                            it.id == transactionId
                                        }
                                if (
                                    transaction != null &&
                                    transaction.requiresCompletion
                                ) {
                                    smsSheetTransaction = transaction
                                    android.util.Log.d(
                                        "SMS_RECOMPOSE_DIAG",
                                        "STATE_ASSIGNED transactionId=${transaction.id} thread=${Thread.currentThread().name}"
                                    )
                                    selectedReceiptUri =
                                        transaction.receiptUri
                                            ?.takeIf { it.isNotBlank() }
                                            ?.let(Uri::parse)
                                }
                            }
                            // Recover durable incomplete SMS transactions after process death.
                            // An explicit notification/deep-link transaction ID is handled by LaunchedEffect above.
                            if (pendingCoordinatorId == null && smsOpenTransactionId.value.isNullOrBlank()) {
                                SmsTransactionQueue.showNextIfNeeded(context)
                            }
                            onDispose {
                                SmsCompletionCoordinator.unregisterHost()
                            }
                        }

                        fun onNavSelect(index: Int) {
                            selectedNavIndex = index

                            currentScreen = when (index) {
    0 -> AppScreen.Dashboard
    1 -> AppScreen.Transactions
    2 -> AppScreen.AddTransaction
    3 -> AppScreen.Accounts
    4 -> AppScreen.Tools
    else -> AppScreen.Dashboard
}
                        }

                        // CI-57C — CENTRAL SYSTEM BACK HANDLER
                        //
                        // Child BackHandlers composed later retain priority.
                        // The root Dashboard intentionally does not intercept
                        // system Back so Android can finish the Activity.
                        BackHandler(
                            enabled = smsSheetTransaction == null &&
                                !showAddBankAccountScreen &&
                                currentScreen != AppScreen.Dashboard
                        ) {
                            currentScreen = when (currentScreen) {
                                AppScreen.AddTransaction -> {
                                    selectedNavIndex = 0
                                    AppScreen.Dashboard
                                }
                                AppScreen.Transactions -> {
                                    selectedNavIndex = 0
                                    AppScreen.Dashboard
                                }
                                AppScreen.Accounts -> {
                                    selectedNavIndex = 0
                                    AppScreen.Dashboard
                                }
                                AppScreen.Tools -> {
                                    selectedNavIndex = 0
                                    AppScreen.Dashboard
                                }
                                AppScreen.Budget -> {
                                    selectedNavIndex = 0
                                    AppScreen.Dashboard
                                }

                                AppScreen.Reminder -> AppScreen.Tools
                                AppScreen.AddReminder -> AppScreen.Reminder

                                AppScreen.Transfer -> AppScreen.Operations
                                AppScreen.Operations -> AppScreen.Tools

                                AppScreen.Reports -> AppScreen.Tools

                                AppScreen.SettingsList -> AppScreen.Tools
                                AppScreen.SmsLearning -> AppScreen.SettingsList
                                AppScreen.ThemeBuilder -> AppScreen.SettingsList

                                // TransactionSyncScreen owns its system BackHandler.
                                AppScreen.TransactionSync -> AppScreen.SettingsList

                                AppScreen.LoanManagement -> AppScreen.Tools
                                AppScreen.CheckManagement -> AppScreen.Tools

                                AppScreen.Dashboard -> AppScreen.Dashboard
                            }
                        }

                        // Add-bank-account is an overlay state rather than
                        // an AppScreen, so system Back closes the overlay.
                        BackHandler(
                            enabled = showAddBankAccountScreen &&
                                smsSheetTransaction == null
                        ) {
                            showAddBankAccountScreen = false
                        }
                        AppScreenContainer {
                            when (currentScreen) {
                            is AppScreen.Accounts -> AccountsScreen(
                                selectedNavIndex = selectedNavIndex,
                                onNavSelect = ::onNavSelect,
                            )

                            is AppScreen.Transactions -> TransactionsScreen(

                                selectedNavIndex = selectedNavIndex,

                                onNavSelect = ::onNavSelect,

                            )


                            is AppScreen.Dashboard -> DashboardScreen(
                                onAddTransactionClick = { currentScreen = AppScreen.AddTransaction },
                                onOpenTransactions = {
                                    selectedNavIndex = 1
                                    currentScreen = AppScreen.Transactions
                                },
                                onOpenBudgetClick = {
                                    selectedNavIndex = 4
                                    currentScreen = AppScreen.Budget
                                },
                                onOpenMoreClick = {
                                    selectedNavIndex = 4
                                    currentScreen = AppScreen.Tools
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
                                 onOpenReports = {
    selectedNavIndex = 4
                                    currentScreen = AppScreen.Reports
},
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

        is AppScreen.Transfer -> TransferScreen(

            onBack = {

                currentScreen = AppScreen.Operations

            },

            onTransferCompleted = {

                currentScreen = AppScreen.Dashboard

            },

        )

        is AppScreen.Operations -> OperationsScreen(
            onBack = { currentScreen = AppScreen.Tools },
            onOpenCheckManagement = {
                currentScreen = AppScreen.CheckManagement
            },
            onOpenLoanManagement = {
                currentScreen = AppScreen.LoanManagement
            },
        onOpenTransfer = {
            currentScreen = AppScreen.Transfer
        },    
        )

                            is AppScreen.Reports -> ReportsScreen(
    selectedNavIndex = selectedNavIndex,
    onNavSelect = ::onNavSelect,
)

                            is AppScreen.SettingsList -> SettingsListScreen(
                                onBack = { currentScreen = AppScreen.Tools },
                                onOpenThemeBuilder = { currentScreen = AppScreen.ThemeBuilder },
        onOpenSmsLearning = {
                                    currentScreen = AppScreen.SmsLearning
                                },
                                onOpenTransactionSync = {
                                    currentScreen = AppScreen.TransactionSync
                                },
                                onOpenBankList = {
                                    currentScreen = AppScreen.Accounts
                                },
                            )
        is AppScreen.TransactionSync -> TransactionSyncScreen(
            viewModel =
                remember {
                    TransactionSyncViewModel(
                        applicationContext
                    )
                },
            onBack = {
                currentScreen = AppScreen.SettingsList
            }
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
                                onNavigate = { route ->
                                    currentScreen = when (route) {
                                        "dashboard" -> AppScreen.Dashboard
                                        "tools" -> AppScreen.Tools
                                        else -> AppScreen.Tools
                                    }
                                },
                            )

                            is AppScreen.CheckManagement -> CheckManagementScreen(
                                onBack = { currentScreen = AppScreen.Tools },
                            )
                            }
                        }

        // SMS transaction completion sheet

        if (smsSheetTransaction != null) {
            android.util.Log.d(
                "SMS_RECOMPOSE_DIAG",
                "SHEET_IF_ENTER transactionId=${smsSheetTransaction?.id} thread=${Thread.currentThread().name}"
            )

            val transaction = smsSheetTransaction!!


            val metadata =

                SmsTransactionMetadataManager.get(transaction.id)


            val detectedBank =

                metadata

                    ?.originalBank

                    ?.takeIf { it.isNotBlank() }

                    ?: transaction.subtitle


            val sender =

                metadata?.sender


            val learnedCategory =

                metadata?.let {

                    SmsLearningManager.findPatternRule(

                        sender = it.sender,

                        messageBody = it.rawBody,

                    )?.category

                        ?: SmsLearningManager.getSenderCategory(

                            it.sender

                        )

                }
            if (showAddBankAccountScreen) {
    AddBankAccountScreen(
        onBack = {
            showAddBankAccountScreen = false
        },
        onSave = { bankName, accountNumber, cardNumber, openingBalance ->
            AccountManager.add(
                name = bankName,
                type = AccountType.BANK,
                bankName = bankName,
                accountNumber = accountNumber,
                cardLast4 = cardNumber.takeLast(4),
                openingBalance = openingBalance,
            )
            showAddBankAccountScreen = false
        },
    )
} else {
    SmsTransactionCompletionSheetHost(

                transaction = transaction,

                detectedBank = detectedBank,

                sender = sender,

                initialCategory = learnedCategory,

                activeLoans = LoanStore.loans.toList(),

                receiptUri =

                    selectedReceiptUri

                        ?: transaction.receiptUri

                            ?.takeIf { it.isNotBlank() }

                            ?.let(Uri::parse),


                onPickReceipt = {

                    receiptPicker.launch(

                        arrayOf("image/*")

                    )

                },


                onTakePhoto = {


                    if (

                        androidx.core.content.ContextCompat.checkSelfPermission(

                            context,

                            android.Manifest.permission.CAMERA,

                        ) ==

                            android.content.pm.PackageManager.PERMISSION_GRANTED

                    ) {


                        val directory =

                            File(context.filesDir, "receipts").apply {

                                mkdirs()

                            }


                        val file =

                            File.createTempFile(

                                "receipt_camera_",

                                ".jpg",

                                directory,

                            )


                        val uri =

                            FileProvider.getUriForFile(

                                context,

                                 "${context.packageName}.fileprovider",

                                file,

                            )


                        pendingCameraUri = uri

                        cameraLauncher.launch(uri)


                    } else {


                        cameraPermissionLauncher.launch(

                            android.Manifest.permission.CAMERA

                        )

                    }

                },


                onComplete = {

                    selectedType,
                    isTransfer,
                    destinationAccountId,

                    category,

                    tags,

                    description,

                    receiptUri,

                    selectedBank,

                    selectedAccountId,

                    learnBank,

                    loan,

                    installment,

                ->


                    val currentMetadata =

                        SmsTransactionMetadataManager.get(

                            transaction.id

                        )


                    val result =

                        SmsTransactionCompletionHandler.complete(

                            context = context,

                            request =

                                SmsTransactionCompletionHandler.CompletionRequest(

                                    transaction = transaction,

                                    selectedType = selectedType,
                                    isTransfer = isTransfer,
                                    destinationAccountId = destinationAccountId,

                                    sender =

                                        currentMetadata

                                            ?.sender

                                            ?: "",

                                    rawBody =

                                        currentMetadata

                                            ?.rawBody,

                                    selectedBank = selectedBank,

                                    selectedCategory = category,

                                    tags = tags,

                                    description = description,

                                    receiptUri =

                                        receiptUri?.toString(),

                                    selectedAccountId =

                                        selectedAccountId,

                                    smsAccountNumber =

                                        currentMetadata

                                            ?.accountNumber

                                            ?.trim()

                                            ?.takeIf {

                                                it.isNotBlank()

                                            },

                                    smsCardLast4 =

                                        currentMetadata

                                            ?.cardLast4

                                            ?.trim()

                                            ?.takeIf {

                                                it.isNotBlank()

                                            },

                                    learnBank = learnBank,

                                    selectedLoan = loan,

                                    selectedInstallmentNumber =

                                        installment?.number,

                                ),

                        )


                    when (result) {


                        is SmsTransactionCompletionHandler.CompletionResult.Success -> {


                            smsSheetTransaction = null

                            selectedReceiptUri = null

                            pendingCameraUri = null


                            SmsTransactionQueue.onCompletionFinished(

                                context

                            )

                        }


                        SmsTransactionCompletionHandler.CompletionResult.NoChanges ->
                            Toast.makeText(context, "اطلاعات تکمیل تراکنش ناقص است.", Toast.LENGTH_SHORT).show()

                        SmsTransactionCompletionHandler.CompletionResult.TransactionNotFound ->
                            Toast.makeText(context, "تراکنش پیامکی پیدا نشد.", Toast.LENGTH_SHORT).show()

                        SmsTransactionCompletionHandler.CompletionResult.AccountRequired ->
                            Toast.makeText(context, "ابتدا یک حساب فعال انتخاب یا اضافه کنید.", Toast.LENGTH_SHORT).show()

                        SmsTransactionCompletionHandler.CompletionResult.InvalidAccount ->
                            Toast.makeText(context, "حساب انتخاب‌شده معتبر نیست.", Toast.LENGTH_SHORT).show()

                        SmsTransactionCompletionHandler.CompletionResult.InvalidInstallment ->
                            Toast.makeText(context, "قسط انتخاب‌شده معتبر نیست.", Toast.LENGTH_SHORT).show()

                        SmsTransactionCompletionHandler.CompletionResult.InstallmentAlreadyPaid ->
                            Toast.makeText(context, "این قسط قبلاً ثبت شده است.", Toast.LENGTH_SHORT).show()

                        SmsTransactionCompletionHandler.CompletionResult.InstallmentLinkFailed ->
                            Toast.makeText(context, "اتصال تراکنش به قسط انجام نشد.", Toast.LENGTH_SHORT).show()

                    }

                },


                onDismissAsNotBankTransaction = {

                    SmsTransactionCompletionHandler.dismissAsNotBankTransaction(
                        context = context,
                        transaction = transaction,
                    )

                    smsSheetTransaction = null
                    selectedReceiptUri = null
                    pendingCameraUri = null

                    SmsTransactionQueue.onCompletionFinished(
                        context
                    )
                },


                onIgnoreSender = {

    SmsTransactionCompletionHandler.ignoreSender(
        context = context,
        transaction = transaction,
        sender = sender,
    )

    smsSheetTransaction = null
    selectedReceiptUri = null
    pendingCameraUri = null

    SmsTransactionQueue.onCompletionFinished(
        context
    )
},


                onAddBankAccount = {
                    showAddBankAccountScreen = true
                },

                onDismiss = {

                    val dismissedTransactionId = smsSheetTransaction?.id

                    smsSheetTransaction = null

                    selectedReceiptUri = null

                    pendingCameraUri = null

                    if (!dismissedTransactionId.isNullOrBlank()) {
                        SmsTransactionQueue.onCompletionDismissed(
                            context = context,
                            transactionId = dismissedTransactionId,
                        )
                    } else {
                        SmsTransactionQueue.reset()
                    }

                },

            )

        }
    }
        }

                    }
        }

    }

        requestFirstRunPermissionsIfNeeded()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        smsOpenTransactionId.value = intent.getStringExtra(EXTRA_SMS_TRANSACTION_ID)
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

























































