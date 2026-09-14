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
import com.example.walletapp.data.resolveTransactionIcon
import com.example.walletapp.data.toPersianCurrency
import com.example.walletapp.ui.theme.*

/** Hero card: total balance + income/expense summary, teal gradient like the web mock. */
@Composable
fun BalanceHeroCard(
    totalBalance: Long,
    income: Long,
    expense: Long,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(Primary, PrimaryContainer)))
            .padding(24.dp)
    ) {
        Column {
            Text("مانده کل", color = OnPrimary.copy(alpha = 0.8f), style = AppTextStyles.bodyMd)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    totalBalance.toPersianCurrency(),
                    color = OnPrimary,
                    fontFamily = AppFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                )
                Spacer(Modifier.width(8.dp))
                Text("تومان", color = OnPrimary.copy(alpha = 0.8f), style = AppTextStyles.bodyLg)
            }
            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
            Spacer(Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryStat(
                    label = "درآمد",
                    amount = income,
                    icon = Icons.Filled.ArrowDownward,
                    iconColor = PrimaryFixed,
                )
                SummaryStat(
                    label = "هزینه",
                    amount = expense,
                    icon = Icons.Filled.ArrowUpward,
                    iconColor = SecondaryFixedDim,
                )
            }
        }
    }
}

@Composable
private fun SummaryStat(label: String, amount: Long, icon: androidx.compose.ui.graphics.vector.ImageVector, iconColor: Color) {
    Column(horizontalAlignment = Alignment.Start) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(label, color = OnPrimary.copy(alpha = 0.8f), style = AppTextStyles.bodyMd)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            amount.toPersianCurrency(),
            color = OnPrimary,
            style = AppTextStyles.headlineSm.copy(
                fontFamily = AppFontFamily,
                fontSize = 20.sp,
                lineHeight = 30.sp,
            ),
        )
    }
}

/** One item in the 4-column quick-actions bento grid. */
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
            modifier = Modifier.size(56.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = label, tint = iconColor)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = OnSurface)
    }
}

/** A single transaction row card. */
@Composable
fun TransactionRow(transaction: Transaction, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    val isExpense = transaction.type == TransactionType.EXPENSE
    val amountColor = if (isExpense) AppError else OnPrimaryContainer
    val sign = if (isExpense) "-" else "+"

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceContainerLowest,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(resolveTransactionIcon(transaction.iconKey), contentDescription = null, tint = Tertiary)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(transaction.title, fontWeight = FontWeight.Bold, style = AppTextStyles.bodyLg, color = OnSurface)
                    Text(transaction.subtitle, style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                    if (transaction.tags.isNotEmpty()) {
                        Text(
                            transaction.tags.joinToString(" ") { "#$it" },
                            style = MaterialTheme.typography.labelMedium,
                            color = Primary,
                        )
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "$sign${transaction.amount.toPersianCurrency()}",
                    fontWeight = FontWeight.Bold,
                    color = amountColor,
                    style = AppTextStyles.bodyLg,
                )
                Text("تومان", style = MaterialTheme.typography.labelMedium, color = Outline)
            }
        }
    }
}

data class NavItem(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
fun DashboardBottomBar(
    items: List<NavItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier,
        containerColor = SurfaceContainerLow,
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label, style = MaterialTheme.typography.labelMedium) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = OnPrimaryContainer,
                    selectedTextColor = OnPrimaryContainer,
                    indicatorColor = PrimaryContainer,
                    unselectedIconColor = OnSurfaceVariant,
                    unselectedTextColor = OnSurfaceVariant,
                ),
            )
        }
    }
}

@Composable
fun DashboardFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FloatingActionButton(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        containerColor = Secondary,
        contentColor = OnSecondary,
        modifier = modifier.size(64.dp),
    ) {
        Icon(Icons.Filled.Add, contentDescription = "تراکنش جدید")
    }
}

