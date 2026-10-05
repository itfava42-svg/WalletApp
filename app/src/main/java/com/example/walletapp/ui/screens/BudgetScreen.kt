package com.example.walletapp.ui.screens

import com.example.walletapp.ui.components.AppFloatingActionButton

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
import androidx.compose.ui.text.font.FontWeight
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
            CenterAlignedTopAppBar(title = { Text("بودجه‌بندی", fontWeight = FontWeight.Bold) })
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
                    .padding(horizontal = 16.dp, vertical = 16.dp),
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
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text("هنوز بودجه‌ای تعریف نشده است", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(AppDimens.ItemGap))
                    Text(
                        "برای هر دسته‌بندی سقف خرج تعیین کنید و مصرف خود را دنبال کنید.",
                        color = OnSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
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
        Text(label, color = OnPrimary.copy(alpha = 0.85f), style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(4.dp))
        Text("${amount.toPersianCurrency()} تومان", color = OnPrimary, fontWeight = FontWeight.Bold)
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

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(budget.category, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                    Text(budget.period.label, style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                }
                Text(
                    "${(percentage * 100).toInt()}%",
                    fontWeight = FontWeight.Bold,
                    color = progressColor,
                )
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { percentage.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = progressColor,
                trackColor = SurfaceContainer,
            )
            Spacer(Modifier.height(AppDimens.ItemGap))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "${spent.toPersianCurrency()} تومان هزینه شده",
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceVariant,
                )
                Text(
                    "از ${budget.amount.toPersianCurrency()} تومان",
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceVariant,
                )
            }
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
        title = { Text("بودجه جدید", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("دسته‌بندی", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                Spacer(Modifier.height(AppDimens.ItemGap))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                ) {
                    TransactionCategories.expense.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat.label,
                            onClick = { selectedCategory = cat.label },
                            label = { Text(cat.label) },
                            modifier = Modifier.padding(end = 6.dp),
                        )
                    }
                }
                Spacer(Modifier.height(AppDimens.ItemGap))
                Text("مبلغ", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
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
                Text("دوره", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                Spacer(Modifier.height(AppDimens.ItemGap))
                Row(modifier = Modifier.fillMaxWidth()) {
                    BudgetPeriod.entries.forEach { period ->
                        FilterChip(
                            selected = selectedPeriod == period,
                            onClick = { selectedPeriod = period },
                            label = { Text(period.label) },
                            modifier = Modifier.padding(end = 6.dp),
                        )
                    }
                }
                if (showError) {
                    Spacer(Modifier.height(AppDimens.ItemGap))
                    Text("لطفاً مبلغ معتبری وارد کنید.", color = AppError, style = MaterialTheme.typography.labelMedium)
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
