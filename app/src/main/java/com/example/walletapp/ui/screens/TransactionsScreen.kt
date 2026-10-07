package com.example.walletapp.ui.screens

import com.example.walletapp.ui.components.AppTopBar

import com.example.walletapp.ui.components.AppTextButton
import com.example.walletapp.ui.components.AppCard
import com.example.walletapp.ui.components.AppFilterChip
import com.example.walletapp.ui.components.AppAssistChip
import com.example.walletapp.ui.components.AppStatusPill

import com.example.walletapp.data.JalaliCalendar
import com.example.walletapp.data.toPersianCurrency
import com.example.walletapp.data.toPersianDigits

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.HistoryToggleOff
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import com.example.walletapp.ui.components.AppIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.walletapp.data.AccountStore
import com.example.walletapp.data.Transaction
import com.example.walletapp.data.TransactionKind
import com.example.walletapp.data.TransactionStore
import com.example.walletapp.data.TransactionType
import com.example.walletapp.ui.components.DashboardBottomBar
import com.example.walletapp.ui.components.AppTextField
import com.example.walletapp.ui.theme.AppDimens
import com.example.walletapp.ui.components.EditTransactionDialog
import com.example.walletapp.ui.components.appNavItems
import com.example.walletapp.ui.theme.AppError
import com.example.walletapp.ui.theme.AppTextStyles
import com.example.walletapp.ui.theme.OnPrimary
import com.example.walletapp.ui.theme.OnPrimaryContainer
import com.example.walletapp.ui.theme.OnSurface
import com.example.walletapp.ui.theme.OnSurfaceVariant
import com.example.walletapp.ui.theme.Outline
import com.example.walletapp.ui.theme.Primary
import com.example.walletapp.ui.theme.PrimaryContainer
import com.example.walletapp.ui.theme.SurfaceContainer
import com.example.walletapp.ui.theme.SurfaceContainerHigh
import com.example.walletapp.ui.theme.SurfaceContainerLow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class TransactionFilter(
    val title: String
) {
    ALL("همه"),
    INCOME("درآمد"),
    EXPENSE("هزینه"),
    TRANSFER("انتقال"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    selectedNavIndex: Int = 1,
    onNavSelect: (Int) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        AccountStore.attachContext(context)
        TransactionStore.attachContext(context)
    }

    val transactions = TransactionStore.transactions

    var selectedFilter by remember {
        mutableStateOf(TransactionFilter.ALL)
    }

    var searchQuery by remember {
        mutableStateOf("")
    }

    var editingTransaction by remember {
        mutableStateOf<Transaction?>(null)
    }

    var selectedAccountId by remember {
        mutableStateOf<String?>(null)
    }

    var newestFirst by remember {
        mutableStateOf(true)
    }

    val filteredTransactions =
        transactions
            .asSequence()
            .filter { transaction ->
                when (selectedFilter) {
                    TransactionFilter.ALL -> true

                    TransactionFilter.INCOME ->
                        transaction.kind == TransactionKind.NORMAL &&
                            transaction.type == TransactionType.INCOME

                    TransactionFilter.EXPENSE ->
                        transaction.kind == TransactionKind.NORMAL &&
                            transaction.type == TransactionType.EXPENSE

                    TransactionFilter.TRANSFER ->
                        transaction.kind == TransactionKind.TRANSFER
                }
            }
            .filter { transaction ->
                selectedAccountId == null ||
                    transaction.accountId == selectedAccountId ||
                    transaction.sourceAccountId == selectedAccountId ||
                    transaction.destinationAccountId == selectedAccountId
            }
            .filter { transaction ->
                val query = searchQuery.trim()

                if (query.isBlank()) {
                    true
                } else {
                    val accountName =
                        transaction.accountId
                            ?.let { id ->
                                AccountStore.accounts.firstOrNull { it.id == id }?.name
                            }
                            .orEmpty()

                    val sourceAccountName =
                        transaction.sourceAccountId
                            ?.let { id ->
                                AccountStore.accounts.firstOrNull { it.id == id }?.name
                            }
                            .orEmpty()

                    val destinationAccountName =
                        transaction.destinationAccountId
                            ?.let { id ->
                                AccountStore.accounts.firstOrNull { it.id == id }?.name
                            }
                            .orEmpty()

                    listOf(
                        transaction.title,
                        transaction.subtitle,
                        transaction.note,
                        transaction.description,
                        transaction.category.orEmpty(),
                        accountName,
                        sourceAccountName,
                        destinationAccountName,
                    ).any {
                        it.contains(query, ignoreCase = true)
                    }
                }
            }
            .toList()
            .sortedWith(
                compareBy<Transaction> {
                    it.dateEpochMillis ?: it.createdAt
                }.let { comparator ->
                    if (newestFirst) {
                        comparator.reversed()
                    } else {
                        comparator
                    }
                }
            )

    Scaffold(
        topBar = {
            AppTopBar(centerAligned = true, 
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "تراکنش‌ها",
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "${filteredTransactions.size} مورد",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant,
                        )
                    }
                },
                actions = {
                    AppIconButton(
                        onClick = {
                            newestFirst = !newestFirst
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Tune,
                            contentDescription = "مرتب‌سازی",
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            DashboardBottomBar(
                items = appNavItems,
                selectedIndex = selectedNavIndex,
                onSelect = onNavSelect,
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = AppDimens.PagePadding)
        ) {

            Spacer(Modifier.height(10.dp))

            AppTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                label = "جستجو",
                singleLine = true,
                leadingIcon = {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = "جستجو",
                        tint = Primary,
                    )
                },
                placeholder = {
                    Text(
                        "تراکنش، حساب یا دسته‌بندی"
                    )
                }
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = "نوع تراکنش",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = OnSurface,
            )

            Spacer(Modifier.height(AppDimens.ItemGap))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                TransactionFilter.entries.forEach { filter ->
                    AppFilterChip(
                        selected = selectedFilter == filter,
                        onClick = {
                            selectedFilter = filter
                        },
                        label = {
                            Text(filter.title)
                        },
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            if (AccountStore.accounts.any { it.isActive }) {
                Text(
                    text = "حساب",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface,
                )

                Spacer(Modifier.height(AppDimens.ItemGap))

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 1.dp
                    )
                ) {
                    item {
                        AppFilterChip(
                            selected = selectedAccountId == null,
                            onClick = {
                                selectedAccountId = null
                            },
                            label = {
                                Text("همه حساب‌ها")
                            },
                        )
                    }

                    items(
                        AccountStore.accounts.filter { it.isActive },
                        key = { it.id }
                    ) { account ->
                        AppFilterChip(
                            selected = selectedAccountId == account.id,
                            onClick = {
                                selectedAccountId =
                                    if (selectedAccountId == account.id) {
                                        null
                                    } else {
                                        account.id
                                    }
                            },
                            label = {
                                Text(account.name)
                            },
                        )
                    }
                }
            }

            Spacer(Modifier.height(AppDimens.ItemGap))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = if (newestFirst) {
                        "جدیدترین تراکنش‌ها"
                    } else {
                        "قدیمی‌ترین تراکنش‌ها"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )

                AppTextButton(
                    onClick = {
                        newestFirst = !newestFirst
                    }
                ) {
                    Text(
                        if (newestFirst) {
                            "قدیمی‌ترها"
                        } else {
                            "جدیدترها"
                        },
                        color = Primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(Modifier.height(AppDimens.ItemGap))

            if (filteredTransactions.isEmpty()) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                Primary.copy(alpha = 0.10f)
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Outlined.HistoryToggleOff,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(34.dp),
                        )
                    }

                    Spacer(Modifier.height(AppDimens.ItemGap))

                    Text(
                        text = if (transactions.isEmpty()) {
                            "هنوز تراکنشی ثبت نشده است"
                        } else {
                            "تراکنشی با این فیلتر پیدا نشد"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = if (transactions.isEmpty()) {
                            "اولین تراکنش خود را ثبت کنید"
                        } else {
                            "فیلتر یا عبارت جستجو را تغییر دهید"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant,
                    )
                }

            } else {

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        bottom = 16.dp
                    )
                ) {
                    items(
                        items = filteredTransactions,
                        key = { it.id }
                    ) { transaction ->
                        TransactionFullCard(
                            transaction = transaction,
                            onClick = {
                                editingTransaction = transaction
                            }
                        )
                    }
                }
            }
        }
    }

    editingTransaction?.let { transaction ->
        EditTransactionDialog(
            transaction = transaction,
            onDismiss = {
                editingTransaction = null
            },
            onSave = { updated ->
                TransactionStore.update(updated)
                editingTransaction = null
            },
            onDelete = { toDelete ->
                TransactionStore.remove(toDelete.id)
                editingTransaction = null
            },
        )
    }
}

private fun formatTransactionDate(transaction: Transaction): String {
    val timestamp =
        transaction.dateEpochMillis
            ?: transaction.createdAt

    if (timestamp <= 0L) {
        return ""
    }

    val jalaliDate =
        JalaliCalendar.fromEpochMillisSlashed(timestamp)

    val time =
        SimpleDateFormat(
            "HH:mm",
            Locale.US
        )
            .format(Date(timestamp))
            .toPersianDigits()

    return "$jalaliDate $time"
}

@Composable
private fun TransactionFullCard(
    transaction: Transaction,
    onClick: () -> Unit,
) {
    val accountName =
        transaction.accountId
            ?.let { id ->
                AccountStore.accounts.firstOrNull { it.id == id }?.name
            }

    val sourceName =
        transaction.sourceAccountId
            ?.let { id ->
                AccountStore.accounts.firstOrNull { it.id == id }?.name
            }

    val destinationName =
        transaction.destinationAccountId
            ?.let { id ->
                AccountStore.accounts.firstOrNull { it.id == id }?.name
            }

    val isTransfer = transaction.kind == TransactionKind.TRANSFER
    val isIncome =
        !isTransfer &&
            transaction.type == TransactionType.INCOME

    val typeTitle =
        when {
            isTransfer -> "انتقال"
            isIncome -> "درآمد"
            else -> "هزینه"
        }

    val accentColor =
        when {
            isTransfer -> Color(0xFF3D8BDE)
            isIncome -> Primary
            else -> AppError
        }

    val icon =
        when {
            isTransfer -> Icons.Filled.SwapHoriz
            isIncome -> Icons.Filled.ArrowDownward
            else -> Icons.Filled.ArrowUpward
        }

    val sign =
        when {
            isTransfer -> ""
            isIncome -> "+"
            else -> "-"
        }

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        onClick = onClick,
    ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(
                            accentColor.copy(alpha = 0.10f)
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(23.dp),
                    )
                }

                Spacer(Modifier.size(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = transaction.title.ifBlank {
                            typeTitle
                        },
                        fontWeight = FontWeight.Bold,
                        style = AppTextStyles.bodyLg,
                        color = OnSurface,
                        maxLines = 1,
                    )

                    Spacer(Modifier.height(3.dp))

                    Text(
                        text = formatTransactionDate(transaction),
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant,
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "$sign${formatTransactionAmount(transaction.amount)}",
                        style = AppTextStyles.titleSm.copy(fontWeight = FontWeight.Bold),
                        color = accentColor,
                    )

                    Spacer(Modifier.height(2.dp))

                    Text(
                        text = "ریال",
                        style = MaterialTheme.typography.labelSmall,
                        color = Outline,
                    )
                }
            }

            Spacer(Modifier.height(AppDimens.CardGap))

            AppStatusPill(
                text = if (isTransfer) {
                    val source =
                        sourceName
                            ?: transaction.sourceAccountId
                            ?: "مبدأ نامشخص"
                    val destination =
                        destinationName
                            ?: transaction.destinationAccountId
                            ?: "مقصد نامشخص"
                    "$typeTitle • $source ← $destination"
                } else {
                    val detail =
                        accountName
                            ?: transaction.category?.takeIf { it.isNotBlank() }
                            ?: ""
                    if (detail.isBlank()) typeTitle else "$typeTitle • $detail"
                },
                accentColor = accentColor,
            )

            if (transaction.subtitle.isNotBlank()) {
                Spacer(Modifier.height(7.dp))

                Text(
                    text = transaction.subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceVariant,
                    maxLines = 1,
                )
            }

            if (transaction.note.isNotBlank()) {
                Spacer(Modifier.height(4.dp))

                Text(
                    text = transaction.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = Outline,
                    maxLines = 2,
                )
            }
        }
}

private fun formatTransactionAmount(
    amount: Long
): String {
    return amount.toPersianCurrency()
}


