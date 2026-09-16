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
import com.example.walletapp.ui.screens.ToolsScreen
import com.example.walletapp.ui.screens.ReportsScreen

import com.example.walletapp.ui.screens.OperationsScreen
import com.example.walletapp.ui.screens.TransferScreen
import com.example.walletapp.ui.screens.TransactionsScreen
import com.example.walletapp.ui.screens.AccountsScreen
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.DisposableEffect
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import com.example.walletapp.data.AccountStore
import com.example.walletapp.sms.SmsCompletionCoordinator
import com.example.walletapp.sms.SmsIgnoreStore
import com.example.walletapp.sms.SmsLearningStore
import com.example.walletapp.sms.SmsTransactionCompletionHandler
import com.example.walletapp.sms.SmsTransactionCompletionSheetHost
import com.example.walletapp.sms.SmsTransactionMetadataStore
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

        TransactionStore.attachContext(applicationContext)
        LoanStore.attachContext(applicationContext)
        LoanLegacyMigration.migrate(applicationContext)
        CheckStore.attachContext(applicationContext)
        AccountStore.attachContext(applicationContext)
        SmsTransactionMetadataStore.attachContext(applicationContext)
        SmsLearningStore.attachContext(applicationContext)
        SmsIgnoreStore.attachContext(applicationContext)

        setContent {
            // App content is Persian, so force RTL layout direction regardless
            // of device locale.
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                WalletAppTheme {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Dashboard) }
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

                        val context =
                            androidx.compose.ui.platform.LocalContext.current

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
                                        File(
                                            context.filesDir,
                                            "receipts"
                                        ).apply {
                                            mkdirs()
                                        }

                                    val file =
                                        File.createTempFile(
                                            "receipt_camera_",
                                            ".jpg",
                                            directory
                                        )

                                    val uri =
                                        FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            file
                                        )

                                    pendingCameraUri = uri
                                    cameraLauncher.launch(uri)
                                }
                            }

                        DisposableEffect(Unit) {

                            SmsCompletionCoordinator.registerHost { transactionId ->

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

                                    selectedReceiptUri =
                                        transaction.receiptUri
                                            ?.takeIf {
                                                it.isNotBlank()
                                            }
                                            ?.let(Uri::parse)
                                }
                            }

                            onDispose {
                                SmsCompletionCoordinator.unregisterHost()
                            }
                        }

                        smsSheetTransaction?.let { transaction ->

                            val metadata =
                                SmsTransactionMetadataStore.get(
                                    transaction.id
                                )

                            val detectedBank =
                                metadata
                                    ?.originalBank
                                    ?.takeIf {
                                        it.isNotBlank()
                                    }
                                    ?: transaction.subtitle

                            val sender =
                                metadata?.sender

                            val learnedCategory =
                                metadata?.let {
                                    SmsLearningStore.findPatternRule(
                                        sender = it.sender,
                                        messageBody = it.rawBody,
                                    )?.category
                                        ?: SmsLearningStore.getSenderCategory(
                                            it.sender
                                        )
                                }

                            SmsTransactionCompletionSheetHost(
                                transaction = transaction,
                                detectedBank = detectedBank,
                                sender = sender,
                                initialCategory = learnedCategory,
                                activeLoans = LoanStore.loans.toList(),
                                receiptUri =
                                    selectedReceiptUri
                                        ?: transaction.receiptUri
                                            ?.let(Uri::parse),

                                onPickReceipt = {
                                    receiptPicker.launch(
                                        arrayOf("image/*")
                                    )
                                },

                                onTakePhoto = {

                                    if (
                                        androidx.core.content.ContextCompat
                                            .checkSelfPermission(
                                                context,
                                                Manifest.permission.CAMERA
                                            ) ==
                                        android.content.pm.PackageManager
                                            .PERMISSION_GRANTED
                                    ) {

                                        val directory =
                                            File(
                                                context.filesDir,
                                                "receipts"
                                            ).apply {
                                                mkdirs()
                                            }

                                        val file =
                                            File.createTempFile(
                                                "receipt_camera_",
                                                ".jpg",
                                                directory
                                            )

                                        val uri =
                                            FileProvider.getUriForFile(
                                                context,
                                                "${context.packageName}.fileprovider",
                                                file
                                            )

                                        pendingCameraUri = uri
                                        cameraLauncher.launch(uri)

                                    } else {

                                        cameraPermissionLauncher.launch(
                                            Manifest.permission.CAMERA
                                        )
                                    }
                                },

                                onComplete = {
                                    category,
                                    tags,
                                    description,
                                    receiptUri,
                                    selectedBank,
                                    selectedAccountId,
                                    learnBank,
                                    loan,
                                    installment ->

                                    val request =
                                        SmsTransactionCompletionHandler.CompletionRequest(
                                            transaction = transaction,
                                            sender =
                                                metadata?.sender
                                                    ?: "",
                                            rawBody =
                                                metadata?.rawBody,
                                            selectedBank =
                                                selectedBank,
                                            selectedCategory =
                                                category,
                                            tags =
                                                tags,
                                            description =
                                                description,
                                            receiptUri =
                                                receiptUri?.toString(),
                                            selectedAccountId =
                                                selectedAccountId,
                                            smsAccountNumber =
                                                metadata
                                                    ?.accountNumber
                                                    ?.trim()
                                                    ?.takeIf {
                                                        it.isNotBlank()
                                                    },
                                            smsCardLast4 =
                                                metadata
                                                    ?.cardLast4
                                                    ?.trim()
                                                    ?.takeIf {
                                                        it.isNotBlank()
                                                    },
                                            learnBank =
                                                learnBank,
                                            selectedLoan =
                                                loan,
                                            selectedInstallmentNumber =
                                                installment
                                                    ?.number,
                                        )

                                    when (
                                        val result =
                                            SmsTransactionCompletionHandler
                                                .complete(
                                                    context,
                                                    request
                                                )
                                    ) {

                                        is SmsTransactionCompletionHandler
                                            .CompletionResult.Success -> {

                                            smsSheetTransaction = null
                                            selectedReceiptUri = null
                                            pendingCameraUri = null

                                            SmsTransactionQueue
                                                .onCompletionFinished(
                                                    context
                                                )
                                        }

                                        else -> {
                                            // Keep the sheet open so the
                                            // existing UI can correct the
                                            // missing/invalid completion data.
                                        }
                                    }
                                },

                                onIgnoreSender = {

                                    val ignoreMetadata =
                                        SmsTransactionMetadataStore.get(
                                            transaction.id
                                        )

                                    ignoreMetadata?.let { meta ->

                                        if (
                                            !sender.isNullOrBlank() &&
                                            !meta.rawBody.isNullOrBlank()
                                        ) {

                                            SmsIgnoreStore.ignorePattern(
                                                sender = sender,
                                                pattern = meta.rawBody
                                            )
                                        }
                                    }

                                    TransactionStore.update(
                                        transaction.copy(
                                            requiresCompletion = false
                                        )
                                    )

                                    smsSheetTransaction = null
                                    selectedReceiptUri = null

                                    SmsTransactionQueue.onCompletionFinished(context)
                                    pendingCameraUri = null

                                    SmsTransactionQueue
                                        .onCompletionFinished(
                                            context
                                        )
                                },

                                onDismiss = {

                                    smsSheetTransaction = null
                                    selectedReceiptUri = null
                                    pendingCameraUri = null
                                }
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




































