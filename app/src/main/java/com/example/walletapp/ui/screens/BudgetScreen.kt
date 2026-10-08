package com.example.walletapp.ui.screens

import com.example.walletapp.ui.components.AppTopBar

import com.example.walletapp.ui.components.AppFloatingActionButton
import com.example.walletapp.ui.components.AppFilterChip
import com.example.walletapp.ui.components.AppCard

import com.example.walletapp.ui.components.AppTextButton

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.walletapp.data.Budget
import com.example.walletapp.data.BudgetPeriod
import com.example.walletapp.data.BudgetManager
import com.example.walletapp.data.BudgetStore
import com.example.walletapp.data.TransactionCategories
import com.example.walletapp.data.toPersianCurrency
import com.example.walletapp.ui.components.DashboardBottomBar
import com.example.walletapp.ui.components.AppPrimaryButton
import com.example.walletapp.ui.components.AppTextField
import com.example.walletapp.ui.theme.AppDimens
import com.example.walletapp.ui.theme.AppTextStyles
import com.example.walletapp.ui.components.appNavItems
import com.example.walletapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    selectedNavIndex: Int,
    onNavSelect: (Int) -> Unit,
) {
    var showAddDialog by remember { mutableStateOf(false) }
    val budgets = BudgetStore.budgets

    val totalBudget = budgets.sumOf { it.amount }
    val totalSpent = budgets.sumOf { BudgetStore.spentFor(it) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(centerAligned = true, title = { Text("بودجه‌بندی", style = AppTextStyles.titleLg) })
        },
        bottomBar = {
            DashboardBottomBar(items = appNavItems, selectedIndex = selectedNavIndex, onSelect = onNavSelect)
        },
        floatingActionButton = {
            AppFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Primary,
                contentColor = OnPrimary,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "افزودن بودجه")
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Primary)
                     .padding(horizontal = AppDimens.PagePadding, vertical = AppDimens.PagePadding),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                SummaryStatColumn("بودجه کل", totalBudget)
                SummaryStatColumn("هزینه‌شده", totalSpent)
                SummaryStatColumn("باقی‌مانده", (totalBudget - totalSpent).coerceAtLeast(0))
            }

            if (budgets.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(AppDimens.PagePadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text("هنوز بودجه‌ای تعریف نشده است", style = AppTextStyles.bodyLg.copy(fontWeight = AppTextStyles.titleMd.fontWeight))
                    Spacer(Modifier.height(AppDimens.ItemGap))
                    Text(
                        "برای هر دسته‌بندی سقف خرج تعیین کنید و مصرف خود را دنبال کنید.",
                        color = OnSurfaceVariant,
                        style = AppTextStyles.bodyMd,
                    )
                    Spacer(Modifier.height(AppDimens.ItemGap))
                    AppPrimaryButton(
                        text = "ثبت اولین بودجه",
                        onClick = { showAddDialog = true }
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = AppDimens.PagePadding),
                    contentPadding = PaddingValues(vertical = AppDimens.PagePadding),
                ) {
                    items(budgets, key = { it.id }) { budget ->
                        BudgetCard(budget)
                        Spacer(Modifier.height(AppDimens.CardGap))
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddBudgetDialog(
            onDismiss = { showAddDialog = false },
            onSave = { category, amount, period ->
                BudgetManager.add(category, amount, period)
                showAddDialog = false
            },
        )
    }
}

@Composable
private fun SummaryStatColumn(label: String, amount: Long) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = OnPrimary.copy(alpha = 0.85f), style = AppTextStyles.labelMd)
        Spacer(Modifier.height(AppDimens.TightGap))
        Text("${amount.toPersianCurrency()} تومان", color = OnPrimary, style = AppTextStyles.titleMd)
    }
}

@Composable
private fun BudgetCard(budget: Budget) {
    val spent = BudgetStore.spentFor(budget)
    val percentage = if (budget.amount > 0) (spent.toFloat() / budget.amount.toFloat()) else 0f
    val progressColor = when {
        percentage >= 1f -> AppError
        percentage >= 0.8f -> SecondaryContainer
        else -> Primary
    }

    AppCard(
        modifier = Modifier.fillMaxWidth(),
            ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(budget.category, style = AppTextStyles.bodyLg.copy(fontWeight = AppTextStyles.titleMd.fontWeight))
                    Text(budget.period.label, style = AppTextStyles.labelMd, color = OnSurfaceVariant)
                }
                Text(
                    "${(percentage * 100).toInt()}%",
                    style = AppTextStyles.titleMd,
                    color = progressColor,
                )
            }
            Spacer(Modifier.height(AppDimens.ItemGap))
            LinearProgressIndicator(
                progress = { percentage.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppDimens.ProgressHeight)
                    .clip(RoundedCornerShape(AppDimens.TightGap)),
                color = progressColor,
                trackColor = SurfaceContainer,
            )
            Spacer(Modifier.height(AppDimens.ItemGap))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "${spent.toPersianCurrency()} تومان هزینه شده",
                    style = AppTextStyles.labelMd,
                    color = OnSurfaceVariant,
                )
                Text(
                    "از ${budget.amount.toPersianCurrency()} تومان",
                    style = AppTextStyles.labelMd,
                    color = OnSurfaceVariant,
                )
            }
        }
}

@Composable
private fun AddBudgetDialog(
    onDismiss: () -> Unit,
    onSave: (category: String, amount: Long, period: BudgetPeriod) -> Unit,
) {
    var selectedCategory by remember { mutableStateOf(TransactionCategories.expense.first().label) }
    var amountDigits by remember { mutableStateOf("") }
    var selectedPeriod by remember { mutableStateOf(BudgetPeriod.MONTHLY) }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("بودجه جدید", style = AppTextStyles.titleLg) },
        text = {
            Column {
                Text("دسته‌بندی", style = AppTextStyles.labelMd, color = OnSurfaceVariant)
                Spacer(Modifier.height(AppDimens.ItemGap))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                ) {
                    TransactionCategories.expense.forEach { cat ->
                        AppFilterChip(
                            selected = selectedCategory == cat.label,
                            onClick = { selectedCategory = cat.label },
                            label = { Text(cat.label) },
                            modifier = Modifier.padding(end = AppDimens.IntroGap),
                        )
                    }
                }
                Spacer(Modifier.height(AppDimens.ItemGap))
                Text("مبلغ", style = AppTextStyles.labelMd, color = OnSurfaceVariant)
                Spacer(Modifier.height(AppDimens.ItemGap))
                AppTextField(
                    value = amountDigits,
                    onValueChange = { input -> amountDigits = input.filter { it.isDigit() }.take(12) },
                    label = "مبلغ",
                    placeholder = { Text("۰") },
                    suffix = { Text("تومان") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(AppDimens.ItemGap))
                Text("دوره", style = AppTextStyles.labelMd, color = OnSurfaceVariant)
                Spacer(Modifier.height(AppDimens.ItemGap))
                Row(modifier = Modifier.fillMaxWidth()) {
                    BudgetPeriod.entries.forEach { period ->
                        AppFilterChip(
                            selected = selectedPeriod == period,
                            onClick = { selectedPeriod = period },
                            label = { Text(period.label) },
                            modifier = Modifier.padding(end = AppDimens.FilterChipGap),
                        )
                    }
                }
                if (showError) {
                    Spacer(Modifier.height(AppDimens.ItemGap))
                    Text("لطفاً مبلغ معتبری وارد کنید.", color = AppError, style = AppTextStyles.labelMd)
                }
            }
        },
        confirmButton = {
            AppTextButton(onClick = {
                val amount = amountDigits.toLongOrNull()
                if (amount == null || amount <= 0) {
                    showError = true
                } else {
                    onSave(selectedCategory, amount, selectedPeriod)
                }
            }) { Text("ذخیره") }
        },
        dismissButton = {
            AppTextButton(onClick = onDismiss) { Text("انصراف") }
        },
    )
}
