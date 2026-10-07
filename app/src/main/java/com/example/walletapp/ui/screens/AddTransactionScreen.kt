package com.example.walletapp.ui.screens

import com.example.walletapp.ui.components.AppTextButton
import com.example.walletapp.ui.components.AppSegmentedButton
import com.example.walletapp.ui.components.AppSelectionTile
import com.example.walletapp.ui.components.AppAssistChip

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
import androidx.compose.foundation.layout.Spacer
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
import com.example.walletapp.ui.components.resolveTransactionIcon
import com.example.walletapp.ui.components.AppTextField
import com.example.walletapp.ui.components.AppOutlinedButton
import com.example.walletapp.ui.components.AppPrimaryButton
import com.example.walletapp.data.TransactionType
import com.example.walletapp.data.toPersianCurrency
import com.example.walletapp.data.toPersianDigits
import com.example.walletapp.ui.components.JalaliDatePickerDialog
import com.example.walletapp.ui.theme.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onBack: () -> Unit,
    onSave: (Transaction) -> Unit,
) {
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var amountDigits by remember { mutableStateOf("") } // normalized ASCII digits
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
                    AppIconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowForward, contentDescription = "بازگشت")
                    }
                },
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = SurfaceContainerLowest) {
                AppPrimaryButton(
                    text = if (type == TransactionType.EXPENSE) "ثبت هزینه" else "ثبت درآمد",
                    onClick = {
                        val normalizedAmount = amountDigits.map { ch ->
                            when {
                                ch.code in 0x06F0..0x06F9 -> ('0'.code + (ch.code - 0x06F0)).toChar()
                                ch.code in 0x0660..0x0669 -> ('0'.code + (ch.code - 0x0660)).toChar()
                                else -> ch
                            }
                        }.joinToString("")
                        val amountLong = normalizedAmount.toLongOrNull()
                        if (amountLong == null || amountLong <= 0L || title.isBlank()) {
                            showValidationError = true
                            return@AppPrimaryButton
                        }
                        if (selectedAccountId == null || activeAccounts.none { it.id == selectedAccountId }) {
                            showValidationError = true
                            return@AppPrimaryButton
                        }
                        showValidationError = false
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
                        .padding(20.dp),
                    containerColor = accentColor,
                    textStyle = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                )
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
                AppSegmentedButton(
                    label = "هزینه",
                    selected = type == TransactionType.EXPENSE,
                    selectedColor = AppError,
                    onClick = { type = TransactionType.EXPENSE },
                    modifier = Modifier.weight(1f),
                )
                AppSegmentedButton(
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
            AppTextField(
                value = amountDigits,
                onValueChange = { input ->
                    amountDigits = input.mapNotNull { ch ->
                        when {
                            ch in '0'..'9' -> ch
                            ch.code in 0x06F0..0x06F9 -> ('0'.code + (ch.code - 0x06F0)).toChar()
                            ch.code in 0x0660..0x0669 -> ('0'.code + (ch.code - 0x0660)).toChar()
                            else -> null
                        }
                    }.take(12).joinToString("")
                    showValidationError = false
                },
                modifier = Modifier.fillMaxWidth(),
                label = "مبلغ",
                placeholder = { Text("۰") },
                suffix = { Text("تومان") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = MaterialTheme.typography.headlineSmall,
                singleLine = true,
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
            AppTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = "عنوان",
                placeholder = { Text("مثلاً خرید سوپرمارکت") },
                singleLine = true,
            )

            androidx.compose.foundation.layout.Spacer(Modifier.height(20.dp))

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
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        AppSelectionTile(selected = category == selectedCategory, selectedColor = accentColor,
                            onClick = { selectedCategory = category }, modifier = Modifier.fillMaxWidth()) {
                            Icon(resolveTransactionIcon(category.iconKey), contentDescription = category.label,
                                tint = if (category == selectedCategory) accentColor else OnSurfaceVariant)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(category.label, style = MaterialTheme.typography.labelMedium,
                            color = if (category == selectedCategory) accentColor else OnSurfaceVariant, maxLines = 1)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Date
            Text("تاریخ", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppAssistChip(
                    onClick = {
                        dateLabel = JalaliCalendar.todaySlashed()
                        dateEpochMillis = null
                    },
                    label = { Text("امروز") },
                )
                Spacer(Modifier.width(8.dp))
                AppAssistChip(
                    onClick = {
                        dateLabel = JalaliCalendar.daysAgoSlashed(1)
                        dateEpochMillis = null
                    },
                    label = { Text("دیروز") },
                )
                Spacer(Modifier.width(8.dp))
                AppOutlinedButton(
                    text = dateLabel,
                    onClick = { showDatePicker = true },
                    leadingIcon = {
                        Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                )
            }

            Spacer(Modifier.height(20.dp))

            // Account
            Text("حساب", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = accountMenuExpanded,
                onExpandedChange = { accountMenuExpanded = it },
            ) {
                AppTextField(
                    value = selectedAccountName,
                    onValueChange = {},
                    label = "حساب",
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = accountMenuExpanded
                        )
                    },
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
            AppTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                label = "توضیحات",
                placeholder = { Text("توضیح بیشتر...") },
                singleLine = false,
            )

            androidx.compose.foundation.layout.Spacer(Modifier.height(20.dp))

            // Recurring + attach receipt row
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppTextButton(
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

                    AppTextButton(
                        onClick = {
                            cameraPermissionLauncher.launch(
                                Manifest.permission.CAMERA,
                            )
                        },
                    ) {
                        Text("دوربین")
                    }

                    if (selectedReceiptUri != null) {
                        AppTextButton(
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
                    "لطفاً مبلغ، عنوان و یک حساب فعال را بررسی کنید.",
                    color = AppError,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showDatePicker) {
        JalaliDatePickerDialog(
            initialDate = dateEpochMillis?.let {
                JalaliCalendar.fromEpochMillisSlashed(it)
            } ?: dateLabel,
            onDismissRequest = {
                showDatePicker = false
            },
            onDateSelected = { selectedDate ->
                val asciiDate = selectedDate.map { ch ->
                    when {
                        ch.code in 0x06F0..0x06F9 ->
                            ('0'.code + (ch.code - 0x06F0)).toChar()
                        ch.code in 0x0660..0x0669 ->
                            ('0'.code + (ch.code - 0x0660)).toChar()
                        else -> ch
                    }
                }.joinToString("")

                val parts = asciiDate.split("/")
                val year = parts.getOrNull(0)?.toIntOrNull()
                val month = parts.getOrNull(1)?.toIntOrNull()
                val day = parts.getOrNull(2)?.toIntOrNull()

                if (year != null && month != null && day != null) {
                    val selectedMillis =
                        JalaliCalendar.jalaliToEpochMillis(year, month, day)

                    dateEpochMillis = selectedMillis
                    dateLabel = JalaliCalendar.fromEpochMillisSlashed(selectedMillis)
                }

                showDatePicker = false
            }
        )
    }
}
