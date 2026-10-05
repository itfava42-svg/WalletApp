package com.example.walletapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.walletapp.data.Transaction
import com.example.walletapp.data.TransactionType
import com.example.walletapp.data.TransactionKind
import com.example.walletapp.data.TransactionStore
import com.example.walletapp.data.ReportCalculator
import com.example.walletapp.ui.components.resolveTransactionIcon
import com.example.walletapp.data.toPersianCurrency
import com.example.walletapp.ui.theme.*
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun BalanceHeroCard(
    totalBalance: Long,
    income: Long,
    expense: Long,
    modifier: Modifier = Modifier,
) {
    val transfer = TransactionStore.transactions
        .asSequence()
        .filter { it.kind == TransactionKind.TRANSFER }
        .sumOf { it.amount }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF1E3A8A), Color(0xFF3B5FDB))
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "موجودی",
                            color = Color.White.copy(alpha = .78f),
                            fontSize = 13.sp,
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                totalBalance.toPersianCurrency(),
                                color = Color.White,
                                fontFamily = AppFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 28.sp,
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                 "تومان",
                                 color = Color.White.copy(alpha = .75f),
                                 fontFamily = AppFontFamily,
                                 fontSize = 12.sp,
                             )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = .12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Visibility, contentDescription = "نمایش موجودی", tint = Color.White)
                    }
                }

                Spacer(Modifier.height(18.dp))
                HorizontalDivider(color = Color.White.copy(alpha = .16f))
                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SummaryStat("درآمد", income, Icons.Filled.ArrowDownward, Color(0xFFB8F2D5), Modifier.weight(1f))
                    SummaryStat("هزینه", expense, Icons.Filled.ArrowUpward, Color(0xFFFFB7C0), Modifier.weight(1f))
                    SummaryStat("انتقال", transfer, Icons.Filled.SwapHoriz, Color(0xFFB9D9FF), Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun DashboardPeriodSelector(
    selectedPeriod: com.example.walletapp.ui.screens.DashboardPeriod,
    onPeriodSelected: (
        com.example.walletapp.ui.screens.DashboardPeriod
    ) -> Unit,
) {
    val periods =
        com.example.walletapp.ui.screens.DashboardPeriod.entries

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(
                rememberScrollState()
            )
            .padding(
                horizontal = 16.dp,
                vertical = 4.dp,
            ),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        periods.forEach { period ->

            FilterChip(
                selected = selectedPeriod == period,
                onClick = {
                    onPeriodSelected(period)
                },
                label = {
                    Text(period.title)
                },
            )
        }
    }
}
@Composable
fun DashboardMonthlySummaryCard(
    title: String = "این ماه",
    income: Long,
    expense: Long,
    cashFlow: Long,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
             .padding(horizontal = 0.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SummaryStat(
                    label = "درآمد",
                    amount = income,
                    icon = Icons.Filled.TrendingUp,
                    iconColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    contentColor = OnSurface,
                )

                SummaryStat(
                    label = "هزینه",
                    amount = expense,
                    icon = Icons.Filled.TrendingDown,
                    iconColor = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f),
                    contentColor = OnSurface,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            SummaryStat(
                label = "جریان نقدی",
                amount = cashFlow,
                icon = Icons.Filled.AccountBalanceWallet,
                iconColor = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.fillMaxWidth(),
                contentColor = OnSurface,
            )
        }
    }
}

@Composable
fun DashboardPeriodComparisonCard(
    currentIncome: Long,
    previousIncome: Long,
    currentExpense: Long,
    previousExpense: Long,
) {
    fun changePercent(
        current: Long,
        previous: Long,
    ): Int? {
        if (previous == 0L) {
            return null
        }

        return (
            ((current - previous).toDouble() / previous.toDouble()) * 100.0
        ).toInt()
    }

    val incomeChange =
        changePercent(
            current = currentIncome,
            previous = previousIncome,
        )

    val expenseChange =
        changePercent(
            current = currentExpense,
            previous = previousExpense,
        )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "مقایسه با دوره قبل",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(12.dp))

            ComparisonRow(
                title = "درآمد",
                changePercent = incomeChange,
            )

            Spacer(modifier = Modifier.height(8.dp))

            ComparisonRow(
                title = "هزینه",
                changePercent = expenseChange,
            )
        }
    }
}

@Composable
private fun ComparisonRow(
    title: String,
    changePercent: Int?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
        )

        Text(
            text =
                changePercent?.let {
                    if (it > 0) {
                        "+$it٪"
                    } else {
                        "$it٪"
                    }
                } ?: "—",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}
@Composable
fun DashboardCategorySummaryCard(
    categories: List<ReportCalculator.CategorySummary>,
) {
    if (categories.isEmpty()) {
        return
    }

    val visibleCategories = categories.take(6)
    val displayedTotal = visibleCategories.sumOf { it.expense }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "هزینه‌ها بر اساس دسته‌بندی",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(12.dp))

            visibleCategories.forEach { item ->

                val percentage =
                    if (displayedTotal > 0L) {
                        (
                            item.expense.toDouble() /
                                displayedTotal.toDouble()
                        ) * 100.0
                    } else {
                        0.0
                    }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = item.category,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                    )

                    Text(
                        text = item.expense.toPersianCurrency(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
@Composable
private fun SummaryStat(
    label: String,
    amount: Long,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    contentColor: Color = OnPrimary,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(25.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(14.dp),
                )
            }

            Spacer(Modifier.width(7.dp))

            Text(
                text = label,
                color = contentColor.copy(alpha = 0.78f),
                style = AppTextStyles.bodyMd.copy(
                    fontFamily = AppFontFamily,
                ),
            )
        }

        Spacer(Modifier.height(5.dp))

        Text(
            text = amount.toPersianCurrency(),
            color = contentColor,
            fontFamily = AppFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
        )
    }
}

@Composable
fun QuickAction(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(16.dp),
            color = containerColor,
            tonalElevation = 0.dp,
            shadowElevation = 1.dp,
            modifier = Modifier.size(52.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = label,
            style = AppTextStyles.labelMd.copy(
                fontFamily = AppFontFamily,
            ),
            color = OnSurface,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
fun TransactionRow(
    transaction: Transaction,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val isExpense = transaction.type == TransactionType.EXPENSE
    val amountColor = if (isExpense) AppError else Primary
    val sign = if (isExpense) "-" else "+"

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceContainerLowest,
        tonalElevation = 0.dp,
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 13.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(
                            if (isExpense) {
                                AppError.copy(alpha = 0.10f)
                            } else {
                                Primary.copy(alpha = 0.10f)
                            }
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        resolveTransactionIcon(transaction.iconKey),
                        contentDescription = null,
                        tint = if (isExpense) AppError else Primary,
                        modifier = Modifier.size(22.dp),
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = transaction.title,
                        fontWeight = FontWeight.Bold,
                        style = AppTextStyles.bodyLg,
                        color = OnSurface,
                        maxLines = 1,
                    )

                    if (transaction.subtitle.isNotBlank()) {
                        Spacer(Modifier.height(2.dp))

                        Text(
                            text = transaction.subtitle,
                            style = AppTextStyles.labelMd,
                            color = OnSurfaceVariant,
                            maxLines = 1,
                        )
                    }

                    if (transaction.tags.isNotEmpty()) {
                        Spacer(Modifier.height(3.dp))

                        Text(
                            text = transaction.tags.joinToString(" ") { "#$it" },
                            style = AppTextStyles.labelMd.copy(
                                    fontFamily = AppFontFamily,
                                ),
                            color = Primary,
                            maxLines = 1,
                        )
                    }
                }
            }

            Spacer(Modifier.width(10.dp))

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "$sign${transaction.amount.toPersianCurrency()}",
                    fontWeight = FontWeight.Bold,
                    color = amountColor,
                    style = AppTextStyles.bodyLg,
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = "تومان",
                    style = AppTextStyles.labelMd.copy(
                                    fontFamily = AppFontFamily,
                                ),
                    color = Outline,
                )
            }
        }
    }
}

data class NavItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun DashboardBottomBar(
    items: List<NavItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 10.dp,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEachIndexed { index, item ->
                if (index == 2) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center,
                    ) {
                        FloatingActionButton(
                            onClick = { onSelect(2) },
                            modifier = Modifier.size(54.dp),
                            shape = CircleShape,
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            elevation = FloatingActionButtonDefaults.elevation(
                                defaultElevation = 5.dp,
                                pressedElevation = 2.dp,
                            ),
                        ) {
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = "ثبت تراکنش",
                                modifier = Modifier.size(27.dp),
                            )
                        }
                    }
                } else {
                    NavigationBarItem(
                        selected = index == selectedIndex,
                        onClick = { onSelect(index) },
                        modifier = Modifier.weight(1f),
                        icon = {
                            Icon(
                                item.icon,
                                contentDescription = item.label,
                                modifier = Modifier.size(21.dp),
                            )
                        },
                        label = {
                            Text(
                                item.label,
                                style = AppTextStyles.labelMd.copy(
                                    fontFamily = AppFontFamily,
                                ),
                                fontWeight = if (index == selectedIndex) FontWeight.Bold else FontWeight.Normal,
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = Color.Transparent,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardTrendChart(
    points: List<ReportCalculator.DailyTrendPoint>,
) {
    if (points.isEmpty()) {
        return
    }

    val maxValue =
        points
            .flatMap { listOf(it.income, it.expense) }
            .maxOrNull()
            ?.coerceAtLeast(1L)
            ?: 1L

    val incomeColor = MaterialTheme.colorScheme.primary
    val expenseColor = MaterialTheme.colorScheme.error

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "روند درآمد و هزینه",
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Canvas(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(180.dp),
            ) {
                if (points.size < 2) {
                    return@Canvas
                }

                val horizontalStep =
                    size.width / (points.size - 1).toFloat()

                fun yFor(value: Long): Float {
                    val ratio =
                        value.toFloat() / maxValue.toFloat()

                    return size.height -
                        (ratio * size.height)
                }

                val incomePath = Path()
                val expensePath = Path()

                points.forEachIndexed { index, point ->
                    val x =
                        index * horizontalStep

                    val incomeY = yFor(point.income)
                    val expenseY = yFor(point.expense)

                    if (index == 0) {
                        incomePath.moveTo(x, incomeY)
                        expensePath.moveTo(x, expenseY)
                    } else {
                        incomePath.lineTo(x, incomeY)
                        expensePath.lineTo(x, expenseY)
                    }
                }

                drawPath(
                    path = incomePath,
                    color = incomeColor,
                    style = Stroke(width = 5f),
                )

                drawPath(
                    path = expensePath,
                    color = expenseColor,
                    style = Stroke(width = 5f),
                )

                points.forEachIndexed { index, point ->
                    val x =
                        index * horizontalStep

                    drawCircle(
                        color = incomeColor,
                        radius = 5f,
                        center =
                            Offset(
                                x = x,
                                y = yFor(point.income),
                            ),
                    )

                    drawCircle(
                        color = expenseColor,
                        radius = 5f,
                        center =
                            Offset(
                                x = x,
                                y = yFor(point.expense),
                            ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "● درآمد",
                    style = MaterialTheme.typography.labelMedium,
                    color = incomeColor,
                )

                Spacer(modifier = Modifier.width(20.dp))

                Text(
                    text = "● هزینه",
                    style = MaterialTheme.typography.labelMedium,
                    color = expenseColor,
                )
            }
        }
    }
}
@Composable
// Shared account carousel entry point.
fun DashboardAccountsCard(
    accounts: List<ReportCalculator.AccountSummary>,
) {
    val items = accounts.map { summary ->
        val account = summary.account
        val accountType =
            when (account.type) {
                com.example.walletapp.data.AccountType.BANK -> "بانکی"
                com.example.walletapp.data.AccountType.CASH -> "نقدی"
                com.example.walletapp.data.AccountType.CARD -> "کارت"
                com.example.walletapp.data.AccountType.SAVINGS -> "پس‌انداز"
                com.example.walletapp.data.AccountType.WALLET -> "کیف پول"
            }
        AppBankCardItem(
            title = account.name,
            subtitle = listOfNotNull(
                account.bankName.trim().takeIf { it.isNotBlank() },
                accountType,
            ).joinToString(" • "),
            balance = summary.balance.toPersianCurrency(),
            icon = when (account.type) {
                com.example.walletapp.data.AccountType.BANK -> Icons.Filled.AccountBalance
                com.example.walletapp.data.AccountType.CASH -> Icons.Filled.Payments
                com.example.walletapp.data.AccountType.CARD -> Icons.Filled.CreditCard
                com.example.walletapp.data.AccountType.SAVINGS -> Icons.Filled.Savings
                com.example.walletapp.data.AccountType.WALLET -> Icons.Filled.AccountBalanceWallet
            },
        )
    }
    AppBankCardCarousel(items = items)
}
@Composable
fun DashboardFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = CircleShape,
        modifier = modifier.size(58.dp),
    ) {
        Icon(
            Icons.Filled.Add,
            contentDescription = "تراکنش جدید",
            modifier = Modifier.size(28.dp),
        )
    }
}











