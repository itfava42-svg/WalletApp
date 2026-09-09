package com.example.walletapp.sms

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.RadioButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.walletapp.data.Transaction
import com.example.walletapp.data.TransactionStore
import com.example.walletapp.data.TransactionType
import com.example.walletapp.data.AccountStore
import com.example.walletapp.data.LoanEntry
import com.example.walletapp.data.LoanStore
import com.example.walletapp.data.toPersianCurrency
import com.example.walletapp.loan.LoanPaymentManager
import com.example.walletapp.ui.theme.AppTextStyles
import com.example.walletapp.ui.theme.WalletAppTheme
import java.io.File

private data class LoanInstallmentChoice(
    val number: Int,
    val dueDate: String,
    val amount: Long,
)

private fun unpaidInstallmentsFor(
    loan: LoanEntry,
): List<LoanInstallmentChoice> {
    val paidNumbers = loan.payments.map { it.installmentNumber }.toSet()

    return (1..loan.totalInstallments)
        .filterNot { it in paidNumbers }
        .map { number ->
            LoanInstallmentChoice(
                number = number,
                dueDate = loanInstallmentDueDate(loan, number),
                amount = loan.installmentAmount,
            )
        }
}

private fun loanInstallmentDueDate(
    loan: LoanEntry,
    installmentNumber: Int,
): String {
    val year = loan.firstInstallmentYear ?: return "تاریخ نامشخص"
    val month = loan.firstInstallmentMonth ?: return "تاریخ نامشخص"
    val day = loan.firstInstallmentDay ?: return "تاریخ نامشخص"
    val monthsToAdd = (installmentNumber - 1) * loan.intervalMonths
    val totalMonths = (year * 12) + (month - 1) + monthsToAdd
    val dueYear = totalMonths / 12
    val dueMonth = (totalMonths % 12) + 1
    val maxDay = if (dueMonth <= 6) 31 else 30
    val dueDay = day.coerceAtMost(maxDay)

    return "%04d/%02d/%02d".format(dueYear, dueMonth, dueDay)
}

class SmsTransactionCompletionActivity : ComponentActivity() {

    private val receiptPicker =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                runCatching {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION,
                    )
                }
                selectedReceiptUri = uri
            }
        }

    private var selectedReceiptUri by mutableStateOf<Uri?>(null)

    private var cameraUri: Uri? = null

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                val uri = createCameraUri()
                cameraUri = uri
                cameraLauncher.launch(uri)
            }
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            val uri = cameraUri
            if (success && uri != null) {
                selectedReceiptUri = uri
            }
        }

    private var transactionId: String? = null

    private fun createCameraUri(): Uri {
        val directory =
            File(filesDir, "receipts").apply {
                mkdirs()
            }

        val file =
            File.createTempFile(
                "receipt_camera_",
                ".jpg",
                directory,
            )

        return FileProvider.getUriForFile(
            this,
            "$packageName.fileprovider",
            file,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        transactionId =
            intent.getStringExtra(EXTRA_TRANSACTION_ID)

        val id = transactionId

        if (id.isNullOrBlank()) {
            finish()
            return
        }

                TransactionStore.attachContext(applicationContext)
        AccountStore.attachContext(applicationContext)
        SmsTransactionMetadataStore.attachContext(applicationContext)
        SmsLearningStore.attachContext(applicationContext)
        SmsIgnoreStore.attachContext(applicationContext)
        LoanStore.attachContext(applicationContext)

        val transaction =
            TransactionStore.transactions
                .firstOrNull { it.id == id }

        if (
            transaction == null ||
            !transaction.requiresCompletion
        ) {
            finish()
            return
        }

        val metadata =
            SmsTransactionMetadataStore.get(id)

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

        setContent {
            WalletAppTheme {
                SmsCompletionContent(
                    transaction = transaction,
                    detectedBank =
                        metadata?.originalBank
                            ?.takeIf { it.isNotBlank() }
                            ?: transaction.subtitle,
                    sender = metadata?.sender,
                    initialCategory = learnedCategory,
                    activeLoans = LoanStore.loans.toList(),
                    receiptUri =
                        selectedReceiptUri
                            ?: transaction.receiptUri?.let(Uri::parse),
                    onPickReceipt = {
                        receiptPicker.launch(
                            arrayOf("image/*")
                        )
                    },
                    onTakePhoto = {
                        if (
                            androidx.core.content.ContextCompat.checkSelfPermission(
                                this,
                                android.Manifest.permission.CAMERA,
                            ) ==
                                android.content.pm.PackageManager.PERMISSION_GRANTED
                        ) {
                            val uri = createCameraUri()
                            cameraUri = uri
                            cameraLauncher.launch(uri)
                        } else {
                            cameraPermissionLauncher.launch(
                                android.Manifest.permission.CAMERA
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
                            installment,
                        ->
                        completeTransaction(
                            transaction = transaction,
                            category = category,
                            tags = tags,
                            description = description,
                            receiptUri = receiptUri,
                            selectedBank = selectedBank,
                            selectedAccountId = selectedAccountId,
                            learnBank = learnBank,
                            loan = loan,
                            installment = installment,
                        )
                    },
                    onIgnoreSender = {
                        ignoreSenderAndFinish(
                            transaction = transaction,
                            sender = metadata?.sender,
                        )
                    },
                )
            }
        }
    }

    private fun completeTransaction(
        transaction: Transaction,
        category: String?,
        tags: List<String>,
        description: String,
        receiptUri: Uri?,
        selectedBank: String,
        selectedAccountId: String?,
        learnBank: Boolean,
        loan: LoanEntry?,
        installment: LoanInstallmentChoice?,
    ) {
        if (category == "قسط") {
            if (
                loan == null ||
                installment == null ||
                installment.number !in 1..loan.totalInstallments ||
                loan.payments.any {
                    it.installmentNumber == installment.number
                }
            ) {
                return
            }

            val linkedInstallment =
                LoanPaymentManager.linkExistingTransactionToInstallment(
                    context = applicationContext,
                    loan = loan,
                    installmentNumber = installment.number,
                    transactionId = transaction.id,
                )

            if (linkedInstallment == null) {
                return
            }
        }

        val hasCompletionData =
            category != null ||
                tags.isNotEmpty() ||
                description.isNotBlank() ||
                receiptUri != null ||
                selectedBank != transaction.subtitle

        if (!hasCompletionData) return

        val metadata =
            SmsTransactionMetadataStore.get(transaction.id)

        if (
            learnBank &&
            !metadata?.sender.isNullOrBlank()
        ) {
            SmsLearningStore.saveSenderBank(
                sender = metadata!!.sender,
                bankName = selectedBank,
            )
        }

        if (
            category != null &&
            !metadata?.sender.isNullOrBlank()
        ) {
            SmsLearningStore.saveSenderCategory(
                sender = metadata!!.sender,
                category = category,
            )
        }

        // -------------------------------------------------------------
        // یادگیری الگوی واقعی پیامک
        // اعداد متغیر مانند مبلغ، تاریخ و شماره حساب
        // در SmsLearningStore هنگام ذخیره الگو نرمال می‌شوند.
        // -------------------------------------------------------------
        if (!metadata?.sender.isNullOrBlank()) {
            SmsLearningStore.savePatternRule(
                sender = metadata!!.sender,
                pattern = metadata.rawBody,
                bankName = selectedBank,
                category = category,
                transactionType = transaction.type.name,
            )
        }

        /*
         * Account linking is intentionally conservative.
         *
         * Exactly one active account for the selected bank:
         *     -> link automatically.
         *
         * Zero or multiple matching accounts:
         *     -> do not guess.
         *
         * Multiple-account selection will be added in the next step.
         */
        val matchingAccounts =
            AccountStore.accounts.filter { account ->
                account.isActive &&
                    account.bankName.trim() == selectedBank.trim()
            }

        val resolvedAccountId =
            when {
                matchingAccounts.size == 1 ->
                    matchingAccounts.first().id

                else ->
                    transaction.accountId
            }

        val updatedTransaction =
            if (
                transaction.subtitle == "تراکنش بانکی"
            ) {
                transaction.copy(
                    title = selectedBank,
                    subtitle = "تراکنش بانکی",
                    accountId = selectedAccountId ?: resolvedAccountId,
                    iconKey =
                        category
                            ?: transaction.iconKey,
                    tags = tags,
                    description = description.trim(),
                    receiptUri = receiptUri?.toString(),
                    requiresCompletion = false,
                )
            } else {
                transaction.copy(
                    subtitle = selectedBank,
                    accountId = selectedAccountId ?: resolvedAccountId,
                    iconKey =
                        category
                            ?: transaction.iconKey,
                    tags = tags,
                    description = description.trim(),
                    receiptUri = receiptUri?.toString(),
                    requiresCompletion = false,
                )
            }

        TransactionStore.update(updatedTransaction)

        SmsTransactionQueue.onCompletionFinished(this)

        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun ignoreSenderAndFinish(
        transaction: Transaction,
        sender: String?,
    ) {
        if (!sender.isNullOrBlank()) {
            SmsIgnoreStore.ignoreSender(sender)
        }

        TransactionStore.update(
            transaction.copy(
                requiresCompletion = false,
            )
        )

        SmsTransactionQueue.onCompletionFinished(this)

        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onBackPressed() {
        // Intentionally blocked: this activity is mandatory.
    }

    companion object {
        private const val EXTRA_TRANSACTION_ID =
            "com.example.walletapp.sms.TRANSACTION_ID"

        fun start(
            context: Context,
            transactionId: String,
        ) {
            val intent =
                Intent(
                    context,
                    SmsTransactionCompletionActivity::class.java,
                ).apply {
                    putExtra(
                        EXTRA_TRANSACTION_ID,
                        transactionId,
                    )
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

            context.startActivity(intent)
        }
    }
}

@Composable
private fun SmsCompletionContent(
    transaction: Transaction,
    detectedBank: String,
    sender: String?,
    initialCategory: String?,
    activeLoans: List<LoanEntry>,
    receiptUri: Uri?,
    onPickReceipt: () -> Unit,
    onTakePhoto: () -> Unit,
    onComplete: (
        category: String?,
        tags: List<String>,
        description: String,
        receiptUri: Uri?,
        selectedBank: String,
        selectedAccountId: String?,
        learnBank: Boolean,
        loan: LoanEntry?,
        installment: LoanInstallmentChoice?,
    ) -> Unit,
    onIgnoreSender: () -> Unit,
) {
    BackHandler(enabled = true) {}

    var selectedCategory by
        remember(initialCategory) {
            mutableStateOf(initialCategory)
        }

    var tags by remember {
        mutableStateOf(transaction.tags)
    }

    var tagInput by remember {
        mutableStateOf("")
    }

    var description by remember {
        mutableStateOf(transaction.description)
    }

    var selectedBank by remember(detectedBank) {
        mutableStateOf(detectedBank)
    }

    val matchingAccounts =
        remember(selectedBank) {
            AccountStore.accounts.filter { account ->
                account.isActive &&
                    account.bankName.trim() == selectedBank.trim()
            }
        }

    var selectedAccountId by remember(transaction.id, selectedBank) {
        mutableStateOf(
            transaction.accountId
                ?.takeIf { id ->
                    matchingAccounts.any { it.id == id }
                }
                ?: matchingAccounts.singleOrNull()?.id
        )
    }

    var showBankDialog by remember {
        mutableStateOf(false)
    }

    var showAccountDialog by remember {
        mutableStateOf(false)
    }

    var showIgnoreDialog by remember {
        mutableStateOf(false)
    }

    var showLoanDialog by remember { mutableStateOf(false) }
    var showInstallmentDialog by remember { mutableStateOf(false) }
    var selectedLoan by remember { mutableStateOf<LoanEntry?>(null) }
    var selectedInstallment by remember { mutableStateOf<LoanInstallmentChoice?>(null) }

    var learnBank by remember {
        mutableStateOf(true)
    }

    val banks =
        listOf(
            "بانک ملی",
            "بانک سپه",
            "بانک تجارت",
            "بانک ملت",
            "بانک صادرات",
            "بانک کشاورزی",
            "بانک مسکن",
            "بانک توسعه تعاون",
            "بانک رفاه کارگران",
            "بانک اقتصاد نوین",
            "بانک پارسیان",
            "بانک سامان",
            "بانک پاسارگاد",
            "بانک سینا",
            "بانک سرمایه",
            "بلو (بانک آینده)",
            "بانک قرض الحسنه رسالت",
            "بانک شهر",
            "بانک قرض الحسنه مهر ایران",
            "بانک دی",
            "بانک آینده",
            "بانک گردشگری",
            "بانک ایران زمین",
            "بانک خاورمیانه",
            "بانک کارآفرین",
            "بانک حکمت ایرانیان",
            "بانک انصار",
            "بانک قوامین",
        )

    val categories =
        when (transaction.type) {
            TransactionType.EXPENSE ->
                listOf(
                    "غذا",
                    "حمل‌ونقل",
                    "قبض",
                    "قسط",
                    "خرید",
                    "سلامت",
                    "تفریح",
                    "سایر",
                )

            TransactionType.INCOME ->
                listOf(
                    "حقوق",
                    "هدیه",
                    "سرمایه‌گذاری",
                    "بازگشت وجه",
                    "سایر",
                )
        }

    val hasCompletionData =
        selectedCategory != null ||
            tags.isNotEmpty() ||
            description.isNotBlank() ||
            receiptUri != null ||
            selectedBank != detectedBank

    val isInstallmentCategory = selectedCategory == "قسط"
    val unpaidInstallments =
        selectedLoan?.let(::unpaidInstallmentsFor).orEmpty()
    val canComplete =
        hasCompletionData &&
            (matchingAccounts.size <= 1 || selectedAccountId != null) &&
            (!isInstallmentCategory ||
                (selectedLoan != null && selectedInstallment != null))

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    MaterialTheme.colorScheme.background
                ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(
                        horizontal = 20.dp,
                        vertical = 18.dp,
                    )
                    .navigationBarsPadding(),
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "تکمیل تراکنش",
                        style = AppTextStyles.headlineSm,
                        color =
                            MaterialTheme.colorScheme.onSurface,
                    )

                    Spacer(
                        Modifier.height(3.dp)
                    )

                    Text(
                        text =
                            "اطلاعات تراکنش بانکی را بررسی و تکمیل کنید",
                        style = AppTextStyles.bodyMd,
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Box(
                    modifier =
                        Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector =
                            Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint =
                            MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            TransactionSummaryCard(
                transaction = transaction,
                bankName = selectedBank,
            )

            Spacer(Modifier.height(18.dp))

            SectionTitle(
                icon = Icons.Default.AccountBalance,
                title = "بانک",
                subtitle =
                    if (sender.isNullOrBlank()) {
                        "بانک تشخیص‌داده‌شده"
                    } else {
                        "فرستنده: $sender"
                    },
            )

            Spacer(Modifier.height(10.dp))

            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            showBankDialog = true
                        },
                shape = MaterialTheme.shapes.large,
                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme.surfaceContainer
                    ),
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    verticalAlignment =
                        Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector =
                            Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint =
                            MaterialTheme.colorScheme.primary,
                    )

                    Spacer(
                        Modifier.width(12.dp)
                    )

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = selectedBank,
                            style =
                                AppTextStyles.bodyLg.copy(
                                    fontWeight =
                                        FontWeight.SemiBold
                                ),
                        )

                        Spacer(
                            Modifier.height(3.dp)
                        )

                        Text(
                            text = "برای تغییر بانک لمس کنید",
                            style =
                                AppTextStyles.labelMd,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Text(
                        text = "تغییر",
                        color =
                            MaterialTheme.colorScheme.primary,
                        fontWeight =
                            FontWeight.SemiBold,
                    )
                }
            }

            if (
                selectedBank != detectedBank
            ) {
                Spacer(
                    Modifier.height(8.dp)
                )

                Text(
                    text =
                        "بانک اصلاح شد؛ این اصلاح می‌تواند برای پیامک‌های بعدی همین فرستنده ذخیره شود.",
                    style = AppTextStyles.labelMd,
                    color =
                        MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(Modifier.height(20.dp))

            SectionTitle(
                icon = Icons.Default.AccountBalance,
                title = "حساب",
                subtitle =
                    when {
                        matchingAccounts.isEmpty() ->
                            "برای این بانک حساب فعالی ثبت نشده است"

                        matchingAccounts.size == 1 ->
                            "حساب به‌صورت خودکار انتخاب شده است"

                        else ->
                            "حساب مورد استفاده برای این تراکنش را انتخاب کنید"
                    },
            )

            Spacer(Modifier.height(10.dp))

            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable(enabled = matchingAccounts.isNotEmpty()) {
                            showAccountDialog = true
                        },
                shape = MaterialTheme.shapes.large,
                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme.surfaceContainer
                    ),
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    verticalAlignment =
                        Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector =
                            Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint =
                            MaterialTheme.colorScheme.primary,
                    )

                    Spacer(Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        val selectedAccount =
                            matchingAccounts.firstOrNull {
                                it.id == selectedAccountId
                            }

                        Text(
                            text =
                                selectedAccount?.name
                                    ?: if (matchingAccounts.isEmpty()) {
                                        "حسابی برای این بانک پیدا نشد"
                                    } else {
                                        "انتخاب حساب"
                                    },
                            style =
                                AppTextStyles.bodyLg.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                        )

                        Spacer(Modifier.height(3.dp))

                        Text(
                            text =
                                selectedAccount?.let {
                                    it.bankName.ifBlank {
                                        "حساب بانکی"
                                    }
                                } ?: "برای انتخاب حساب لمس کنید",
                            style = AppTextStyles.labelMd,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    if (matchingAccounts.size > 1) {
                        Text(
                            text = "تغییر",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            if (matchingAccounts.size > 1 && selectedAccountId == null) {
                Spacer(Modifier.height(8.dp))

                Text(
                    text = "برای ثبت این تراکنش، ابتدا حساب را انتخاب کنید.",
                    style = AppTextStyles.labelMd,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(Modifier.height(20.dp))

            SectionTitle(
                icon = Icons.Default.Label,
                title = "دسته‌بندی",
                subtitle = "یک دسته برای این تراکنش انتخاب کنید",
            )

            Spacer(Modifier.height(10.dp))

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {
                categories.chunked(2).forEach { rowCategories ->
                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp),
                    ) {
                        rowCategories.forEach { category ->
                            FilterChip(
                                selected =
                                    selectedCategory ==
                                        category,
                                onClick = {
                                    selectedCategory =
                                        if (
                                            selectedCategory ==
                                                category
                                        ) {
                                            null
                                        } else {
                                            category
                                        }

                                    if (selectedCategory != "قسط") {
                                        selectedLoan = null
                                        selectedInstallment = null
                                    }
                                },
                                label = {
                                    Text(
                                        text = category,
                                        style =
                                            AppTextStyles.bodyMd,
                                    )
                                },
                                modifier =
                                    Modifier.weight(1f),
                            )
                        }

                        if (
                            rowCategories.size == 1
                        ) {
                            Spacer(
                                Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            if (isInstallmentCategory) {
                Spacer(Modifier.height(20.dp))

                SectionTitle(
                    icon = Icons.Default.AccountBalance,
                    title = "وام و قسط",
                    subtitle = "ابتدا وام فعال و سپس قسط پرداخت‌نشده را انتخاب کنید",
                )

                Spacer(Modifier.height(10.dp))

                if (activeLoans.isEmpty()) {
                    Text(
                        text = "وام فعالی برای انتخاب وجود ندارد.",
                        style = AppTextStyles.bodyMd,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    SelectionCard(
                        title = "وام",
                        value = selectedLoan?.title ?: "انتخاب وام فعال",
                        onClick = { showLoanDialog = true },
                    )

                    Spacer(Modifier.height(8.dp))

                    SelectionCard(
                        title = "قسط",
                        value =
                            selectedInstallment?.let {
                                "قسط شماره ${it.number} — ${it.dueDate}"
                            } ?: "انتخاب قسط پرداخت‌نشده",
                        enabled = selectedLoan != null,
                        onClick = { showInstallmentDialog = true },
                    )

                    if (selectedLoan != null && unpaidInstallments.isEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "همه اقساط این وام پرداخت شده‌اند.",
                            style = AppTextStyles.labelMd,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            SectionTitle(
                icon = Icons.Default.Label,
                title = "برچسب",
                subtitle =
                    "برای جست‌وجوی سریع‌تر، برچسب اضافه کنید",
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = tagInput,
                onValueChange = {
                    tagInput = it
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = {
                    Text("برچسب جدید")
                },
                trailingIcon = {
                    TextButton(
                        enabled =
                            tagInput.trim().isNotEmpty(),
                        onClick = {
                            val value =
                                tagInput.trim()

                            if (
                                value.isNotEmpty() &&
                                value !in tags
                            ) {
                                tags =
                                    tags + value
                                tagInput = ""
                            }
                        },
                    ) {
                        Text("افزودن")
                    }
                },
            )

            if (tags.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp),
                ) {
                    tags.forEach { tag ->
                        FilterChip(
                            selected = true,
                            onClick = {
                                tags =
                                    tags - tag
                            },
                            label = {
                                Text(tag)
                            },
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            SectionTitle(
                icon = Icons.Default.Description,
                title = "توضیحات",
                subtitle =
                    "جزئیات دلخواه خود را درباره این تراکنش بنویسید",
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = description,
                onValueChange = {
                    description = it
                },
                modifier =
                    Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                label = {
                    Text("توضیحات تراکنش")
                },
                placeholder = {
                    Text("مثلاً خرید ماهانه منزل")
                },
            )

            Spacer(Modifier.height(20.dp))

            SectionTitle(
                icon = Icons.Default.AddPhotoAlternate,
                title = "رسید",
                subtitle =
                    "تصویر رسید را به این تراکنش پیوست کنید",
            )

            Spacer(Modifier.height(10.dp))

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(12.dp),
            ) {
                ReceiptActionCard(
                    modifier =
                        Modifier.weight(1f),
                    icon =
                        Icons.Default.AddPhotoAlternate,
                    title = "پیوست تصویر",
                    subtitle =
                        if (receiptUri == null) {
                            "از گالری یا فایل‌ها"
                        } else {
                            "رسید انتخاب شد"
                        },
                    onClick = onPickReceipt,
                )

                ReceiptActionCard(
                    modifier =
                        Modifier.weight(1f),
                    icon = Icons.Default.CameraAlt,
                    title = "دوربین",
                    subtitle = "ثبت عکس جدید",
                    onClick = onTakePhoto,
                )
            }

            Spacer(Modifier.height(20.dp))

            Card(
                modifier =
                    Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme.surfaceContainer
                    ),
            ) {
                Column(
                    modifier =
                        Modifier.padding(14.dp)
                ) {
                    Text(
                        text =
                            "یادگیری هوشمند",
                        style =
                            AppTextStyles.bodyLg.copy(
                                fontWeight =
                                    FontWeight.SemiBold
                            ),
                    )

                    Spacer(
                        Modifier.height(5.dp)
                    )

                    Text(
                        text =
                            "اگر بانک را اصلاح کرده‌اید، اصلاح برای پیامک‌های بعدی این فرستنده ذخیره شود.",
                        style =
                            AppTextStyles.bodyMd,
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(
                        Modifier.height(8.dp)
                    )

                    FilterChip(
                        selected = learnBank,
                        onClick = {
                            learnBank = !learnBank
                        },
                        label = {
                            Text(
                                if (learnBank) {
                                    "ذخیره اصلاح بانک برای این فرستنده"
                                } else {
                                    "فقط اصلاح همین تراکنش"
                                }
                            )
                        },
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            OutlinedButton(
                onClick = {
                    showIgnoreDialog = true
                },
                modifier =
                    Modifier.fillMaxWidth(),
                shape =
                    MaterialTheme.shapes.large,
            ) {
                Text(
                    text =
                        "این فرستنده را دیگر بانکی شناسایی نکن"
                )
            }

            Spacer(Modifier.height(20.dp))

            Card(
                modifier =
                    Modifier.fillMaxWidth(),
                shape =
                    MaterialTheme.shapes.large,
                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            if (canComplete) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainer
                            },
                    ),
            ) {
                Row(
                    modifier =
                        Modifier.padding(14.dp),
                    verticalAlignment =
                        Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector =
                            Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint =
                            if (canComplete) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                    )

                    Spacer(
                        Modifier.width(10.dp)
                    )

                    Text(
                        text =
                            if (canComplete) {
                                "اطلاعات تکمیل شد؛ تراکنش آماده ثبت است."
                            } else {
                                "حداقل یکی از موارد بالا را تکمیل کنید."
                            },
                        style =
                            AppTextStyles.bodyMd,
                        color =
                            if (canComplete) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Button(
                onClick = {
                    onComplete(
                        selectedCategory,
                        tags,
                        description,
                        receiptUri,
                        selectedBank,
                        selectedAccountId,
                        learnBank,
                        selectedLoan,
                        selectedInstallment,
                    )
                },
                enabled = canComplete,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                shape =
                    MaterialTheme.shapes.large,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            MaterialTheme.colorScheme.primary,
                    ),
            ) {
                Text(
                    text = "تکمیل و ثبت تراکنش",
                    style =
                        AppTextStyles.bodyLg.copy(
                            fontWeight =
                                FontWeight.SemiBold
                        ),
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }

    if (showBankDialog) {
        AlertDialog(
            onDismissRequest = {
                showBankDialog = false
            },
            title = {
                Text("انتخاب بانک")
            },
            text = {
                Column(
                    modifier =
                        Modifier
                            .heightIn(
                                max = 420.dp
                            )
                            .verticalScroll(
                                rememberScrollState()
                            ),
                    verticalArrangement =
                        Arrangement.spacedBy(4.dp),
                ) {
                    banks.forEach { bank ->
                        Text(
                            text = bank,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedBank =
                                            bank
                                        showBankDialog =
                                            false
                                    }
                                    .padding(
                                        vertical = 12.dp,
                                        horizontal = 8.dp,
                                    ),
                            color =
                                if (
                                    selectedBank == bank
                                ) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                            fontWeight =
                                if (
                                    selectedBank == bank
                                ) {
                                    FontWeight.Bold
                                } else {
                                    FontWeight.Normal
                                },
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showBankDialog = false
                    }
                ) {
                    Text("بستن")
                }
            },
        )
    }

    if (showLoanDialog) {
        AlertDialog(
            onDismissRequest = { showLoanDialog = false },
            title = { Text("انتخاب وام فعال") },
            text = {
                Column(
                    modifier = Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    activeLoans.forEach { loan ->
                        Text(
                            text = loan.title,
                            modifier = Modifier.fillMaxWidth().clickable {
                                selectedLoan = loan
                                selectedInstallment = null
                                showLoanDialog = false
                                showInstallmentDialog = true
                            }.padding(vertical = 12.dp, horizontal = 8.dp),
                            color = if (selectedLoan?.id == loan.id) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showLoanDialog = false }) { Text("بستن") } },
        )
    }

    if (showInstallmentDialog) {
        AlertDialog(
            onDismissRequest = { showInstallmentDialog = false },
            title = { Text("انتخاب قسط پرداخت‌نشده") },
            text = {
                Column(
                    modifier = Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    unpaidInstallments.forEach { installment ->
                        Text(
                            text = "قسط شماره ${installment.number} — ${installment.dueDate} — ${installment.amount.toPersianCurrency()} تومان",
                            modifier = Modifier.fillMaxWidth().clickable {
                                selectedInstallment = installment
                                showInstallmentDialog = false
                            }.padding(vertical = 12.dp, horizontal = 8.dp),
                            color = if (selectedInstallment?.number == installment.number) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showInstallmentDialog = false }) { Text("بستن") } },
        )
    }

    if (showAccountDialog) {
        AlertDialog(
            onDismissRequest = {
                showAccountDialog = false
            },
            title = {
                Text("انتخاب حساب")
            },
            text = {
                Column {
                    matchingAccounts.forEach { account ->
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedAccountId = account.id
                                        showAccountDialog = false
                                    }
                                    .padding(
                                        vertical = 12.dp,
                                        horizontal = 8.dp,
                                    ),
                            verticalAlignment =
                                Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected =
                                    selectedAccountId == account.id,
                                onClick = {
                                    selectedAccountId = account.id
                                    showAccountDialog = false
                                },
                            )

                            Spacer(Modifier.width(8.dp))

                            Column {
                                Text(
                                    text = account.name,
                                    fontWeight = FontWeight.SemiBold,
                                )

                                if (account.accountNumber.isNotBlank()) {
                                    Text(
                                        text =
                                            "شماره حساب: ${account.accountNumber}",
                                        style =
                                            AppTextStyles.labelMd,
                                        color =
                                            MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                } else if (account.cardLast4.isNotBlank()) {
                                    Text(
                                        text =
                                            "کارت: ****${account.cardLast4}",
                                        style =
                                            AppTextStyles.labelMd,
                                        color =
                                            MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showAccountDialog = false
                    }
                ) {
                    Text("انصراف")
                }
            },
        )
    }

    if (showIgnoreDialog) {
        AlertDialog(
            onDismissRequest = {
                showIgnoreDialog = false
            },
            title = {
                Text("نادیده گرفتن فرستنده")
            },
            text = {
                Text(
                    "پیامک‌های بعدی از این فرستنده دیگر به‌عنوان پیامک بانکی پردازش نمی‌شوند."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showIgnoreDialog = false
                        onIgnoreSender()
                    }
                ) {
                    Text("تأیید")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showIgnoreDialog = false
                    }
                ) {
                    Text("انصراف")
                }
            },
        )
    }
}

@Composable
private fun SelectionCard(
    title: String,
    value: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = AppTextStyles.labelMd)
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = AppTextStyles.bodyLg,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TransactionSummaryCard(
    transaction: Transaction,
    bankName: String,
) {
    val isExpense =
        transaction.type ==
            TransactionType.EXPENSE

    val accent =
        if (isExpense) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.primary
        }

    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            MaterialTheme.shapes.extraLarge,
        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surfaceContainerLow,
            ),
    ) {
        Column(
            modifier =
                Modifier.padding(18.dp)
        ) {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically,
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(48.dp)
                            .clip(
                                RoundedCornerShape(14.dp)
                            )
                            .background(
                                MaterialTheme.colorScheme.primaryContainer
                            ),
                    contentAlignment =
                        Alignment.Center,
                ) {
                    Icon(
                        imageVector =
                            Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint =
                            MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }

                Spacer(
                    Modifier.width(12.dp)
                )

                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {
                    Text(
                        text = transaction.title,
                        style =
                            AppTextStyles.bodyLg.copy(
                                fontWeight =
                                    FontWeight.SemiBold
                            ),
                        color =
                            MaterialTheme.colorScheme.onSurface,
                    )

                    Spacer(
                        Modifier.height(2.dp)
                    )

                    Text(
                        text = bankName,
                        style =
                            AppTextStyles.bodyMd,
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(
                Modifier.height(16.dp)
            )

            Text(
                text =
                    "${if (isExpense) "-" else "+"}${transaction.amount.toPersianCurrency()} تومان",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = accent,
            )
        }
    }
}

@Composable
private fun ReceiptActionCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Card(
        modifier =
            modifier
                .heightIn(min = 132.dp)
                .clickable(onClick = onClick),
        shape =
            MaterialTheme.shapes.large,
        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surfaceContainer,
            ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
            horizontalAlignment =
                Alignment.CenterHorizontally,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(46.dp)
                        .clip(
                            RoundedCornerShape(14.dp)
                        )
                        .background(
                            MaterialTheme.colorScheme.primaryContainer
                        ),
                contentAlignment =
                    Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint =
                        MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier =
                        Modifier.size(24.dp),
                )
            }

            Spacer(
                Modifier.height(10.dp)
            )

            Text(
                text = title,
                style =
                    AppTextStyles.bodyMd.copy(
                        fontWeight =
                            FontWeight.SemiBold
                    ),
                color =
                    MaterialTheme.colorScheme.onSurface,
                textAlign =
                    TextAlign.Center,
            )

            Spacer(
                Modifier.height(2.dp)
            )

            Text(
                text = subtitle,
                style =
                    AppTextStyles.labelMd,
                color =
                    MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign =
                    TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SectionTitle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
) {
    Row(
        modifier =
            Modifier.fillMaxWidth(),
        verticalAlignment =
            Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint =
                MaterialTheme.colorScheme.primary,
            modifier =
                Modifier.size(22.dp),
        )

        Spacer(
            Modifier.width(8.dp)
        )

        Column {
            Text(
                text = title,
                style =
                    AppTextStyles.bodyLg.copy(
                        fontWeight =
                            FontWeight.SemiBold
                    ),
                color =
                    MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = subtitle,
                style =
                    AppTextStyles.labelMd,
                color =
                    MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}






