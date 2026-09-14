package com.example.walletapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.HistoryToggleOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.walletapp.data.AccountStore
import com.example.walletapp.data.AccountLedger
import com.example.walletapp.data.TransactionStore
import com.example.walletapp.data.TransactionType
import com.example.walletapp.data.resolveTransactionIcon
import com.example.walletapp.ui.components.*
import com.example.walletapp.ui.theme.OnSurfaceVariant
import com.example.walletapp.ui.theme.Outline
import com.example.walletapp.ui.theme.Primary
import com.example.walletapp.ui.theme.PrimaryContainer
import com.example.walletapp.ui.theme.SecondaryContainer
import com.example.walletapp.ui.theme.OnSecondaryContainer
import com.example.walletapp.ui.theme.SurfaceContainerHigh
import com.example.walletapp.ui.theme.Tertiary
import com.example.walletapp.data.TransactionSyncViewModel
import com.example.walletapp.ui.components.TransactionSyncCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onAddTransactionClick: () -> Unit = {},
    onOpenTransactions: () -> Unit = {},
    onOpenBudgetClick: () -> Unit = {},
    onOpenMoreClick: () -> Unit = {},
    selectedNavIndex: Int = 0,
    onNavSelect: (Int) -> Unit = {},
) {
    val context = LocalContext.current

    var showDashboardMenu by remember { mutableStateOf(false) }
    var showDashboardFilter by remember { mutableStateOf(false) }
    var dashboardFilter by remember { mutableStateOf("ALL") }
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(Unit) {
        AccountStore.attachContext(context)
        TransactionStore.attachContext(context)
    }
    val transactions = TransactionStore.transactions

    val filteredTransactions =
        when (dashboardFilter) {
            "INCOME" ->
                transactions.filter {
                    it.kind == com.example.walletapp.data.TransactionKind.NORMAL &&
                        it.type == TransactionType.INCOME
                }

            "EXPENSE" ->
                transactions.filter {
                    it.kind == com.example.walletapp.data.TransactionKind.NORMAL &&
                        it.type == TransactionType.EXPENSE
                }

            else ->
                transactions
        }
    var editingTransaction by remember { mutableStateOf<com.example.walletapp.data.Transaction?>(null) }

    val syncViewModel =
        remember {
            TransactionSyncViewModel(
                    context = context.applicationContext
                )
        }

    val syncState by
        syncViewModel.state.collectAsState()

    val syncPendingCount by
        syncViewModel.pendingCount.collectAsState()

    DisposableEffect(
        lifecycleOwner,
        syncViewModel
    ) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (
                    event ==
                        androidx.lifecycle.Lifecycle.Event.ON_RESUME
                ) {
                    syncViewModel.refreshPendingCount()
                }
            }

        lifecycleOwner.lifecycle.addObserver(observer)

        syncViewModel.refreshPendingCount()

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Totals computed live from the real transaction list, not hardcoded —
    // updates automatically as SMS-detected or manually-added transactions
    // come in.
    // Financial totals use the real ledger model.
    // Internal transfers are deliberately excluded from income/expense.
    val totalIncome =
        transactions
            .asSequence()
            .filter {
                it.kind == com.example.walletapp.data.TransactionKind.NORMAL &&
                    it.type == TransactionType.INCOME
            }
            .sumOf { it.amount }

    val totalExpense =
        transactions
            .asSequence()
            .filter {
                it.kind == com.example.walletapp.data.TransactionKind.NORMAL &&
                    it.type == TransactionType.EXPENSE
            }
            .sumOf { it.amount }

    val totalBalance =
        AccountLedger.totalBalance(
            accounts = AccountStore.accounts,
            transactions = transactions,
        )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("داشبورد", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    Box {
    IconButton(
        onClick = {
            showDashboardMenu = true
        }
    ) {
        Icon(
            Icons.Filled.Menu,
            contentDescription = "منو"
        )
    }

    DropdownMenu(
        expanded = showDashboardMenu,
        onDismissRequest = {
            showDashboardMenu = false
        }
    ) {
        DropdownMenuItem(
            text = {
                Text("حساب‌ها")
            },
            onClick = {
                showDashboardMenu = false
                onNavSelect(1)
            }
        )

        DropdownMenuItem(
            text = {
                Text("گزارشات")
            },
            onClick = {
                showDashboardMenu = false
                onNavSelect(2)
            }
        )

        DropdownMenuItem(
            text = {
                Text("ابزارها")
            },
            onClick = {
                showDashboardMenu = false
                onNavSelect(3)
            }
        )

        DropdownMenuItem(
            text = {
                Text("بودجه")
            },
            onClick = {
                showDashboardMenu = false
                onNavSelect(4)
            }
        )
    }
}
                },
                actions = {
                    Box {
                        IconButton(
                            onClick = {
                                showDashboardFilter = true
                            }
                        ) {
                            Icon(
                                Icons.Filled.Tune,
                                contentDescription = "فیلترها"
                            )
                        }

                        DropdownMenu(
                            expanded = showDashboardFilter,
                            onDismissRequest = {
                                showDashboardFilter = false
                            }
                        ) {
                            DropdownMenuItem(
                                text = { Text("همه تراکنش‌ها") },
                                onClick = {
                                    dashboardFilter = "ALL"
                                    showDashboardFilter = false
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("درآمدها") },
                                onClick = {
                                    dashboardFilter = "INCOME"
                                    showDashboardFilter = false
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("هزینه‌ها") },
                                onClick = {
                                    dashboardFilter = "EXPENSE"
                                    showDashboardFilter = false
                                }
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            DashboardBottomBar(
                items = appNavItems,
                selectedIndex = selectedNavIndex,
                onSelect = onNavSelect,
            )
        },
        floatingActionButton = {
            DashboardFab(onClick = onAddTransactionClick)
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
        ) {
            item {
                BalanceHeroCard(
                    totalBalance = totalBalance,
                    income = totalIncome,
                    expense = totalExpense,
                )
                Spacer(Modifier.height(24.dp))
            }

            item {
                TransactionSyncCard(
                    state = syncState,
                    pendingCount = syncPendingCount,
                    onSyncClick = {
                        syncViewModel.sync()
                    },
                )
                Spacer(Modifier.height(24.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    QuickAction("ثبت", Icons.Filled.Add, SecondaryContainer, OnSecondaryContainer, onClick = onAddTransactionClick)
                    QuickAction("پرداخت", Icons.Filled.Payments, SurfaceContainerHigh, Tertiary, onClick = onAddTransactionClick)
                    QuickAction("بودجه", Icons.Filled.Analytics, SurfaceContainerHigh, Primary, onClick = onOpenBudgetClick)
                    QuickAction("بیشتر", Icons.Filled.MoreHoriz, SurfaceContainerHigh, Outline, onClick = onOpenMoreClick)
                }
                Spacer(Modifier.height(32.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("آخرین تراکنش‌ها", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                    TextButton(onClick = onOpenTransactions) {
                        Text("مشاهده همه", color = Primary, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            items(filteredTransactions, key = { it.id }) { tx ->
                TransactionRow(tx, onClick = { editingTransaction = tx })
                Spacer(Modifier.height(12.dp))
            }

            if (transactions.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            Icons.Outlined.HistoryToggleOff,
                            contentDescription = null,
                            tint = Outline,
                            modifier = Modifier.size(36.dp),
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "هنوز تراکنشی ثبت نشده",
                            color = OnSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    editingTransaction?.let { tx ->
        EditTransactionDialog(
            transaction = tx,
            onDismiss = { editingTransaction = null },
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









