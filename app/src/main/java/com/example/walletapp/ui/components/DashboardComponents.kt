package com.example.walletapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.walletapp.data.resolveTransactionIcon
import com.example.walletapp.data.toPersianCurrency
import com.example.walletapp.ui.theme.*

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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF004F5A), Color(0xFF008F83))
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
                            "ظ…ظˆط¬ظˆط¯غŒ",
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
                            Text("تومان", color = Color.White.copy(alpha = .75f), fontSize = 12.sp)
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
                    SummaryStat("ط¯ط±ط¢ظ…ط¯", income, Icons.Filled.ArrowDownward, Color(0xFFB8F2D5), Modifier.weight(1f))
                    SummaryStat("ظ‡ط²غŒظ†ظ‡", expense, Icons.Filled.ArrowUpward, Color(0xFFFFB7C0), Modifier.weight(1f))
                    SummaryStat("انتقال", transfer, Icons.Filled.SwapHoriz, Color(0xFFB9D9FF), Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun DashboardMonthlySummaryCard(
    income: Long,
    expense: Long,
    cashFlow: Long,
) {
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
                text = "این ماه",
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
                )

                SummaryStat(
                    label = "هزینه",
                    amount = expense,
                    icon = Icons.Filled.TrendingDown,
                    iconColor = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            SummaryStat(
                label = "جریان نقدی",
                amount = cashFlow,
                icon = Icons.Filled.AccountBalanceWallet,
                iconColor = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.fillMaxWidth(),
            )
        }
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
                color = OnPrimary.copy(alpha = 0.78f),
                style = AppTextStyles.bodyMd,
            )
        }

        Spacer(Modifier.height(5.dp))

        Text(
            text = amount.toPersianCurrency(),
            color = OnPrimary,
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
            shape = RoundedCornerShape(18.dp),
            color = containerColor,
            tonalElevation = 0.dp,
            shadowElevation = 1.dp,
            modifier = Modifier.size(58.dp),
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
        shape = RoundedCornerShape(18.dp),
        color = SurfaceContainerLowest,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
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
        color = Color.White,
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

