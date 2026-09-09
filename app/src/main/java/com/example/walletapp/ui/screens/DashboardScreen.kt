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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onAddTransactionClick: () -> Unit = {},
    onOpenBudgetClick: () -> Unit = {},
    selectedNavIndex: Int = 0,
    onNavSelect: (Int) -> Unit = {},
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        AccountStore.attachContext(context)
        TransactionStore.attachContext(context)
    }
    val transactions = TransactionStore.transactions
    var editingTransaction by remember { mutableStateOf<com.example.walletapp.data.Transaction?>(null) }

    // Totals computed live from the real transaction list, not hardcoded â€”
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
                title = { Text("ط¯ط§ط´ط¨ظˆط±ط¯", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: open menu */ }) {
                        Icon(Icons.Filled.Menu, contentDescription = "ظ…ظ†ظˆ")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: open filters */ }) {
                        Icon(Icons.Filled.Tune, contentDescription = "ظپغŒظ„طھط±ظ‡ط§")
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
                Spacer(Modifier.height(32.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    QuickAction("ط«ط¨طھ", Icons.Filled.Add, SecondaryContainer, OnSecondaryContainer, onClick = onAddTransactionClick)
                    QuickAction("ظ¾ط±ط¯ط§ط®طھ", Icons.Filled.Payments, SurfaceContainerHigh, Tertiary, onClick = {})
                    QuickAction("ط¨ظˆط¯ط¬ظ‡", Icons.Filled.Analytics, SurfaceContainerHigh, Primary, onClick = onOpenBudgetClick)
                    QuickAction("ط¨غŒط´طھط±", Icons.Filled.MoreHoriz, SurfaceContainerHigh, Outline, onClick = {})
                }
                Spacer(Modifier.height(32.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("ط¢ط®ط±غŒظ† طھط±ط§ع©ظ†ط´â€Œظ‡ط§", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                    TextButton(onClick = { /* TODO: view all */ }) {
                        Text("ظ…ط´ط§ظ‡ط¯ظ‡ ظ‡ظ…ظ‡", color = Primary, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            items(transactions, key = { it.id }) { tx ->
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
                            "ظ‡ظ†ظˆط² طھط±ط§ع©ظ†ط´غŒ ط«ط¨طھ ظ†ط´ط¯ظ‡",
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
