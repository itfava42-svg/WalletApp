package com.example.walletapp.ui.screens

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.walletapp.data.AccountStore
import com.example.walletapp.data.JalaliCalendar
import com.example.walletapp.data.Transaction
import com.example.walletapp.data.TransactionCategories
import com.example.walletapp.data.TransactionCategory
import com.example.walletapp.data.TransactionType
import com.example.walletapp.data.toPersianCurrency
import com.example.walletapp.data.toPersianDigits
import com.example.walletapp.ui.theme.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onBack: () -> Unit,
    onSave: (Transaction) -> Unit,
) {
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var amountDigits by remember { mutableStateOf("") } // raw ASCII digits only
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<TransactionCategory?>(null) }
    var selectedAccountId by remember { mutableStateOf<String?>(null) }
    var accountMenuExpanded by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        AccountStore.attachContext(context)

        if (selectedAccountId == null) {
            selectedAccountId =
                AccountStore.accounts
                    .firstOrNull { it.isDefault && it.isActive }
                    ?.id
                    ?: AccountStore.accounts
                        .firstOrNull { it.isActive }
                        ?.id
        }
    }

    val activeAccounts = AccountStore.accounts.filter { it.isActive }

    val selectedAccountName =
        activeAccounts
            .firstOrNull { it.id == selectedAccountId }
            ?.name
            ?: activeAccounts
                .firstOrNull { it.isDefault }
                ?.name
            ?: activeAccounts
                .firstOrNull()
                ?.name
            ?: "حساب اصلی"
    var isRecurring by remember { mutableStateOf(false) }
    var dateLabel by remember { mutableStateOf(JalaliCalendar.todaySlashed()) }
    var dateEpochMillis by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showValidationError by remember { mutableStateOf(false) }

    var selectedReceiptUri by remember { mutableStateOf<Uri?>(null) }
    var receiptPreviewBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    fun loadReceiptPreview(uri: Uri?) {
        if (uri == null) {
            receiptPreviewBitmap = null
            return
        }

        receiptPreviewBitmap =
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    BitmapFactory.decodeStream(input)
                }
            }.getOrNull()
    }

    fun createCameraUri(): Uri {
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

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
    }

    val galleryLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument(),
        ) { uri ->
            if (uri != null) {
                runCatching {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION,
                    )
                }

                selectedReceiptUri = uri
                    loadReceiptPreview(uri)
            }
        }

    val cameraLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.TakePicture(),
        ) { success ->
            val uri = cameraUri

            if (success && uri != null) {
                selectedReceiptUri = uri
            }
        }

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { granted ->
            if (granted) {
                val uri = createCameraUri()
                cameraUri = uri
                cameraLauncher.launch(uri)
            }
        }

    val categories = if (type == TransactionType.EXPENSE) TransactionCategories.expense else TransactionCategories.income
    // Reset category if it doesn't belong to the newly selected type's list.
    LaunchedEffect(type) {
        if (selectedCategory != null && selectedCategory !in categories) {
            selectedCategory = null
        }
    }

    val accentColor = if (type == TransactionType.EXPENSE) AppError else Primary

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("ثبت تراکنش جدید", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت")
                    }
                },
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = SurfaceContainerLowest) {
                Button(
                    onClick = {
                        val amountLong = amountDigits.toLongOrNull()
                        if (amountLong == null || amountLong <= 0L || title.isBlank()) {
                            showValidationError = true
                            return@Button
                        }
                        val category = selectedCategory
                        val subtitle = buildString {
                            append(category?.label ?: "سایر")
                            append(" • ")
                            append(dateLabel)
                            if (isRecurring) append(" • تکرارشونده")
                        }
                        onSave(
                            Transaction(
                                id = UUID.randomUUID().toString(),
                                title = title.trim(),
                                subtitle = subtitle,
                                amount = amountLong,
                                type = type,
                                iconKey = category?.label ?: "سایر",
                                accountId = selectedAccountId,
                                category = category?.label,
                                dateEpochMillis = dateEpochMillis,
                                note = note.trim(),
                                isRecurring = isRecurring,
                                receiptUri = selectedReceiptUri?.toString(),
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .height(54.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = OnPrimary),
                ) {
                    Text(
                        if (type == TransactionType.EXPENSE) "ثبت هزینه" else "ثبت درآمد",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    )
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(8.dp))

            // Expense / Income segmented toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceContainer)
                    .padding(4.dp),
            ) {
                SegmentButton(
                    label = "هزینه",
                    selected = type == TransactionType.EXPENSE,
                    selectedColor = AppError,
                    onClick = { type = TransactionType.EXPENSE },
                    modifier = Modifier.weight(1f),
                )
                SegmentButton(
                    label = "درآمد",
                    selected = type == TransactionType.INCOME,
                    selectedColor = Primary,
                    onClick = { type = TransactionType.INCOME },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(24.dp))

            // Amount
            Text("مبلغ", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = amountDigits,
                onValueChange = { input -> amountDigits = input.filter { it.isDigit() }.take(12) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("۰") },
                suffix = { Text("تومان") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = MaterialTheme.typography.headlineSmall,
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
            )
            if (amountDigits.isNotEmpty()) {
                Text(
                    (amountDigits.toLongOrNull() ?: 0L).toPersianCurrency() + " تومان",
                    style = MaterialTheme.typography.labelMedium,
                    color = accentColor,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            Spacer(Modifier.height(20.dp))

            // Title
            Text("عنوان", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("مثلاً خرید سوپرمارکت") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
            )

            Spacer(Modifier.height(20.dp))

            // Category grid
            Text("دسته‌بندی", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.height(if (categories.size > 4) 176.dp else 88.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                userScrollEnabled = false,
            ) {
                items(categories) { category ->
                    CategoryChip(
                        category = category,
                        selected = category == selectedCategory,
                        accentColor = accentColor,
                        onClick = { selectedCategory = category },
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Date
            Text("تاریخ", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                AssistChip(
                    onClick = {
                        dateLabel = JalaliCalendar.todaySlashed()
                        dateEpochMillis = null
                    },
                    label = { Text("امروز") },
                )
                Spacer(Modifier.width(8.dp))
                AssistChip(
                    onClick = {
                        dateLabel = JalaliCalendar.daysAgoSlashed(1)
                        dateEpochMillis = null
                    },
                    label = { Text("دیروز") },
                )
                Spacer(Modifier.width(8.dp))
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(dateLabel)
                }
            }

            Spacer(Modifier.height(20.dp))

            // Account
            Text("حساب", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = accountMenuExpanded,
                onExpandedChange = { accountMenuExpanded = it },
            ) {
                OutlinedTextField(
                    value = selectedAccountName,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = accountMenuExpanded
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                )

                ExposedDropdownMenu(
                    expanded = accountMenuExpanded,
                    onDismissRequest = { accountMenuExpanded = false },
                ) {
                    if (activeAccounts.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("هیچ حسابی وجود ندارد") },
                            onClick = {
                                accountMenuExpanded = false
                            },
                        )
                    } else {
                        activeAccounts.forEach { account ->
                            DropdownMenuItem(
                                text = { Text(account.name) },
                                onClick = {
                                    selectedAccountId = account.id
                                    accountMenuExpanded = false
                                },
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Note
            Text("یادداشت (اختیاری)", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                placeholder = { Text("توضیح بیشتر...") },
                shape = RoundedCornerShape(14.dp),
            )

            Spacer(Modifier.height(20.dp))

            // Recurring + attach receipt row
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = {
                            galleryLauncher.launch(
                                arrayOf(
                                    "image/jpeg",
                                    "image/png",
                                    "image/webp",
                                ),
                            )
                        },
                    ) {
                        Icon(
                            Icons.Filled.PhotoCamera,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )

                        Spacer(Modifier.width(4.dp))

                        Text(
                            if (selectedReceiptUri == null) {
                                "افزودن رسید"
                            } else {
                                "تعویض رسید"
                            },
                        )
                    }

                    TextButton(
                        onClick = {
                            cameraPermissionLauncher.launch(
                                Manifest.permission.CAMERA,
                            )
                        },
                    ) {
                        Text("دوربین")
                    }

                    if (selectedReceiptUri != null) {
                        TextButton(
                            onClick = {
                                selectedReceiptUri = null
                                cameraUri = null
                                receiptPreviewBitmap = null
                            },
                        ) {
                            Text("حذف رسید")
                        }
                    }
                }

                receiptPreviewBitmap?.let { previewBitmap ->
                    Spacer(Modifier.height(8.dp))

                    Image(
                        bitmap = previewBitmap.asImageBitmap(),
                        contentDescription = "پیش‌نمایش رسید",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(14.dp)),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    )
                }
            }

            if (showValidationError) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "لطفاً مبلغ و عنوان را وارد کنید.",
                    color = AppError,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = dateEpochMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        dateEpochMillis = millis
                        dateLabel = JalaliCalendar.fromEpochMillisSlashed(millis)
                    }
                    showDatePicker = false
                }) { Text("تأیید") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("انصراف") }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun SegmentButton(
    label: String,
    selected: Boolean,
    selectedColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) selectedColor else androidx.compose.ui.graphics.Color.Transparent,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                label,
                color = if (selected) OnPrimary else OnSurfaceVariant,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun CategoryChip(
    category: TransactionCategory,
    selected: Boolean,
    accentColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(14.dp),
            color = if (selected) accentColor.copy(alpha = 0.12f) else SurfaceContainer,
            border = if (selected) androidx.compose.foundation.BorderStroke(1.5.dp, accentColor) else null,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    category.icon,
                    contentDescription = category.label,
                    tint = if (selected) accentColor else OnSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            category.label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) accentColor else OnSurfaceVariant,
            maxLines = 1,
        )
    }
}






