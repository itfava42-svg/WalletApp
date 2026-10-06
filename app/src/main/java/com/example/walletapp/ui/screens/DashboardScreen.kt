package com.example.walletapp.ui.screens

import com.example.walletapp.ui.components.AppTextButton

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
import java.util.Calendar
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.walletapp.data.AccountStore
import com.example.walletapp.data.AccountLedger
import com.example.walletapp.data.ReportCalculator
import com.example.walletapp.data.TransactionStore
import com.example.walletapp.data.TransactionType
import com.example.walletapp.ui.components.resolveTransactionIcon
import com.example.walletapp.ui.theme.AppDimens
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

enum class DashboardPeriod(
    val title: String,
) {
    TODAY("امروز"),
    THIS_WEEK("این هفته"),
    THIS_MONTH("این ماه"),
    LAST_MONTH("ماه قبل"),
}

private fun calculateDashboardPreviousRange(
    period: DashboardPeriod,
): Pair<Long, Long> {
    return when (period) {
        DashboardPeriod.TODAY -> {
            val current = Calendar.getInstance()
            current.add(Calendar.DAY_OF_MONTH, -1)

            val from = current.clone() as Calendar
            val to = current.clone() as Calendar

            from.set(Calendar.HOUR_OF_DAY, 0)
            from.set(Calendar.MINUTE, 0)
            from.set(Calendar.SECOND, 0)
            from.set(Calendar.MILLISECOND, 0)

            to.set(Calendar.HOUR_OF_DAY, 23)
            to.set(Calendar.MINUTE, 59)
            to.set(Calendar.SECOND, 59)
            to.set(Calendar.MILLISECOND, 999)

            from.timeInMillis to to.timeInMillis
        }

        DashboardPeriod.THIS_WEEK -> {
            val current = Calendar.getInstance()

            val dayOfWeek = current.get(Calendar.DAY_OF_WEEK)
            val firstDay = current.firstDayOfWeek
            val daysFromFirst = (dayOfWeek - firstDay + 7) % 7

            val from = current.clone() as Calendar
            val to = current.clone() as Calendar

            from.add(Calendar.DAY_OF_MONTH, -daysFromFirst - 7)
            to.add(Calendar.DAY_OF_MONTH, -daysFromFirst - 1)

            from.set(Calendar.HOUR_OF_DAY, 0)
            from.set(Calendar.MINUTE, 0)
            from.set(Calendar.SECOND, 0)
            from.set(Calendar.MILLISECOND, 0)

            to.set(Calendar.HOUR_OF_DAY, 23)
            to.set(Calendar.MINUTE, 59)
            to.set(Calendar.SECOND, 59)
            to.set(Calendar.MILLISECOND, 999)

            from.timeInMillis to to.timeInMillis
        }

        DashboardPeriod.THIS_MONTH -> {
            val current = Calendar.getInstance()

            val from = current.clone() as Calendar
            val to = current.clone() as Calendar

            from.set(Calendar.DAY_OF_MONTH, 1)
            from.add(Calendar.MONTH, -1)

            to.set(Calendar.DAY_OF_MONTH, 1)
            to.add(Calendar.MONTH, -1)
            to.set(
                Calendar.DAY_OF_MONTH,
                to.getActualMaximum(Calendar.DAY_OF_MONTH)
            )

            from.set(Calendar.HOUR_OF_DAY, 0)
            from.set(Calendar.MINUTE, 0)
            from.set(Calendar.SECOND, 0)
            from.set(Calendar.MILLISECOND, 0)

            to.set(Calendar.HOUR_OF_DAY, 23)
            to.set(Calendar.MINUTE, 59)
            to.set(Calendar.SECOND, 59)
            to.set(Calendar.MILLISECOND, 999)

            from.timeInMillis to to.timeInMillis
        }

        DashboardPeriod.LAST_MONTH -> {
            val current = Calendar.getInstance()

            val from = current.clone() as Calendar
            val to = current.clone() as Calendar

            from.set(Calendar.DAY_OF_MONTH, 1)
            from.add(Calendar.MONTH, -2)

            to.set(Calendar.DAY_OF_MONTH, 1)
            to.add(Calendar.MONTH, -2)
            to.set(
                Calendar.DAY_OF_MONTH,
                to.getActualMaximum(Calendar.DAY_OF_MONTH)
            )

            from.set(Calendar.HOUR_OF_DAY, 0)
            from.set(Calendar.MINUTE, 0)
            from.set(Calendar.SECOND, 0)
            from.set(Calendar.MILLISECOND, 0)

            to.set(Calendar.HOUR_OF_DAY, 23)
            to.set(Calendar.MINUTE, 59)
            to.set(Calendar.SECOND, 59)
            to.set(Calendar.MILLISECOND, 999)

            from.timeInMillis to to.timeInMillis
        }
    }
}
private fun calculateDashboardRange(
    period: DashboardPeriod,
): Pair<Long, Long> {

    val now = Calendar.getInstance()

    fun startOfDay(calendar: Calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
    }

    fun endOfDay(calendar: Calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
    }

    val from = now.clone() as Calendar
    val to = now.clone() as Calendar

    when (period) {

        DashboardPeriod.TODAY -> {
            startOfDay(from)
            endOfDay(to)
        }

        DashboardPeriod.THIS_WEEK -> {
            startOfDay(from)

            val dayOfWeek = from.get(Calendar.DAY_OF_WEEK)
            val firstDay = from.firstDayOfWeek
            val daysFromFirst =
                (dayOfWeek - firstDay + 7) % 7

            from.add(
                Calendar.DAY_OF_MONTH,
                -daysFromFirst,
            )

            startOfDay(from)
            endOfDay(to)
        }

        DashboardPeriod.THIS_MONTH -> {
            from.set(
                Calendar.DAY_OF_MONTH,
                1,
            )

            startOfDay(from)
            endOfDay(to)
        }

        DashboardPeriod.LAST_MONTH -> {
            from.set(
                Calendar.DAY_OF_MONTH,
                1,
            )

            from.add(
                Calendar.MONTH,
                -1,
            )

            startOfDay(from)

            to.set(
                Calendar.DAY_OF_MONTH,
                1,
            )

            to.add(
                Calendar.MONTH,
                -1,
            )

            to.set(
                Calendar.DAY_OF_MONTH,
                to.getActualMaximum(
                    Calendar.DAY_OF_MONTH,
                ),
            )

            endOfDay(to)
        }
    }

    return from.timeInMillis to to.timeInMillis
}
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
    var dashboardPeriod by remember {
        mutableStateOf(DashboardPeriod.THIS_MONTH)
    }

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

    val dashboardRange =
        calculateDashboardRange(dashboardPeriod)

    val dashboardPreviousRange =
        calculateDashboardPreviousRange(dashboardPeriod)
    val dashboardAccountSummaries =
    ReportCalculator.accountSummaries()
val dashboardMonthSummary =
        ReportCalculator.periodSummary(
            fromMillis = dashboardRange.first,
            toMillis = dashboardRange.second,
        )

    val dashboardPreviousSummary =
        ReportCalculator.periodSummary(
            fromMillis = dashboardPreviousRange.first,
            toMillis = dashboardPreviousRange.second,
        )

    val dashboardTrendPoints =
    ReportCalculator.dailyTrend(
        fromMillis = dashboardRange.first,
        toMillis = dashboardRange.second,
    )
val dashboardMonthCategories =
        ReportCalculator.categorySummary(
            fromMillis = dashboardRange.first,
            toMillis = dashboardRange.second,
        )

    val dashboardExpenseCategories =
        dashboardMonthCategories
            .filter { it.expense > 0L }
            .sortedByDescending { it.expense }
    val totalBalance =
        AccountLedger.totalBalance(
            accounts = AccountStore.accounts,
            transactions = transactions,
        )

    Scaffold(
        topBar = {
    CenterAlignedTopAppBar(
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("سلام، مطلب 👋", fontWeight = FontWeight.Bold)
                Text(
                    "مدیریت مالی شما",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        navigationIcon = {
            AppIconButton(onClick = { showDashboardMenu = true }) {
                Icon(Icons.Filled.AccountCircle, contentDescription = "پروفایل", tint = MaterialTheme.colorScheme.primary)
            }
        },
        actions = {
            AppIconButton(onClick = { showDashboardFilter = true }) {
                Icon(Icons.Filled.NotificationsNone, contentDescription = "اعلان‌ها")
            }
            DropdownMenu(
                expanded = showDashboardFilter,
                onDismissRequest = { showDashboardFilter = false },
            ) {
                DropdownMenuItem(text = { Text("همه تراکنش‌ها") }, onClick = { dashboardFilter = "ALL"; showDashboardFilter = false })
                DropdownMenuItem(text = { Text("درآمدها") }, onClick = { dashboardFilter = "INCOME"; showDashboardFilter = false })
                DropdownMenuItem(text = { Text("هزینه‌ها") }, onClick = { dashboardFilter = "EXPENSE"; showDashboardFilter = false })
            }
            DropdownMenu(
                expanded = showDashboardMenu,
                onDismissRequest = { showDashboardMenu = false },
            ) {
                DropdownMenuItem(text = { Text("حساب‌ها") }, onClick = { showDashboardMenu = false; onNavSelect(3) })
                DropdownMenuItem(text = { Text("گزارشات") }, onClick = { showDashboardMenu = false; onOpenMoreClick() })
                DropdownMenuItem(text = { Text("ابزارها") }, onClick = { showDashboardMenu = false; onNavSelect(4) })
                DropdownMenuItem(text = { Text("بودجه") }, onClick = { showDashboardMenu = false; onOpenBudgetClick() })
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
                .padding(horizontal = AppDimens.PagePadding),
            contentPadding = PaddingValues(vertical = AppDimens.PagePadding),
        ) {
            item {
                BalanceHeroCard(
                    totalBalance = totalBalance,
                    income = totalIncome,
                    expense = totalExpense,
                )
                Spacer(Modifier.height(AppDimens.ItemGap))
            }

            item {
                DashboardPeriodSelector(
                    selectedPeriod = dashboardPeriod,
                    onPeriodSelected = {
                        dashboardPeriod = it
                    },
                )
                DashboardAccountsCard(
                    accounts = dashboardAccountSummaries,
                )

                DashboardMonthlySummaryCard(
                    title = dashboardPeriod.title,
                    income = dashboardMonthSummary.income,
                    expense = dashboardMonthSummary.expense,
                    cashFlow = dashboardMonthSummary.net,
                )

        DashboardTrendChart(
            points = dashboardTrendPoints,
        )
        DashboardPeriodComparisonCard(


            currentIncome = dashboardMonthSummary.income,


            previousIncome = dashboardPreviousSummary.income,


            currentExpense = dashboardMonthSummary.expense,


            previousExpense = dashboardPreviousSummary.expense,


        )



        DashboardCategorySummaryCard(


            categories = dashboardExpenseCategories,


        )
        TransactionSyncCard(
                    state = syncState,
                    pendingCount = syncPendingCount,
                    onSyncClick = {
                        syncViewModel.sync()
                    },
                )
                Spacer(Modifier.height(AppDimens.ItemGap))
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
                Spacer(Modifier.height(AppDimens.SectionGap))
            }

            item {
                AppSectionHeader(
                    title = "آخرین تراکنش‌ها",
                    action = {
                        AppTextButton(onClick = onOpenTransactions) {
                            Text("مشاهده همه")
                        }
                    },
                )
                Spacer(Modifier.height(AppDimens.ItemGap))
            }

            items(filteredTransactions, key = { it.id }) { tx ->
                TransactionRow(tx, onClick = { editingTransaction = tx })
                Spacer(Modifier.height(AppDimens.ItemGap))
            }

            if (transactions.isEmpty()) {
                item {
                    AppEmptyState(
                        title = "هنوز تراکنشی ثبت نشده",
                        icon = Icons.Outlined.HistoryToggleOff,
                    )
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

























