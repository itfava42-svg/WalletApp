package com.example.walletapp.ui.screens

import com.example.walletapp.ui.components.AppIconButton
import com.example.walletapp.ui.components.AppTopBar

import com.example.walletapp.ui.components.JalaliDatePickerDialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.example.walletapp.data.CheckEntry
import com.example.walletapp.data.CheckStatus
import com.example.walletapp.data.CheckManager
import com.example.walletapp.data.CheckStore
import com.example.walletapp.data.CheckType
import com.example.walletapp.data.JalaliCalendar
import com.example.walletapp.data.toPersianCurrency
import com.example.walletapp.ui.components.AppCard
import com.example.walletapp.ui.components.AppEmptyState
import com.example.walletapp.ui.components.AppIconBadge
import com.example.walletapp.ui.components.AppFloatingActionButton
import com.example.walletapp.ui.theme.AppDimens
import com.example.walletapp.ui.theme.AppTextStyles
import com.example.walletapp.ui.components.AppOutlinedButton
import com.example.walletapp.ui.components.AppPrimaryButton
import com.example.walletapp.ui.components.AppTextField
import java.util.UUID

private val CheckOrange = Color(0xFFF97316)
private val CheckOrangeLight = Color(0xFFFFF7ED)
private val CheckGreen = Color(0xFF10B981)
private val CheckGreenLight = Color(0xFFD1FAE5)
private val CheckRed = Color(0xFFEF4444)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckManagementScreen(
    onBack: () -> Unit
) {
    var showEditor by remember { mutableStateOf(false) }
    var editingCheck by remember { mutableStateOf<CheckEntry?>(null) }
    var deleteCheck by remember { mutableStateOf<CheckEntry?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                title = {
                    Text(
                        text = "مدیریت چک",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    AppIconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بازگشت"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            AppFloatingActionButton(
                onClick = {
                    editingCheck = null
                    showEditor = true
                }
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "افزودن چک"
                )
            }
        }
    ) { padding ->
        if (CheckStore.checks.isEmpty()) {
            EmptyCheckState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = AppDimens.PagePadding),
                onAdd = {
                    editingCheck = null
                    showEditor = true
                }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(AppDimens.PagePadding),
                verticalArrangement = Arrangement.spacedBy(AppDimens.CardGap)
            ) {
                items(
                    items = CheckStore.checks,
                    key = { it.id }
                ) { check ->
                    CheckCard(
                        check = check,
                        onEdit = {
                            editingCheck = check
                            showEditor = true
                        },
                        onDelete = { deleteCheck = check }
                    )
                }
            }
        }
    }

    if (showEditor) {
        CheckEditorDialog(
            existingCheck = editingCheck,
            onDismiss = {
                showEditor = false
                editingCheck = null
            },
            onSave = { check ->
                if (editingCheck == null) {
                    CheckManager.add(check)
                } else {
                    CheckManager.update(check)
                }
                showEditor = false
                editingCheck = null
            }
        )
    }

    deleteCheck?.let { check ->
        AlertDialog(
            onDismissRequest = { deleteCheck = null },
            title = {
                Text("حذف چک", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("آیا از حذف چک «${check.title}» مطمئن هستید؟")
            },
            confirmButton = {
                AppPrimaryButton(
                    text = "حذف",
                    onClick = {
                        CheckManager.remove(check.id)
                        deleteCheck = null
                    }
                )
            },
            dismissButton = {
                AppOutlinedButton(text = "انصراف", onClick = { deleteCheck = null })
            }
        )
    }
}

@Composable
private fun EmptyCheckState(
    modifier: Modifier,
    onAdd: () -> Unit,
) {
    AppEmptyState(
        modifier = modifier,
        title = "هنوز چکی ثبت نشده است",
        supportingText = "چک‌های دریافتی و پرداختی خود را مدیریت کنید",
        icon = Icons.Filled.FactCheck,
        action = {
            AppPrimaryButton(
                text = "ثبت چک جدید",
                onClick = onAdd,
                leadingIcon = { Icon(Icons.Filled.Add, contentDescription = null) },
            )
        },
    )
}

@Composable
private fun CheckCard(
    check: CheckEntry,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (check.status) {
        CheckStatus.PENDING -> CheckOrange
        CheckStatus.RECEIVED, CheckStatus.PAID -> CheckGreen
        CheckStatus.RETURNED -> CheckRed
        CheckStatus.CANCELLED -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val statusBackground = when (check.status) {
        CheckStatus.PENDING -> CheckOrangeLight
        CheckStatus.RECEIVED, CheckStatus.PAID -> CheckGreenLight
        CheckStatus.RETURNED -> MaterialTheme.colorScheme.errorContainer
        CheckStatus.CANCELLED -> MaterialTheme.colorScheme.surfaceVariant
    }

    AppCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppDimens.CardGap)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIconBadge(icon = Icons.Filled.FactCheck, size = AppDimens.IconBox, iconSize = AppDimens.Icon)

                Spacer(Modifier.size(AppDimens.ItemGap))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = check.title,
                        style = AppTextStyles.titleMd,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = check.type.label,
                        style = AppTextStyles.bodyMd,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AppIconButton(onClick = onEdit) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "ویرایش",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                AppIconButton(onClick = onDelete) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "حذف",
                        tint = CheckRed
                    )
                }
            }

            AppCard(modifier = Modifier.fillMaxWidth(), containerColor = statusBackground, contentPadding = PaddingValues(AppDimens.CompactPadding)) {
                    Text(
                        text = "مبلغ چک",
                        style = AppTextStyles.labelMd,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = check.amount.toPersianCurrency(),
                        style = AppTextStyles.numeralXl,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                    Spacer(Modifier.height(AppDimens.ItemGap))
                    Text(
                        text = "وضعیت: ${check.status.label}",
                        style = AppTextStyles.bodyMd,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor
                    )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppDimens.ItemGap)
            ) {
                CheckSmallInfo(
                    modifier = Modifier.weight(1f),
                    title = "بانک",
                    value = check.bank.ifBlank { "—" }
                )
                CheckSmallInfo(
                    modifier = Modifier.weight(1f),
                    title = "شماره چک",
                    value = check.checkNumber.ifBlank { "—" }
                )
            }

            CheckSmallInfo(
                modifier = Modifier.fillMaxWidth(),
                title = "تاریخ سررسید",
                value = "${check.dueYear}/${check.dueMonth.toString().padStart(2, '0')}/${check.dueDay.toString().padStart(2, '0')}"
            )

            if (check.personName.isNotBlank()) {
                CheckSmallInfo(
                    modifier = Modifier.fillMaxWidth(),
                    title = if (check.type == CheckType.RECEIVED) "صادرکننده" else "دریافت‌کننده",
                    value = check.personName
                )
            }
        }
    }
}

@Composable
private fun CheckSmallInfo(
    modifier: Modifier,
    title: String,
    value: String
) {
    AppCard(modifier = modifier, containerColor = MaterialTheme.colorScheme.surfaceVariant, contentPadding = PaddingValues(AppDimens.CompactPadding)) {
            Text(
                text = title,
                style = AppTextStyles.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(AppDimens.ItemGap))
            Text(
                text = value,
                style = AppTextStyles.bodyMd,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CheckEditorDialog(
    existingCheck: CheckEntry?,
    onDismiss: () -> Unit,
    onSave: (CheckEntry) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    var title by remember { mutableStateOf(existingCheck?.title ?: "") }
    var type by remember { mutableStateOf(existingCheck?.type ?: CheckType.RECEIVED) }
    var checkNumber by remember { mutableStateOf(existingCheck?.checkNumber ?: "") }
    var bank by remember { mutableStateOf(existingCheck?.bank ?: "") }
    var amount by remember { mutableStateOf(existingCheck?.amount?.toString() ?: "") }
    var personName by remember { mutableStateOf(existingCheck?.personName ?: "") }
    var issueDate by remember {
        mutableStateOf(
            existingCheck?.let {
                "${it.issueYear}/${it.issueMonth.toString().padStart(2, '0')}/${it.issueDay.toString().padStart(2, '0')}"
            } ?: JalaliCalendar.todaySlashed()
        )
    }
    var dueDate by remember {
        mutableStateOf(
            existingCheck?.let {
                "${it.dueYear}/${it.dueMonth.toString().padStart(2, '0')}/${it.dueDay.toString().padStart(2, '0')}"
            } ?: ""
        )
    }
    var status by remember { mutableStateOf(existingCheck?.status ?: CheckStatus.PENDING) }
    var description by remember { mutableStateOf(existingCheck?.description ?: "") }
    var expandedType by remember { mutableStateOf(false) }
    var expandedStatus by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(),
        title = {
            Text(
                text = if (existingCheck == null) "ثبت چک جدید" else "ویرایش چک",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(AppDimens.CardGap)
            ) {
                item {
                    AppTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = "عنوان چک"
                    )
                }
                item {
                    ExposedDropdownMenuBox(
                        expanded = expandedType,
                        onExpandedChange = { expandedType = !expandedType }
                    ) {
                        AppTextField(
                            value = type.label,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            label = "نوع چک",
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType)
                            }
                        )
                        ExposedDropdownMenu(
                            expanded = expandedType,
                            onDismissRequest = { expandedType = false }
                        ) {
                            CheckType.entries.forEach { itemType ->
                                DropdownMenuItem(
                                    text = { Text(itemType.label) },
                                    onClick = {
                                        type = itemType
                                        expandedType = false
                                    }
                                )
                            }
                        }
                    }
                }
                item {
                    CheckInputTile(
                        title = "مبلغ چک",
                        value = amount,
                        onValueChange = { amount = it.filter { c -> c.isDigit() } },
                        background = CheckOrangeLight,
                        accent = CheckOrange,
                        icon = Icons.Filled.Payments
                    )
                }
                item {
                    AppTextField(
                        value = checkNumber,
                        onValueChange = { checkNumber = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = "شماره چک",
                        leadingIcon = { Icon(Icons.Filled.Numbers, contentDescription = null) }
                    )
                }
                item {
                    AppTextField(
                        value = bank,
                        onValueChange = { bank = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = "نام بانک",
                        leadingIcon = { Icon(Icons.Filled.AccountBalance, contentDescription = null) }
                    )
                }
                item {
                    AppTextField(
                        value = personName,
                        onValueChange = { personName = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = if (type == CheckType.RECEIVED) "صادرکننده" else "دریافت‌کننده"
                    )
                }
                item {
                    DateField(
                        label = "تاریخ صدور",
                        value = issueDate,
                        onDateSelected = { issueDate = it },
                        context = context
                    )
                }
                item {
                    DateField(
                        label = "تاریخ سررسید",
                        value = dueDate,
                        onDateSelected = { dueDate = it },
                        context = context
                    )
                }
                item {
                    ExposedDropdownMenuBox(
                        expanded = expandedStatus,
                        onExpandedChange = { expandedStatus = !expandedStatus }
                    ) {
                        AppTextField(
                            value = status.label,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            label = "وضعیت چک",
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus)
                            }
                        )
                        ExposedDropdownMenu(
                            expanded = expandedStatus,
                            onDismissRequest = { expandedStatus = false }
                        ) {
                            CheckStatus.entries.forEach { itemStatus ->
                                DropdownMenuItem(
                                    text = { Text(itemStatus.label) },
                                    onClick = {
                                        status = itemStatus
                                        expandedStatus = false
                                    }
                                )
                            }
                        }
                    }
                }
                item {
                    AppTextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        label = "توضیحات",
                        singleLine = false,
                        maxLines = 5
                    )
                }
            }
        },
        confirmButton = {
            AppPrimaryButton(
                text = if (existingCheck == null) "ثبت چک" else "ذخیره تغییرات",
                onClick = {
                    val amountValue = amount.toLongOrNull() ?: 0L
                    val issueParts = parseJalaliDate(issueDate)
                    val dueParts = parseJalaliDate(dueDate)
                    if (title.isBlank() || amountValue <= 0 || issueParts == null || dueParts == null) {
                        return@AppPrimaryButton
                    }

                    val check = CheckEntry(
                        id = existingCheck?.id ?: UUID.randomUUID().toString(),
                        title = title.trim(),
                        type = type,
                        checkNumber = checkNumber.trim(),
                        bank = bank.trim(),
                        amount = amountValue,
                        personName = personName.trim(),
                        issueYear = issueParts[0],
                        issueMonth = issueParts[1],
                        issueDay = issueParts[2],
                        dueYear = dueParts[0],
                        dueMonth = dueParts[1],
                        dueDay = dueParts[2],
                        status = status,
                        description = description.trim()
                    )
                    onSave(check)
                }
            )
        },
        dismissButton = {
            AppOutlinedButton(text = "انصراف", onClick = onDismiss)
        }
    )
}

@Composable
private fun CheckInputTile(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    background: Color,
    accent: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    AppCard(modifier = Modifier.fillMaxWidth(), containerColor = background, contentPadding = PaddingValues(AppDimens.CompactPadding)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(AppDimens.Icon)
                )
                Spacer(Modifier.size(AppDimens.ItemGap))
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.height(AppDimens.ItemGap))
            AppTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                placeholder = { Text("وارد کنید") },
                label = title
            )
        }
    }

@Composable
private fun DateField(
    label: String,
    value: String,
    onDateSelected: (String) -> Unit,
    context: android.content.Context
) {
    var showPicker by remember { mutableStateOf(false) }

    AppTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        modifier = Modifier.fillMaxWidth(),
        label = label,
        leadingIcon = {
            Icon(Icons.Filled.CalendarMonth, contentDescription = null)
        },
        trailingIcon = {
            AppIconButton(onClick = { showPicker = true }) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = "انتخاب تاریخ")
            }
        }
    )

    if (showPicker) {
        JalaliDatePickerDialog(
            initialDate = value,
            onDismissRequest = { showPicker = false },
            onDateSelected = { selectedDate ->
                onDateSelected(selectedDate)
                showPicker = false
            }
        )
    }
}

private fun parseJalaliDate(value: String): IntArray? {
    val parts = value.split("/")
    if (parts.size != 3) return null
    val year = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull() ?: return null
    val day = parts[2].toIntOrNull() ?: return null
    if (month !in 1..12 || day !in 1..31) return null
    return intArrayOf(year, month, day)
}
