package com.example.walletapp.ui.screens

import com.example.walletapp.ui.components.AppTextButton

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.example.walletapp.sms.SmsIgnoreManager
import com.example.walletapp.ui.components.AppCard
import com.example.walletapp.ui.components.AppOutlinedButton
import com.example.walletapp.ui.components.AppPrimaryButton
import com.example.walletapp.ui.components.AppTextField
import com.example.walletapp.sms.SmsLearningManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsLearningScreen(
    onBack: () -> Unit,
) {
    val context = LocalContext.current

    var refreshKey by remember { mutableStateOf(0) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showManualLearningDialog by remember { mutableStateOf(false) }

    SmsLearningManager.attachContext(context)
    SmsIgnoreManager.attachContext(context)

    val learnedBanks = SmsLearningManager.getAllSenderBanks()
    val learnedPatterns = SmsLearningManager.getAllPatternRules()
    val ignoredPatterns = SmsIgnoreManager.getIgnoredPatterns()
    val ignoredSenders = SmsIgnoreManager.getIgnoredSenders()

    fun refresh() {
        refreshKey++
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "🧠 یادگیری پیامک‌ها",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Filled.ArrowForward,
                            contentDescription = "بازگشت"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {
                AppPrimaryButton(
                    text = "یادگیری دستی پیامک",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        showManualLearningDialog = true
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = null
                        )
                    },
                )
            }

            item {
                LearningSummaryCard(
                    bankCount = learnedBanks.size,
                    patternCount = learnedPatterns.size,
                    ignoredPatternCount = ignoredPatterns.size,
                    ignoredSenderCount = ignoredSenders.size
                )
            }

            item {
                Text(
                    "بانک‌های یادگرفته‌شده",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (learnedBanks.isEmpty()) {
                item {
                    Text(
                        "هنوز بانک یا فرستنده‌ای به‌صورت شخصی یاد گرفته نشده است.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                items(
                    learnedBanks.entries.toList(),
                    key = { it.key }
                ) { entry ->
                    LearnedBankRow(
                        sender = entry.key,
                        bankName = entry.value
                    )
                }
            }

            item {
                Spacer(Modifier.height(8.dp))

                Text(
                    "الگوهای تراکنشی",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (learnedPatterns.isEmpty()) {
                item {
                    Text(
                        "هنوز الگوی تراکنشی ذخیره نشده است.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                items(
                    learnedPatterns,
                    key = {
                        "${it.sender}|${it.pattern}"
                    }
                ) { rule ->

                    LearnedPatternCard(
                        sender = rule.sender,
                        pattern = rule.pattern,
                        bankName = rule.bankName,
                        category = rule.category,
                        transactionType = rule.transactionType,
                        onDelete = {
                            SmsLearningManager.removePatternRule(
                                sender = rule.sender,
                                pattern = rule.pattern
                            )
                            refresh()
                        }
                    )
                }
            }

            item {
                Spacer(Modifier.height(8.dp))

                Text(
                    "الگوهای نادیده‌گرفته‌شده",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (ignoredPatterns.isEmpty()) {
                item {
                    Text(
                        "الگوی نادیده‌گرفته‌شده‌ای وجود ندارد.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                items(
                    ignoredPatterns,
                    key = {
                        "${it.sender}|${it.pattern}"
                    }
                ) { rule ->

                    IgnoredPatternCard(
                        sender = rule.sender,
                        pattern = rule.pattern,
                        onDelete = {
                            SmsIgnoreManager.unignorePattern(
                                sender = rule.sender,
                                pattern = rule.pattern
                            )
                            refresh()
                        }
                    )
                }
            }

            item {
                Spacer(Modifier.height(8.dp))

                Text(
                    "فرستنده‌های کاملاً نادیده‌گرفته‌شده: ${ignoredSenders.size}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            item {
                AppOutlinedButton(
                    text = "پاک کردن یادگیری",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        showClearDialog = true
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = null
                        )
                    },
                )
            }

            item {
                Text(
                    "تمام این اطلاعات فقط روی همین گوشی ذخیره می‌شود و متن پیامک‌ها به سرور یا هوش مصنوعی ارسال نمی‌شود.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }

    if (showManualLearningDialog) {
        ManualLearningDialog(
            onDismiss = {
                showManualLearningDialog = false
            },
            onSaved = {
                refresh()
                showManualLearningDialog = false
            }
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = {
                showClearDialog = false
            },
            title = {
                Text("پاک کردن یادگیری")
            },
            text = {
                Text(
                    "تمام بانک‌ها، الگوهای تراکنشی، دسته‌بندی‌های یادگرفته‌شده و پیام‌های نادیده‌گرفته‌شده حذف شوند؟"
                )
            },
            confirmButton = {
                AppPrimaryButton(
                    text = "بله، پاک کن",
                    onClick = {
                        SmsLearningManager.clearAllLearning()
                        SmsIgnoreManager.clearAllIgnored()

                        showClearDialog = false
                        refresh()
                    }
                )
            },
            dismissButton = {
                AppTextButton(
                    onClick = {
                        showClearDialog = false
                    }
                ) {
                    Text("انصراف")
                }
            }
        )
    }

    @Suppress("UNUSED_VARIABLE")
    val unused = refreshKey
}

@Composable
private fun ManualLearningDialog(
    onDismiss: () -> Unit,
    onSaved: () -> Unit,
) {
    var bankName by remember { mutableStateOf("") }
    var sender by remember { mutableStateOf("") }
    var messageText by remember { mutableStateOf("") }
    var transactionType by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text(
                text = "🧠 آموزش پیامک بانکی",
                fontWeight = FontWeight.Bold
            )
        },

        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // =========================================================
                // توضیح کلی
                // =========================================================

                item {
                    AppCard(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                        Column(
                            modifier = Modifier.padding(14.dp)
                        ) {

                            Text(
                                text = "هدف این بخش چیست؟",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )

                            Spacer(
                                modifier = Modifier.height(6.dp)
                            )

                            Text(
                                text =
                                    "با وارد کردن یک نمونه واقعی از پیامک بانکی، " +
                                    "به برنامه یاد می‌دهید پیامک‌های مشابه را " +
                                    "در آینده بهتر شناسایی و ثبت کند.",
                                style = MaterialTheme.typography.bodySmall
                            )

                            Spacer(
                                modifier = Modifier.height(6.dp)
                            )

                            Text(
                                text =
                                    "لازم نیست مبلغ، تاریخ، ساعت یا شماره حساب را " +
                                    "جداگانه وارد کنید؛ نمونه کامل پیامک کافی است.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // =========================================================
                // 1 - بانک
                // =========================================================

                item {
                    Text(
                        text = "۱. 🏦 نام بانک",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text =
                            "نام بانکی که این پیامک مربوط به آن است را وارد کنید.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    AppTextField(
                        value = bankName,
                        onValueChange = {
                            bankName = it
                            errorMessage = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = "نام بانک",
                        placeholder = {
                            Text("مثال: بانک شهر")
                        },
                        supportingText = {
                            Text(
                                "مثال‌های دیگر: بلو، بانک ملی، ملت، رسالت"
                            )
                        },
                        singleLine = true
                    )
                }

                // =========================================================
                // 2 - شماره سامانه
                // =========================================================

                item {
                    Text(
                        text = "۲. 📱 شماره سامانه / فرستنده",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text =
                            "شماره یا نامی را وارد کنید که پیامک از طرف آن " +
                                    "دریافت شده است.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    AppTextField(
                        value = sender,
                        onValueChange = {
                            sender = it
                            errorMessage = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = "شماره سامانه یا نام فرستنده",
                        placeholder = {
                            Text("+989999987641")
                        },
                        supportingText = {
                            Text(
                                "مثال: +989999987641 یا Bank Shahr"
                            )
                        },
                        singleLine = true
                    )
                }

                // =========================================================
                // 3 - نمونه پیامک
                // =========================================================

                item {
                    Text(
                        text = "۳. 📨 نمونه واقعی پیامک",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text =
                            "یک پیامک واقعی و کامل را دقیقاً همان‌طور که " +
                                    "دریافت کرده‌اید کپی کنید.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    AppCard(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                        Column(
                            modifier = Modifier.padding(10.dp)
                        ) {
                            Text(
                                text = "نمونه:",
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(
                                modifier = Modifier.height(4.dp)
                            )

                            Text(
                                text =
                                    "واریز مبلغ 5,500,000 ریال به حساب شما " +
                                            "انجام شد. موجودی: 12,300,000 ریال",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    AppTextField(
                        value = messageText,
                        onValueChange = {
                            messageText = it
                            errorMessage = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = "متن کامل پیامک",
                        placeholder = {
                            Text(
                                "اینجا متن کامل پیامک را قرار دهید..."
                            )
                        },
                        supportingText = {
                            Text(
                                "مبلغ، تاریخ، ساعت، موجودی و شماره حساب را " +
                                        "حذف نکنید؛ متن کامل را وارد کنید."
                            )
                        },
                        minLines = 6,
                        maxLines = 10
                    )
                }

                // =========================================================
                // 4 - نوع تراکنش
                // =========================================================

                item {
                    Text(
                        text = "۴. 💰 نوع تراکنش",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text =
                            "مشخص کنید این پیامک به معنی ورود پول به حساب " +
                                    "است یا خروج پول از حساب.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    if (transactionType == "INCOME") {
                        AppPrimaryButton(
                            text = "🟢 واریز به حساب / درآمد",
                            onClick = {
                                transactionType = "INCOME"
                                errorMessage = ""
                            }
                        )
                    } else {
                        AppOutlinedButton(
                            text = "🟢 واریز به حساب / درآمد",
                            onClick = {
                                transactionType = "INCOME"
                                errorMessage = ""
                            }
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    if (transactionType == "EXPENSE") {
                        AppPrimaryButton(
                            text = "🔴 برداشت از حساب / هزینه",
                            onClick = {
                                transactionType = "EXPENSE"
                                errorMessage = ""
                            }
                        )
                    } else {
                        AppOutlinedButton(
                            text = "🔴 برداشت از حساب / هزینه",
                            onClick = {
                                transactionType = "EXPENSE"
                                errorMessage = ""
                            }
                        )
                    }
                }

                // =========================================================
                // 5 - دسته بندی
                // =========================================================

                item {
                    Text(
                        text = "۵. 🏷 دسته‌بندی تراکنش (اختیاری)",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text =
                            "اگر می‌خواهید این نوع تراکنش همیشه در یک دسته " +
                                    "ثبت شود، دسته‌بندی را وارد کنید.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    AppTextField(
                        value = category,
                        onValueChange = {
                            category = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = "دسته‌بندی",
                        placeholder = {
                            Text("مثال: خرید")
                        },
                        supportingText = {
                            Text(
                                "نمونه‌ها: خرید، حقوق، انتقال وجه، قبض، شارژ"
                            )
                        },
                        singleLine = true
                    )
                }

                // =========================================================
                // توضیح تشخیص خودکار
                // =========================================================

                item {
                    AppCard(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                        Column(
                            modifier = Modifier.padding(14.dp)
                        ) {

                            Text(
                                text = "🔍 چه چیزهایی را برنامه خودش تشخیص می‌دهد؟",
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(
                                modifier = Modifier.height(8.dp)
                            )

                            Text(
                                text = "✓ مبلغ تراکنش",
                                style = MaterialTheme.typography.bodySmall
                            )

                            Text(
                                text = "✓ تاریخ و ساعت",
                                style = MaterialTheme.typography.bodySmall
                            )

                            Text(
                                text = "✓ شماره حساب یا کارت",
                                style = MaterialTheme.typography.bodySmall
                            )

                            Text(
                                text = "✓ بخش‌های متغیر پیامک",
                                style = MaterialTheme.typography.bodySmall
                            )

                            Spacer(
                                modifier = Modifier.height(8.dp)
                            )

                            Text(
                                text =
                                    "بنابراین شما فقط باید یک نمونه واقعی " +
                                            "و کامل از پیامک را به برنامه بدهید.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // =========================================================
                // خطا
                // =========================================================

                item {
                    if (errorMessage.isNotBlank()) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },

        confirmButton = {
            AppPrimaryButton(
                text = "🧠 ذخیره و یادگیری",
                onClick = {
                    when {
                        bankName.isBlank() -> {
                            errorMessage = "لطفاً نام بانک را وارد کنید."
                        }
                        sender.isBlank() -> {
                            errorMessage = "لطفاً شماره سامانه یا نام فرستنده را وارد کنید."
                        }
                        messageText.isBlank() -> {
                            errorMessage = "لطفاً یک نمونه کامل از پیامک را وارد کنید."
                        }
                        transactionType.isBlank() -> {
                            errorMessage = "لطفاً نوع تراکنش را انتخاب کنید."
                        }
                        else -> {
                            SmsLearningManager.savePatternRule(
                                sender = sender.trim(),
                                pattern = messageText.trim(),
                                bankName = bankName.trim(),
                                category = category.trim().ifBlank { null },
                                transactionType = transactionType
                            )
                            onSaved()
                        }
                    }
                }
            )
        },

        dismissButton = {
            AppTextButton(
                onClick = onDismiss
            ) {
                Text("انصراف")
            }
        }
    )
}
@Composable
private fun LearningSummaryCard(
    bankCount: Int,
    patternCount: Int,
    ignoredPatternCount: Int,
    ignoredSenderCount: Int,
) {
    AppCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Psychology,
                    contentDescription = null
                )

                Spacer(Modifier.padding(horizontal = 6.dp))

                Text(
                    "وضعیت یادگیری",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))

            Text("بانک‌های یادگرفته‌شده: $bankCount")
            Text("الگوهای یادگرفته‌شده: $patternCount")
            Text("پیام‌های نادیده‌گرفته‌شده: $ignoredPatternCount")
            Text("فرستنده‌های نادیده‌گرفته‌شده: $ignoredSenderCount")
        }
    }
}

@Composable
private fun LearnedBankRow(
    sender: String,
    bankName: String,
) {
    AppCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Text(
                bankName,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(4.dp))

            Text(
                "فرستنده: +$sender",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(4.dp))

            Text(
                "✓ یاد گرفته شده",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun LearnedPatternCard(
    sender: String,
    pattern: String,
    bankName: String?,
    category: String?,
    transactionType: String?,
    onDelete: () -> Unit,
) {
    AppCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "فرستنده: +$sender",
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        compactPattern(pattern),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "حذف الگو"
                    )
                }
            }

            Divider(Modifier.padding(vertical = 8.dp))

            Text(
                "بانک: ${bankName ?: "نامشخص"}"
            )

            Text(
                "نوع: ${transactionTypeLabel(transactionType)}"
            )

            if (!category.isNullOrBlank()) {
                Text(
                    "دسته‌بندی: $category"
                )
            }

            Spacer(Modifier.height(4.dp))

            Text(
                "✓ فعال",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun IgnoredPatternCard(
    sender: String,
    pattern: String,
    onDelete: () -> Unit,
) {
    AppCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "فرستنده: +$sender",
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        compactPattern(pattern),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "حذف الگو"
                    )
                }
            }

            Divider(Modifier.padding(vertical = 8.dp))

            Text("→ نادیده گرفتن")

            Spacer(Modifier.height(4.dp))

            Text(
                "✓ فعال",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun transactionTypeLabel(type: String?): String {
    return when (type?.uppercase()) {
        "INCOME" -> "درآمد"
        "EXPENSE" -> "هزینه"
        else -> type ?: "نامشخص"
    }
}

private fun compactPattern(pattern: String): String {
    return pattern
        .replace("<NUM>", "…")
        .replace(Regex("\\s+"), " ")
        .trim()
        .take(140)
}



