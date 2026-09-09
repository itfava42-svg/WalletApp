package com.example.walletapp.ui.screens

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
            ?: "بدون حساب"
    var isRecurring by remember { mutableStateOf(false) }
    var dateLabel by remember { mutableStateOf(JalaliCalendar.todaySlashed()) }
    var dateEpochMillis by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showValidationError by remember { mutableStateOf(false) }

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
                title = { Text("ط«ط¨طھ طھط±ط§ع©ظ†ط´ ط¬ط¯غŒط¯", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowForward, contentDescription = "ط¨ط§ط²ع¯ط´طھ")
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
                            append(category?.label ?: "ط³ط§غŒط±")
                            append(" â€¢ ")
                            append(dateLabel)
                            if (isRecurring) append(" â€¢ طھع©ط±ط§ط±ط´ظˆظ†ط¯ظ‡")
                        }
                        onSave(
                            Transaction(
                                id = UUID.randomUUID().toString(),
                                title = title.trim(),
                                subtitle = subtitle,
                                amount = amountLong,
                                type = type,
                                iconKey = category?.label ?: "ط³ط§غŒط±",
                                accountId = selectedAccountId,
                                category = category?.label,
                                dateEpochMillis = dateEpochMillis,
                                note = note.trim(),
                                isRecurring = isRecurring,
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = OnPrimary),
                ) {
                    Text(
                        if (type == TransactionType.EXPENSE) "ط«ط¨طھ ظ‡ط²غŒظ†ظ‡" else "ط«ط¨طھ ط¯ط±ط¢ظ…ط¯",
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(8.dp))

            // Expense / Income segmented toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainer)
                    .padding(4.dp),
            ) {
                SegmentButton(
                    label = "ظ‡ط²غŒظ†ظ‡",
                    selected = type == TransactionType.EXPENSE,
                    selectedColor = AppError,
                    onClick = { type = TransactionType.EXPENSE },
                    modifier = Modifier.weight(1f),
                )
                SegmentButton(
                    label = "ط¯ط±ط¢ظ…ط¯",
                    selected = type == TransactionType.INCOME,
                    selectedColor = Primary,
                    onClick = { type = TransactionType.INCOME },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(24.dp))

            // Amount
            Text("ظ…ط¨ظ„ط؛", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = amountDigits,
                onValueChange = { input -> amountDigits = input.filter { it.isDigit() }.take(12) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("غ°") },
                suffix = { Text("طھظˆظ…ط§ظ†") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = MaterialTheme.typography.headlineSmall,
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
            )
            if (amountDigits.isNotEmpty()) {
                Text(
                    (amountDigits.toLongOrNull() ?: 0L).toPersianCurrency() + " طھظˆظ…ط§ظ†",
                    style = MaterialTheme.typography.labelMedium,
                    color = accentColor,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            Spacer(Modifier.height(20.dp))

            // Title
            Text("ط¹ظ†ظˆط§ظ†", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("ظ…ط«ظ„ط§ظ‹ ط®ط±غŒط¯ ط³ظˆظ¾ط±ظ…ط§ط±ع©طھ") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
            )

            Spacer(Modifier.height(20.dp))

            // Category grid
            Text("ط¯ط³طھظ‡â€Œط¨ظ†ط¯غŒ", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
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
            Text("طھط§ط±غŒط®", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                AssistChip(
                    onClick = {
                        dateLabel = JalaliCalendar.todaySlashed()
                        dateEpochMillis = null
                    },
                    label = { Text("ط§ظ…ط±ظˆط²") },
                )
                Spacer(Modifier.width(8.dp))
                AssistChip(
                    onClick = {
                        dateLabel = JalaliCalendar.daysAgoSlashed(1)
                        dateEpochMillis = null
                    },
                    label = { Text("ط¯غŒط±ظˆط²") },
                )
                Spacer(Modifier.width(8.dp))
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(dateLabel)
                }
            }

            Spacer(Modifier.height(20.dp))

            // Account
            Text("ط­ط³ط§ط¨", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
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
                    shape = RoundedCornerShape(12.dp),
                )

                ExposedDropdownMenu(
                    expanded = accountMenuExpanded,
                    onDismissRequest = { accountMenuExpanded = false },
                ) {
                    if (activeAccounts.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("حسابی ثبت نشده است") },
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
            Text("غŒط§ط¯ط¯ط§ط´طھ (ط§ط®طھغŒط§ط±غŒ)", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                placeholder = { Text("طھظˆط¶غŒط­ ط¨غŒط´طھط±...") },
                shape = RoundedCornerShape(12.dp),
            )

            Spacer(Modifier.height(20.dp))

            // Recurring + attach receipt row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = isRecurring, onCheckedChange = { isRecurring = it })
                    Spacer(Modifier.width(8.dp))
                    Text("طھط±ط§ع©ظ†ط´ طھع©ط±ط§ط±ط´ظˆظ†ط¯ظ‡", style = MaterialTheme.typography.bodyMedium)
                }
                TextButton(onClick = { /* TODO: hook up an image picker for the receipt photo */ }) {
                    Icon(Icons.Filled.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("ط§ظپط²ظˆط¯ظ† ط±ط³غŒط¯")
                }
            }

            if (showValidationError) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "ظ„ط·ظپط§ظ‹ ظ…ط¨ظ„ط؛ ظˆ ط¹ظ†ظˆط§ظ† ط±ط§ ظˆط§ط±ط¯ ع©ظ†غŒط¯.",
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
                }) { Text("طھط£غŒغŒط¯") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("ط§ظ†طµط±ط§ظپ") }
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
        shape = RoundedCornerShape(12.dp),
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
                .height(52.dp),
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

