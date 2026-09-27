package com.example.walletapp.sms

import com.example.walletapp.data.AccountLedger
import com.example.walletapp.data.JalaliCalendar
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Surface
import androidx.compose.material3.HorizontalDivider

import com.example.walletapp.sms.SmsLoanInstallmentChoice


import android.app.Activity

import android.content.Context

import android.content.Intent

import android.net.Uri

import android.os.Bundle

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
import androidx.compose.foundation.layout.offset

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

import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.ChevronLeft

import androidx.compose.material.icons.filled.Description

import androidx.compose.material.icons.filled.Label

import androidx.compose.material.icons.filled.ReceiptLong

import androidx.compose.material3.AlertDialog

import androidx.compose.material3.Button

import androidx.compose.material3.ButtonDefaults

import androidx.compose.material3.Card

import androidx.compose.material3.CardDefaults

import androidx.compose.material3.FilterChip
import androidx.compose.material3.Switch

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

import com.example.walletapp.data.LoanPaymentManager

import com.example.walletapp.ui.theme.AppTextStyles

import com.example.walletapp.ui.theme.WalletAppTheme

import java.io.File
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.filled.Edit
import androidx.compose.foundation.border
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.IconButton
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TrendingUp
private fun toPersianDigits(value: String): String =
    buildString {
        value.forEach { char ->
            when (char) {
                in '0'..'9' -> append(('۰'.code + char.code - '0'.code).toChar())
                else -> append(char)
            }
        }
    }

private fun unpaidInstallmentsFor(
    loan: LoanEntry,
): List<SmsLoanInstallmentChoice> {
    val paidNumbers = loan.payments.map { it.installmentNumber }.toSet()

    return (1..loan.totalInstallments)
        .filterNot { it in paidNumbers }
        .map { number ->
            SmsLoanInstallmentChoice(
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
@Composable
fun SmsTransactionCompletionBottomSheetContent(
    transaction: Transaction,
    detectedBank: String,
    sender: String?,
    initialCategory: String?,
    activeLoans: List<LoanEntry>,
    receiptUri: Uri?,
    onPickReceipt: () -> Unit,
    onTakePhoto: () -> Unit,
    onComplete: (
        selectedType: TransactionType,
        isTransfer: Boolean,
        destinationAccountId: String?,
        category: String?,
        tags: List<String>,
        description: String,
        receiptUri: Uri?,
        selectedBank: String,
        selectedAccountId: String?,
        learnBank: Boolean,
        loan: LoanEntry?,
        installment: SmsLoanInstallmentChoice?,
    ) -> Unit,
    onIgnoreSender: () -> Unit,
    onDismissAsNotBankTransaction: () -> Unit,
    onAddBankAccount: () -> Unit,
) {
    BackHandler(enabled = true) {}

    var selectedCategory by
        remember(initialCategory) {
            mutableStateOf(initialCategory)
        }

    var selectedSubcategory by remember(initialCategory) {
        mutableStateOf<String?>(null)
    }

    var selectedType by remember(transaction.id) {
        mutableStateOf(transaction.type)
    }

    var isTransfer by remember(transaction.id) {
        mutableStateOf(
            transaction.kind == com.example.walletapp.data.TransactionKind.TRANSFER
        )
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

    val metadata =
        remember(transaction.id) {
            SmsTransactionMetadataManager.get(transaction.id)
        }

    val smsAccountNumber =
        metadata
            ?.accountNumber
            ?.trim()
            ?.takeIf { it.isNotBlank() }

    val smsCardNumber =
        metadata
            ?.cardNumber
            ?.trim()
            ?.takeIf { it.isNotBlank() }

    val smsCardLast4 =
        smsCardNumber
            ?.filter { it.isDigit() }
            ?.takeLast(4)
            ?.takeIf { it.length == 4 }
            ?: metadata
                ?.cardLast4
                ?.trim()
                ?.takeIf { it.isNotBlank() }

    val matchingAccounts =
        remember(selectedBank) {
            AccountStore.accounts.filter { account ->
                account.isActive &&
                    account.bankName.trim() == selectedBank.trim()
            }
        }

    val availableAccounts =
        if (matchingAccounts.isNotEmpty()) {
            matchingAccounts
        } else {
            AccountStore.accounts.filter { it.isActive }
        }

    // A transfer destination may be any active account, not only an
    // account belonging to the detected source bank.
    val transferDestinationAccounts =
        AccountStore.accounts.filter { it.isActive }

    val accountNumberMatches =
        remember(selectedBank, smsAccountNumber) {
            if (smsAccountNumber == null) {
                emptyList()
            } else {
                matchingAccounts.filter { account ->
                    account.accountNumber.trim() == smsAccountNumber
                }
            }
        }

    val cardLast4Matches =
        remember(selectedBank, smsCardLast4) {
            if (smsCardLast4 == null) {
                emptyList()
            } else {
                matchingAccounts.filter { account ->
                    account.cardLast4.trim() == smsCardLast4
                }
            }
        }

    var selectedAccountId by remember(transaction.id, selectedBank) {
        mutableStateOf(
            transaction.accountId
                ?.takeIf { id ->
                    availableAccounts.any { it.id == id }
                }
                ?: accountNumberMatches.singleOrNull()?.id
                ?: cardLast4Matches.singleOrNull()?.id
                ?: availableAccounts.singleOrNull()?.id
        )
    }

    var showBankDialog by remember {
        mutableStateOf(false)
    }

    var showAccountDialog by remember {
        mutableStateOf(false)
    }

    var showDestinationAccountDialog by remember {
        mutableStateOf(false)
    }

    var destinationAccountId by remember(transaction.id) {
        mutableStateOf<String?>(null)
    }

    var showIgnoreDialog by remember {
        mutableStateOf(false)
    }

    var showLoanDialog by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showInstallmentDialog by remember { mutableStateOf(false) }
    var selectedLoan by remember { mutableStateOf<LoanEntry?>(null) }
    var selectedInstallment by remember { mutableStateOf<SmsLoanInstallmentChoice?>(null) }

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
        when {
            isTransfer -> emptyList()
            selectedType == TransactionType.EXPENSE ->
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

            else ->
                listOf(
                    "حقوق",
                    "هدیه",
                    "سرمایه‌گذاری",
                    "بازگشت وجه",
                    "سایر",
                )
        }

    val hasCompletionData =
        isTransfer ||
            selectedType != transaction.type ||
            selectedCategory != null ||
            tags.isNotEmpty() ||
            description.isNotBlank() ||
            receiptUri != null ||
            selectedBank != detectedBank

    val isInstallmentCategory = selectedCategory == "قسط"
    val unpaidInstallments =
        selectedLoan?.let(::unpaidInstallmentsFor).orEmpty()
    val canComplete =
        (hasCompletionData || transaction.requiresCompletion) &&
            availableAccounts.isNotEmpty() &&
            (availableAccounts.size == 1 || selectedAccountId != null) &&
            (!isTransfer || (destinationAccountId != null && destinationAccountId != selectedAccountId)) &&
            (!isInstallmentCategory ||
                (selectedLoan != null && selectedInstallment != null))

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(
                    androidx.compose.ui.graphics.Color(0xFFFFFFFF)
                ),
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(20.dp)
                    .padding(bottom = 100.dp)
                    .navigationBarsPadding(),
            verticalArrangement =
                Arrangement.spacedBy(24.dp),
        ) {

            // =================================================
            // HEADER
            // =================================================

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment =
                    Alignment.CenterVertically,
            ) {

                Text(
                    text = "ثبت تراکنش",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color =
                        androidx.compose.ui.graphics.Color(0xFF1A1A1A),
                )

                Icon(
                    imageVector =
                        androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint =
                        androidx.compose.ui.graphics.Color(0xFF7A7A7A),
                )
            }

            // =================================================
            // AMOUNT CARD
            // =================================================

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(
                            color =
                                androidx.compose.ui.graphics.Color(0xFFFDF2F2),
                            shape = RoundedCornerShape(20.dp),
                        )
                        .border(
                            width = 1.dp,
                            color =
                                androidx.compose.ui.graphics.Color(0xFFF8D7D7),
                            shape = RoundedCornerShape(20.dp),
                        )
                        .padding(16.dp),
                verticalArrangement =
                    Arrangement.spacedBy(16.dp),
            ) {

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(
                                androidx.compose.ui.graphics.Color.White,
                                RoundedCornerShape(12.dp),
                            )
                            .padding(4.dp),
                ) {

                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .background(
                                    if (selectedType == TransactionType.EXPENSE && !isTransfer)
                                        androidx.compose.ui.graphics.Color(0xFFEB1C24)
                                    else
                                        androidx.compose.ui.graphics.Color.Transparent,
                                    RoundedCornerShape(10.dp),
                                )
                                .clickable {
                                    isTransfer = false
                                    selectedType = TransactionType.EXPENSE
                                    selectedCategory = null
                                    selectedLoan = null
                                    selectedInstallment = null
                                }
                                .padding(10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "خرج",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color =
                                if (selectedType == TransactionType.EXPENSE && !isTransfer)
                                    androidx.compose.ui.graphics.Color.White
                                else
                                    androidx.compose.ui.graphics.Color(0xFF1A1A1A),
                        )
                    }

                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .background(
                                    if (selectedType == TransactionType.INCOME)
                                        androidx.compose.ui.graphics.Color(0xFFEB1C24)
                                    else
                                        androidx.compose.ui.graphics.Color.Transparent,
                                    RoundedCornerShape(10.dp),
                                )
                                .clickable {
                                    isTransfer = false
                                    selectedType = TransactionType.INCOME
                                    selectedCategory = null
                                    selectedLoan = null
                                    selectedInstallment = null
                                }
                                .padding(10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "دخل",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color =
                                if (selectedType == TransactionType.INCOME)
                                    androidx.compose.ui.graphics.Color.White
                                else
                                    androidx.compose.ui.graphics.Color(0xFF1A1A1A),
                        )
                    }
                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .background(
                                    if (isTransfer)
                                        androidx.compose.ui.graphics.Color(0xFFEB1C24)
                                    else
                                        androidx.compose.ui.graphics.Color.Transparent,
                                    RoundedCornerShape(10.dp),
                                )
                                .clickable {
                                    isTransfer = true
                                    selectedType = TransactionType.EXPENSE
                                    selectedCategory = null
                                    selectedLoan = null
                                    selectedInstallment = null
                                }
                                .padding(10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "انتقال",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color =
                                if (isTransfer)
                                    androidx.compose.ui.graphics.Color.White
                                else
                                    androidx.compose.ui.graphics.Color(0xFF1A1A1A),
                        )
                    }
                }

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(
                                androidx.compose.ui.graphics.Color(0xFFFCE8E8),
                                RoundedCornerShape(14.dp),
                            )
                            .padding(16.dp),
                    horizontalArrangement =
                        Arrangement.SpaceBetween,
                    verticalAlignment =
                        Alignment.CenterVertically,
                ) {

                    Text(
                        text = "ریال",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color =
                            androidx.compose.ui.graphics.Color(0xFF1A1A1A),
                    )

                    Text(
                        text =
                            String.format(
                                "%,d",
                                transaction.amount,
                            ).replace(
                                '0', '۰'
                            ).replace(
                                '1', '۱'
                            ).replace(
                                '2', '۲'
                            ).replace(
                                '3', '۳'
                            ).replace(
                                '4', '۴'
                            ).replace(
                                '5', '۵'
                            ).replace(
                                '6', '۶'
                            ).replace(
                                '7', '۷'
                            ).replace(
                                '8', '۸'
                            ).replace(
                                '9', '۹'
                            ),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color =
                            androidx.compose.ui.graphics.Color(0xFFEB1C24),
                        textAlign = TextAlign.End,
                        modifier =
                            Modifier
                                .weight(1f),
                    )
                }

                Text(
                    text =
                        "${(transaction.amount / 10).toString().replace(
                            Regex("(\\d)(?=(\\d{3})+$)"),
                            "$1,"
                        )} هزار تومان"
                            .replace("0", "۰")
                            .replace("1", "۱")
                            .replace("2", "۲")
                            .replace("3", "۳")
                            .replace("4", "۴")
                            .replace("5", "۵")
                            .replace("6", "۶")
                            .replace("7", "۷")
                            .replace("8", "۸")
                            .replace("9", "۹"),
                    fontSize = 12.sp,
                    color =
                        androidx.compose.ui.graphics.Color(0xFF1A1A1A),
                    modifier =
                        Modifier.offset(y = (-8).dp),
                )
            }

            // =================================================
            // BANK SUMMARY / ACTIONS
            // =================================================

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = androidx.compose.ui.graphics.Color(0xFFF7F8FA)
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(androidx.compose.ui.graphics.Color(0xFFE8F5E9)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = androidx.compose.ui.graphics.Color(0xFF157A37),
                                modifier = Modifier.size(24.dp),
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (selectedBank == BankSmsParser.UNKNOWN_BANK)
                                    "بانک تشخیص داده نشد"
                                else
                                    selectedBank,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = androidx.compose.ui.graphics.Color(0xFF1A1A1A),
                            )
                            Text(
                                text = if (selectedBank == BankSmsParser.UNKNOWN_BANK)
                                    "برای اصلاح، روی نام بانک بزنید"
                                else
                                    "بانک تشخیص‌داده‌شده پیامک",
                                fontSize = 11.sp,
                                color = androidx.compose.ui.graphics.Color(0xFF6A6A6A),
                            )
                        }

                        TextButton(onClick = { showBankDialog = true }) {
                            Text(
                                text = "تغییر",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = androidx.compose.ui.graphics.Color(0xFFE8F5E9)
                            ),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { learnBank = !learnBank }
                                    .padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = androidx.compose.ui.graphics.Color(0xFF157A37),
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "یادگیری بانک",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = androidx.compose.ui.graphics.Color(0xFF157A37),
                                )
                                Spacer(Modifier.width(6.dp))
                                Switch(
                                    checked = learnBank,
                                    onCheckedChange = { learnBank = it },
                                )
                            }
                        }

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .clickable { showIgnoreDialog = true },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = androidx.compose.ui.graphics.Color(0xFFFFF3F3)
                            ),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Block,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = androidx.compose.ui.graphics.Color(0xFFB42318),
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "این تراکنش بانکی نیست",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = androidx.compose.ui.graphics.Color(0xFFB42318),
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }

            // =================================================
            if (isTransfer) {
                    SectionTitle(
                        icon = Icons.Default.SwapHoriz,
                        title = "حساب مقصد",
                        subtitle = "حسابی که مبلغ به آن منتقل شده است",
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDestinationAccountDialog = true },
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Default.AccountBalance, null)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                AccountStore.accounts.firstOrNull { it.id == destinationAccountId }?.name
                                    ?: "انتخاب حساب مقصد",
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }

            // CATEGORY
            // =================================================

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "دسته بندی",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.ui.graphics.Color(0xFF1A1A1A),
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCategoryPicker = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = androidx.compose.ui.graphics.Color(0xFFFCE8E8),
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "انتخاب دسته بندی",
                            modifier = Modifier.size(20.dp),
                            tint = androidx.compose.ui.graphics.Color(0xFFEB1C24),
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = selectedCategory ?: "تعیین نشده",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = androidx.compose.ui.graphics.Color(0xFFEB1C24),
                                )
                                if (isInstallmentCategory && selectedLoan != null) {
                                    Text(
                                        text = selectedLoan!!.title +
                                            (selectedInstallment?.let { " — قسط ${it.number}" } ?: ""),
                                        fontSize = 11.sp,
                                        color = androidx.compose.ui.graphics.Color(0xFF7A7A7A),
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = androidx.compose.ui.graphics.Color(0xFFEB1C24),
                            )
                        }
                    }
                }
            }

            // DESCRIPTION
            // =================================================

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(
                            androidx.compose.ui.graphics.Color(0xFFF0F0F0),
                            RoundedCornerShape(12.dp),
                        )
                        .padding(
                            horizontal = 16.dp,
                            vertical = 12.dp,
                        ),
                verticalAlignment =
                    Alignment.CenterVertically,
            ) {

                BasicTextField(
                    value = description,
                    onValueChange = {
                        description = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle =
                        androidx.compose.ui.text.TextStyle(
                            fontSize = 14.sp,
                            color =
                                androidx.compose.ui.graphics.Color(0xFF1A1A1A),
                        ),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        if (description.isBlank()) {
                            Text(
                                text = "توضیحات",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color =
                                    androidx.compose.ui.graphics.Color(0xFF7A7A7A),
                            )
                        }
                        innerTextField()
                    },
                )
            }

            // =================================================
            // DATE / TIME
            // =================================================

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(10.dp),
            ) {

                val formattedTime =
                    toPersianDigits(
                        java.text.SimpleDateFormat(
                            "HH:mm",
                            java.util.Locale.US,
                        ).format(
                            java.util.Date(
                                transaction.dateEpochMillis
                                    ?: System.currentTimeMillis()
                            )
                        )
                    )

                val formattedDate =
                    transaction.dateEpochMillis
                        ?.let { JalaliCalendar.fromEpochMillisSlashed(it) }
                        ?.let(::toPersianDigits)
                        ?: "—"

                Row(
                    modifier =
                        Modifier
                            .weight(1f)
                            .background(
                                androidx.compose.ui.graphics.Color(0xFFF0F0F0),
                                RoundedCornerShape(12.dp),
                            )
                            .padding(
                                horizontal = 16.dp,
                                vertical = 12.dp,
                            ),
                    horizontalArrangement =
                        Arrangement.SpaceBetween,
                    verticalAlignment =
                        Alignment.CenterVertically,
                ) {

                    Text(
                        text = formattedTime,
                        fontSize = 14.sp,
                        color =
                            androidx.compose.ui.graphics.Color(0xFF1A1A1A),
                    )

                    Icon(
                        imageVector =
                            androidx.compose.material.icons.Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint =
                            androidx.compose.ui.graphics.Color(0xFF157A37),
                    )
                }

                Row(
                    modifier =
                        Modifier
                            .weight(1f)
                            .background(
                                androidx.compose.ui.graphics.Color(0xFFF0F0F0),
                                RoundedCornerShape(12.dp),
                            )
                            .padding(
                                horizontal = 16.dp,
                                vertical = 12.dp,
                            ),
                    horizontalArrangement =
                        Arrangement.SpaceBetween,
                    verticalAlignment =
                        Alignment.CenterVertically,
                ) {

                    Text(
                        text = formattedDate,
                        fontSize = 14.sp,
                        color =
                            androidx.compose.ui.graphics.Color(0xFF1A1A1A),
                    )

                    Icon(
                        imageVector =
                            androidx.compose.material.icons.Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint =
                            androidx.compose.ui.graphics.Color(0xFF157A37),
                    )
                }
            }

            // =================================================
            // RECEIPT ACTIONS
            // =================================================

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement =
                    Arrangement.spacedBy(10.dp),
            ) {

                Text(
                    text = "رسید تراکنش",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color =
                        androidx.compose.ui.graphics.Color(0xFF1A1A1A),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp),
                ) {

                    ReceiptActionCard(
                        modifier = Modifier.weight(1f),
                        icon =
                            androidx.compose.material.icons.Icons.Default.CameraAlt,
                        title = "دوربین",
                        subtitle =
                            if (receiptUri != null)
                                "رسید انتخاب شده"
                            else
                                "گرفتن عکس",
                        onClick = onTakePhoto,
                    )

                    ReceiptActionCard(
                        modifier = Modifier.weight(1f),
                        icon =
                            androidx.compose.material.icons.Icons.Default.AddPhotoAlternate,
                        title = "گالری",
                        subtitle =
                            if (receiptUri != null)
                                "انتخاب تصویر"
                            else
                                "انتخاب از گالری",
                        onClick = onPickReceipt,
                    )
                }
            }
            // ACCOUNTS
            // =================================================

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement =
                    Arrangement.spacedBy(8.dp),
            ) {

                Text(
                    text = "حساب",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color =
                        androidx.compose.ui.graphics.Color(0xFF1A1A1A),
                )

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .horizontalScroll(
                                rememberScrollState()
                            ),
                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp),
                ) {

                    availableAccounts.forEach { account ->

                        Column(
                            modifier =
                                Modifier
                                    .width(150.dp)
                                    .background(
                                        brush =
                                            androidx.compose.ui.graphics.Brush.linearGradient(
                                                listOf(
                                                    androidx.compose.ui.graphics.Color.White,
                                                    androidx.compose.ui.graphics.Color(0xFFF0F0F0),
                                                )
                                            ),
                                        shape =
                                            RoundedCornerShape(12.dp),
                                    )
                                    .border(
                                        1.dp,
                                        androidx.compose.ui.graphics.Color(0xFFDCDCDC),
                                        RoundedCornerShape(12.dp),
                                    )
                                    .clickable {
                                        selectedAccountId = account.id
                                        if (
                                            selectedBank == BankSmsParser.UNKNOWN_BANK ||
                                            matchingAccounts.none { it.id == account.id }
                                        ) {
                                            selectedBank = account.bankName
                                        }
                                    }
                                    .padding(12.dp),
                            verticalArrangement =
                                Arrangement.spacedBy(4.dp),
                        ) {

                            Box(
                                modifier =
                                    Modifier
                                        .width(22.dp)
                                        .height(14.dp)
                                        .background(
                                            androidx.compose.ui.graphics.Color(0xFFF5C542),
                                            RoundedCornerShape(4.dp),
                                        )
                            )

                            Text(
                                text = "موجودی",
                                fontSize = 11.sp,
                                color =
                                    androidx.compose.ui.graphics.Color(0xFF7A7A7A),
                            )

                            Text(
                                text =
                                    toPersianDigits(
                                        AccountLedger.balanceOf(account.id)
                                            .toString()
                                            .replace(
                                                Regex("(\\d)(?=(\\d{3})+$)"),
                                                "$1,"
                                            )
                                    ),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color =
                                    androidx.compose.ui.graphics.Color(0xFF1A1A1A),
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth(),
                            )

                            Box(
                                modifier =
                                    Modifier
                                        .align(Alignment.Start)
                                        .size(24.dp)
                                        .background(
                                            androidx.compose.ui.graphics.Color(0xFFE8F5E9),
                                            CircleShape,
                                        ),
                                contentAlignment =
                                    Alignment.Center,
                            ) {
                                Icon(
                                    imageVector =
                                        androidx.compose.material.icons.Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint =
                                        androidx.compose.ui.graphics.Color(0xFF157A37),
                                )
                            }
                        }
                    }

                    Column(
                        modifier =
                            Modifier
                                .width(120.dp)
                                .padding(
                                    horizontal = 8.dp,
                                    vertical = 12.dp,
                                )
                                .border(
                                    2.dp,
                                    androidx.compose.ui.graphics.Color(0xFFB0B0B0),
                                    RoundedCornerShape(12.dp),
                                )
                                .clickable {
                                    onAddBankAccount()
                                },
                        horizontalAlignment =
                            Alignment.CenterHorizontally,
                        verticalArrangement =
                            Arrangement.Center,
                    ) {

                        Box(
                            modifier =
                                Modifier
                                    .size(30.dp)
                                    .background(
                                        androidx.compose.ui.graphics.Color(0xFFE8F5E9),
                                        CircleShape,
                                    ),
                            contentAlignment =
                                Alignment.Center,
                        ) {
                            Text(
                                text = "+",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color =
                                    androidx.compose.ui.graphics.Color(0xFF157A37),
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "افزودن حساب",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color =
                                androidx.compose.ui.graphics.Color(0xFF7A7A7A),
                        )
                    }
                }
            }

            // =================================================
            // =================================================
            // SMS DETAILS
            // =================================================

            if (smsCardNumber != null || smsCardLast4 != null || metadata?.balance != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = androidx.compose.ui.graphics.Color(0xFFF7F7F7)
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "اطلاعات پیامک بانکی",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color(0xFF1A1A1A),
                        )

                        if (smsCardNumber != null || smsCardLast4 != null) {
                            val displayCard = smsCardNumber ?: ("****" + smsCardLast4)
                            Text(
                                text = "کارت: " + displayCard,
                                fontSize = 13.sp,
                                color = androidx.compose.ui.graphics.Color(0xFF555555),
                            )
                        }

                        metadata?.balance?.let { smsBalance ->
                            Text(
                                text = "موجودی پیامک: " + toPersianDigits(smsBalance.toString().replace(Regex("(\\\\d)(?=(\\\\d{3})+$)"), "$1,")) + " تومان",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = androidx.compose.ui.graphics.Color(0xFF157A37),
                            )
                        }
                    }
                }
            }
            // =================================================
            // TAGS
            // =================================================

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement =
                    Arrangement.spacedBy(8.dp),
            ) {

                Text(
                    text = "برچسب‌ها",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color =
                        androidx.compose.ui.graphics.Color(0xFF1A1A1A),
                )

                Row(
                    modifier =
                        Modifier
                            .background(
                                androidx.compose.ui.graphics.Color(0xFFE8F5E9),
                                RoundedCornerShape(12.dp),
                            )
                            .clickable {
                                if (tagInput.isNotBlank()) {
                                    tags = tags + tagInput.trim()
                                    tagInput = ""
                                } else {
                                    tagInput = "جدید"
                                }
                            }
                            .padding(
                                horizontal = 16.dp,
                                vertical = 6.dp,
                            ),
                    horizontalArrangement =
                        Arrangement.spacedBy(5.dp),
                    verticalAlignment =
                        Alignment.CenterVertically,
                ) {

                    Text(
                        text = "+",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color =
                            androidx.compose.ui.graphics.Color(0xFF157A37),
                    )

                    Text(
                        text = "افزودن برچسب",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color =
                            androidx.compose.ui.graphics.Color(0xFF157A37),
                    )
                }
            }

            // =================================================
            // DETAILS
            // =================================================

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(
                            androidx.compose.ui.graphics.Color(0xFFF0F0F0),
                            RoundedCornerShape(16.dp),
                        )
                        .padding(16.dp),
            ) {

                Icon(
                    imageVector =
                        androidx.compose.material.icons.Icons.Default.Info,
                    contentDescription = null,
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .size(18.dp),
                    tint =
                        androidx.compose.ui.graphics.Color(0xFF1A1A1A),
                )

                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(0.dp),
                ) {

                    Text(
                        text =
    if (selectedBank == BankSmsParser.UNKNOWN_BANK) {
        "بانک: تشخیص داده نشد"
    } else {
        "بانک: $selectedBank"
    },
                        fontSize = 13.sp,
                        color =
                            androidx.compose.ui.graphics.Color(0xFF1A1A1A),
                    )

                    Text(
                        text =
                            "کارت: ${
                                smsCardNumber
                                    ?: smsCardLast4
                                    ?: "—"
                            }",
                        fontSize = 13.sp,
                        color =
                            androidx.compose.ui.graphics.Color(0xFF1A1A1A),
                    )

                    Text(
                        text =
                            "برداشت: ${
                                transaction.amount
                            } ریال",
                        fontSize = 13.sp,
                        color =
                            androidx.compose.ui.graphics.Color(0xFF1A1A1A),
                    )

                    Text(
                        text =
                            "موجودی: ${
                                (
                                    selectedAccountId
                                        ?: matchingAccounts.singleOrNull()?.id
                                )?.let { AccountLedger.balanceOf(it) }
                                    ?: "—"
                            }",
                        fontSize = 13.sp,
                        color =
                            androidx.compose.ui.graphics.Color(0xFF1A1A1A),
                    )

                    Text(
                        text =
    "تاریخ: ${
        transaction.dateEpochMillis
            ?.let { JalaliCalendar.fromEpochMillisSlashed(it) }
            ?: "—"
    }",
                        fontSize = 13.sp,
                        color =
                            androidx.compose.ui.graphics.Color(0xFF1A1A1A),
                    )

                    Text(
                        text =
    "زمان: ${transaction.dateEpochMillis?.let { toPersianDigits(java.text.SimpleDateFormat("HH:mm", java.util.Locale.US).format(java.util.Date(it))) } ?: "—"}",
                        fontSize = 13.sp,
                        color =
                            androidx.compose.ui.graphics.Color(0xFF1A1A1A),
                    )

                    Text(
                        text = "فرستنده: ${sender ?: "—"}",
                        fontSize = 13.sp,
                        color =
                            androidx.compose.ui.graphics.Color(0xFF1A1A1A),
                    )
                }
            }
        }

        // =====================================================
        // STICKY FOOTER
        // =====================================================

        Column(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        androidx.compose.ui.graphics.Color.White,
                    )
                    .border(
                        width = 1.dp,
                        color =
                            androidx.compose.ui.graphics.Color(0xFFF0F0F0),
                    )
                    .padding(
                        horizontal = 20.dp,
                        vertical = 16.dp,
                    ),
        ) {

            Button(
                onClick = {

                    if (isInstallmentCategory &&
                        selectedLoan != null &&
                        selectedInstallment != null
                    ) {
                        onComplete(
                            selectedType,
                            isTransfer,
                            destinationAccountId,
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
                    } else if (!isInstallmentCategory) {
                        onComplete(
                            selectedType,
                            isTransfer,
                            destinationAccountId,
                            selectedCategory,
                            tags,
                            description,
                            receiptUri,
                            selectedBank,
                            selectedAccountId,
                            learnBank,
                            null,
                            null,
                        )
                    }
                },
                enabled = canComplete,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                shape =
                    RoundedCornerShape(14.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            androidx.compose.ui.graphics.Color(0xFF157A37),
                        disabledContainerColor =
                            androidx.compose.ui.graphics.Color(0xFFE0E0E0),
                    ),
            ) {

                Text(
                    text = "ذخیره تراکنش",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.ui.graphics.Color.White,
                )
            }
            TextButton(
                onClick = onDismissAsNotBankTransaction,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "تراکنش بانکی نیست",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
            }


        }
    }
    if (showCategoryPicker) {
        CategoryPickerDialog(
            selectedType = selectedType,
            selectedCategory = selectedCategory,
            activeLoans = activeLoans,
            selectedLoan = selectedLoan,
            selectedInstallment = selectedInstallment,
            onDismiss = { showCategoryPicker = false },
            onCategorySelected = { category, subcategory ->
                selectedCategory = category
                selectedSubcategory = subcategory
                if (category != "قسط") {
                    selectedLoan = null
                    selectedInstallment = null
                } else {
                    selectedLoan = null
                    selectedInstallment = null
                    showCategoryPicker = false
                    showLoanDialog = true
                }
            },
        )
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
                    if (activeLoans.isEmpty()) {
                        Text(
                            text = "هیچ وام فعالی برای انتخاب وجود ندارد.",
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        activeLoans.forEach { loan ->
                            val paidCount = loan.payments
                                .map { it.installmentNumber }
                                .filter { it in 1..loan.totalInstallments }
                                .distinct()
                                .size
                            val remainingCount = (loan.totalInstallments - paidCount).coerceAtLeast(0)

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedLoan = loan
                                        selectedInstallment = null
                                        showLoanDialog = false
                                        showInstallmentDialog = true
                                    },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                ),
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = loan.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedLoan?.id == loan.id) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        },
                                    )
                                    Spacer(Modifier.height(5.dp))
                                    Text(
                                        text = "نوع وام: " + loan.type.label,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = "اقساط: " + loan.totalInstallments +
                                            "  •  پرداخت‌شده: " + paidCount +
                                            "  •  باقی‌مانده: " + remainingCount,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
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

    if (showDestinationAccountDialog) {
        AlertDialog(
            onDismissRequest = {
                showDestinationAccountDialog = false
            },
            title = {
                Text("انتخاب حساب مقصد")
            },
            text = {
                Column {
                    transferDestinationAccounts
                        .filter { it.id != selectedAccountId }
                        .forEach { account ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        destinationAccountId = account.id
                                        showDestinationAccountDialog = false
                                    }
                                    .padding(
                                        vertical = 12.dp,
                                        horizontal = 8.dp,
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = destinationAccountId == account.id,
                                    onClick = {
                                        destinationAccountId = account.id
                                        showDestinationAccountDialog = false
                                    },
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = account.name,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showDestinationAccountDialog = false
                }) {
                    Text("بستن")
                }
            },
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
                    availableAccounts.forEach { account ->
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
                Text("این تراکنش بانکی نیست")
            },
            text = {
                Text(
                    "پیامک‌های مشابه این تراکنش از این فرستنده در آینده نادیده گرفته می‌شوند."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showIgnoreDialog = false
                        onIgnoreSender()
                    }
                ) {
                    Text("نادیده گرفتن")
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

data class CategoryPickerItem(
    val name: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val accent: Color,
    val subs: List<String>,
)

@Composable
private fun CategoryPickerDialog(
    selectedType: TransactionType,
    selectedCategory: String?,
    activeLoans: List<LoanEntry>,
    selectedLoan: LoanEntry?,
    selectedInstallment: SmsLoanInstallmentChoice?,
    onDismiss: () -> Unit,
    onCategorySelected: (String, String?) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var expandedId by remember { mutableStateOf<String?>(selectedCategory) }

    val expenseItems = listOf(
        CategoryPickerItem("غذا", Icons.Default.Fastfood, Color(0xFF2E9D57), listOf("رستوران", "سوپرمارکت", "کافه", "میوه و سبزیجات")),
        CategoryPickerItem("حمل‌ونقل", Icons.Default.DirectionsCar, Color(0xFF3578C4), listOf("بنزین", "تعمیرات", "بیمه", "پارکینگ")),
        CategoryPickerItem("قبض", Icons.Default.Description, Color(0xFF20A7A7), listOf("برق", "آب", "گاز", "اینترنت")),
        CategoryPickerItem("قسط", Icons.Default.Payments, Color(0xFFE58A21), listOf("وام مسکن", "وام خودرو", "وام قرض‌الحسنه", "بازپرداخت اقساط")),
        CategoryPickerItem("خرید", Icons.Default.ShoppingBag, Color(0xFFD36A9A), listOf("پوشاک", "کفش", "لوازم خانه", "خرید روزمره")),
        CategoryPickerItem("سلامت", Icons.Default.LocalHospital, Color(0xFFD84C4C), listOf("دارو", "دکتر", "آزمایش", "بیمارستان")),
        CategoryPickerItem("تفریح", Icons.Default.SportsEsports, Color(0xFF5A8FD8), listOf("سینما", "بازی", "سفر", "سرگرمی")),
        CategoryPickerItem("سایر", Icons.Default.Category, Color(0xFF777777), emptyList()),
    )
    val incomeItems = listOf(
        CategoryPickerItem("حقوق", Icons.Default.AttachMoney, Color(0xFF2E9D57), listOf("حقوق ماهانه", "دستمزد", "پاداش")),
        CategoryPickerItem("هدیه", Icons.Default.CardGiftcard, Color(0xFFD36A9A), listOf("هدیه نقدی", "هدیه غیرنقدی")),
        CategoryPickerItem("سرمایه‌گذاری", Icons.Default.TrendingUp, Color(0xFFD4A62A), listOf("سود", "سهام", "سپرده بانکی")),
        CategoryPickerItem("بازگشت وجه", Icons.Default.Replay, Color(0xFF5A8FD8), listOf("استرداد", "برگشت خرید")),
        CategoryPickerItem("سایر", Icons.Default.Category, Color(0xFF777777), emptyList()),
    )

    val items = if (selectedType == TransactionType.EXPENSE) expenseItems else incomeItems
    val normalizedQuery = query.trim()
    val filtered = items.filter { item ->
        normalizedQuery.isBlank() ||
            item.name.contains(normalizedQuery, ignoreCase = true) ||
            item.subs.any { it.contains(normalizedQuery, ignoreCase = true) }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 760.dp),
            shape = RoundedCornerShape(26.dp),
            tonalElevation = 5.dp,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1B7A43))
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.Sort, "مرتب‌سازی", tint = Color.White)
                        }
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.Add, "افزودن دسته", tint = Color.White)
                        }
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.Download, "دانلود", tint = Color.White)
                        }
                    }
                    Text(
                        text = "انتخاب دسته بندی",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.ChevronLeft, "بازگشت", tint = Color.White)
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    if (filtered.isEmpty()) {
                        Text(
                            text = "دسته یا زیردسته‌ای پیدا نشد",
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    filtered.forEach { item ->
                        val isExpanded = expandedId == item.name
                        val matchesSub = normalizedQuery.isNotBlank() &&
                            item.subs.any { it.contains(normalizedQuery, ignoreCase = true) }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(15.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            ),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        expandedId = if (isExpanded) null else item.name
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(item.accent.copy(alpha = 0.14f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(item.icon, contentDescription = null, tint = item.accent)
                                }

                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = item.name,
                                    modifier = Modifier.weight(1f),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )

                                Icon(
                                    imageVector = if (isExpanded || matchesSub) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            AnimatedVisibility(
                                visible = isExpanded || matchesSub,
                                enter = expandVertically(),
                                exit = shrinkVertically(),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 66.dp, end = 12.dp, bottom = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(5.dp),
                                ) {
                                    item.subs.forEach { sub ->
                                        val subMatches = normalizedQuery.isBlank() ||
                                            sub.contains(normalizedQuery, ignoreCase = true)
                                        if (subMatches) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                     .clickable {
                                                        onCategorySelected(item.name, sub)
                                                    }
                                                    .padding(vertical = 9.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                                        .background(item.accent),
                                                )
                                                Spacer(Modifier.width(10.dp))
                                                Text(text = sub, fontSize = 13.sp)
                                            }
                                        }
                                    }

                                    TextButton(
                                        onClick = { onCategorySelected(item.name, null) },
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Text("انتخاب «${item.name}»")
                                    }
                                }
                            }
                        }
                    }
                }

                Surface(
                    color = Color(0xFFC8E6C9),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "جستجو", tint = Color(0xFF1B7A43))
                        Spacer(Modifier.width(8.dp))
                        BasicTextField(
                            value = query,
                            onValueChange = { query = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                            ),
                            decorationBox = { innerTextField ->
                                if (query.isBlank()) {
                                    Text(
                                        text = "جستجوی دسته یا زیردسته...",
                                        color = Color(0xFF5B765F),
                                        fontSize = 13.sp,
                                    )
                                }
                                innerTextField()
                            },
                        )
                    }
                }
            }
        }
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


































