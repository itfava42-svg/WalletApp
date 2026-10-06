package com.example.walletapp.ui.screens

import com.example.walletapp.data.toPersianCurrency

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.walletapp.data.ReportCalculator
import com.example.walletapp.ui.components.AppCard
import com.example.walletapp.ui.components.AppOutlinedButton
import com.example.walletapp.ui.components.AppPrimaryButton
import com.example.walletapp.ui.components.DashboardBottomBar
import com.example.walletapp.ui.theme.AppDimens
import com.example.walletapp.ui.components.appNavItems
import java.util.Calendar
private enum class ReportPeriod(
    val title: String
) {
    TODAY("امروز"),
    THIS_WEEK("این هفته"),
    THIS_MONTH("این ماه"),
    LAST_MONTH("ماه قبل"),
}

@Composable
fun ReportsScreen(
    selectedNavIndex: Int,
    onNavSelect: (Int) -> Unit,
) {
var selectedPeriod by remember {
        mutableStateOf(ReportPeriod.THIS_MONTH)
    }

    val range = calculateRange(selectedPeriod)

    val summary = ReportCalculator.periodSummary(
        fromMillis = range.first,
        toMillis = range.second,
    )

    val categories = ReportCalculator.categorySummary(
        fromMillis = range.first,
        toMillis = range.second,
    )

    val accounts = ReportCalculator.accountSummaries(
        fromMillis = range.first,
        toMillis = range.second,
    )

    val currentMonthRange = calculateRange(ReportPeriod.THIS_MONTH)
    val previousMonthRange = calculateRange(ReportPeriod.LAST_MONTH)

    val currentMonthSummary = ReportCalculator.periodSummary(
        fromMillis = currentMonthRange.first,
        toMillis = currentMonthRange.second,
    )

    val previousMonthSummary = ReportCalculator.periodSummary(
        fromMillis = previousMonthRange.first,
        toMillis = previousMonthRange.second,
    )

    val currentMonthCategories = ReportCalculator.categorySummary(
        fromMillis = currentMonthRange.first,
        toMillis = currentMonthRange.second,
    )

    val previousMonthCategories = ReportCalculator.categorySummary(
        fromMillis = previousMonthRange.first,
        toMillis = previousMonthRange.second,
    )

    val financialHealthScore = calculateFinancialHealthScore(
        current = currentMonthSummary,
        previous = previousMonthSummary,
    )

    val smartFinancialAlerts = buildSmartFinancialAlerts(
        current = currentMonthSummary,
        previous = previousMonthSummary,
        categories = currentMonthCategories,
    )
    val p7MonthlySummaries = buildP7MonthlySummaries()

val p7ExpenseCategories = currentMonthCategories.filter { it.expense > 0 }

    val totalBalance = ReportCalculator.currentTotalBalance()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            DashboardBottomBar(
                items = appNavItems,
                selectedIndex = selectedNavIndex,
                onSelect = onNavSelect,
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = AppDimens.PagePadding)
        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
Spacer(modifier = Modifier.weight(1f))

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "مرکز گزارش مالی",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = selectedPeriod.title,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            ReportPeriod.entries.forEach { period ->
                if (period == selectedPeriod) {
                    AppPrimaryButton(
                        modifier = Modifier.weight(1f),
                        text = "● ${period.title}",
                        onClick = { selectedPeriod = period },
                    )
                } else {
                    AppOutlinedButton(
                        modifier = Modifier.weight(1f),
                        text = period.title,
                        onClick = { selectedPeriod = period },
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 0.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                HeroFinancialCard(
                    totalBalance = totalBalance,
                    income = summary.income,
                    expense = summary.expense,
                    net = summary.net,
                )
            }

            item {
                FinancialHealthCard(
                    score = financialHealthScore,
                    current = currentMonthSummary,
                    previous = previousMonthSummary,
                )
            }

            item {
                SmartAlertsCard(
                    alerts = smartFinancialAlerts,
                )
            }
            item {
                SmartInsightCard(
                    income = summary.income,
                    expense = summary.expense,
                    net = summary.net,
                    transactionCount = summary.transactionCount,
                    categories = categories,
                )
            }

            item {
                SectionTitle("جریان مالی")
            }

            item {
                FlowCard(
                    income = summary.income,
                    expense = summary.expense,
                    transferIn = summary.transferIn,
                    transferOut = summary.transferOut,
                )
            }

            item {
                AnalyticsCard(
                    income = summary.income,
                    expense = summary.expense,
                    transactionCount = summary.transactionCount,
                    categories = categories,
                )
            }
            item {
                P7FinancialTrendChart(
                    data = p7MonthlySummaries,
                )
            }
            item {
                P7ExpenseDonutChart(
                    categories = p7ExpenseCategories,
                )
            }
            item {
                P7AccountBalanceChart(
                    accounts = accounts,
                )
            }
            item {
                MonthComparisonCard(
                    current = currentMonthSummary,
                    previous = previousMonthSummary,
                )
            }

            item {
                CategoryGrowthCard(
                    currentCategories = currentMonthCategories,
                    previousCategories = previousMonthCategories,
                )
            }

            item {
                SectionTitle("دسته‌بندی هزینه و درآمد")
            }

            if (categories.isEmpty()) {
                item {
                    EmptyCard("در این بازه تراکنشی ثبت نشده است.")
                }
            } else {
                items(categories) { category ->
                    CategoryRow(category)
                }
            }

            item {
                SectionTitle("وضعیت حساب‌ها")
            }

            if (accounts.isEmpty()) {
                item {
                    EmptyCard("حساب فعالی وجود ندارد.")
                }
            } else {
                items(accounts) { account ->
                    AccountRow(account)
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
    }
}

private data class SmartFinancialAlert(
    val title: String,
    val message: String,
    val level: Int,
)

private fun buildSmartFinancialAlerts(
    current: ReportCalculator.PeriodSummary,
    previous: ReportCalculator.PeriodSummary,
    categories: List<ReportCalculator.CategorySummary>,
): List<SmartFinancialAlert> {

    val alerts = mutableListOf<SmartFinancialAlert>()

    val expenseChange =
        if (previous.expense > 0L) {
            (current.expense - previous.expense).toDouble() /
                previous.expense.toDouble() * 100.0
        } else {
            0.0
        }

    val incomeChange =
        if (previous.income > 0L) {
            (current.income - previous.income).toDouble() /
                previous.income.toDouble() * 100.0
        } else {
            0.0
        }

    if (current.expense > current.income && current.expense > 0L) {
        alerts.add(
            SmartFinancialAlert(
                title = "کسری جریان مالی",
                message = "هزینه‌های این ماه از درآمد بیشتر شده است.",
                level = 3,
            )
        )
    }

    if (expenseChange >= 20.0) {
        alerts.add(
            SmartFinancialAlert(
                title = "افزایش هزینه",
                message = "هزینه‌ها نسبت به ماه قبل ${formatPercent(expenseChange)}٪ افزایش داشته‌اند.",
                level = 2,
            )
        )
    }

    if (incomeChange <= -20.0) {
        alerts.add(
            SmartFinancialAlert(
                title = "کاهش درآمد",
                message = "درآمد نسبت به ماه قبل ${formatPercent(kotlin.math.abs(incomeChange))}٪ کاهش داشته است.",
                level = 2,
            )
        )
    }

    val totalExpense = categories.sumOf { it.expense }

    val topCategory = categories
        .filter { it.expense > 0L }
        .maxByOrNull { it.expense }

    if (topCategory != null && totalExpense > 0L) {
        val topShare =
            topCategory.expense.toDouble() /
                totalExpense.toDouble() * 100.0

        if (topShare >= 35.0) {
            alerts.add(
                SmartFinancialAlert(
                    title = "تمرکز هزینه",
                    message = "${topCategory.category} حدود ${formatPercent(topShare)}٪ از کل هزینه‌های این ماه را تشکیل می‌دهد.",
                    level = 1,
                )
            )
        }
    }

    if (current.income > previous.income &&
        current.expense < previous.expense
    ) {
        alerts.add(
            SmartFinancialAlert(
                title = "روند مثبت",
                message = "درآمد افزایش یافته و هزینه‌ها نسبت به ماه قبل کاهش پیدا کرده‌اند.",
                level = 0,
            )
        )
    }

    if (alerts.isEmpty()) {
        alerts.add(
            SmartFinancialAlert(
                title = "وضعیت پایدار",
                message = "در حال حاضر هشدار مهمی در الگوی مالی این ماه دیده نمی‌شود.",
                level = 0,
            )
        )
    }

    return alerts.sortedByDescending { it.level }
}
private fun calculateFinancialHealthScore(
    current: ReportCalculator.PeriodSummary,
    previous: ReportCalculator.PeriodSummary,
): Int {

    var score = 50.0

    // درآمد نسبت به هزینه
    if (current.income > 0L) {
        val expenseRatio =
            current.expense.toDouble() / current.income.toDouble()

        score += when {
            expenseRatio <= 0.30 -> 25.0
            expenseRatio <= 0.50 -> 18.0
            expenseRatio <= 0.70 -> 10.0
            expenseRatio <= 0.90 -> 2.0
            else -> -12.0
        }
    } else if (current.expense > 0L) {
        score -= 20.0
    }

    // مثبت بودن خالص
    score += when {
        current.net > 0L -> 10.0
        current.net == 0L -> 0.0
        else -> -12.0
    }

    // روند درآمد
    if (previous.income > 0L) {
        val incomeChange =
            ((current.income - previous.income).toDouble() /
                previous.income.toDouble()) * 100.0

        score += when {
            incomeChange >= 10.0 -> 5.0
            incomeChange > 0.0 -> 3.0
            incomeChange <= -10.0 -> -5.0
            else -> -2.0
        }
    }

    // روند هزینه
    if (previous.expense > 0L) {
        val expenseChange =
            ((current.expense - previous.expense).toDouble() /
                previous.expense.toDouble()) * 100.0

        score += when {
            expenseChange <= -10.0 -> 5.0
            expenseChange < 0.0 -> 3.0
            expenseChange >= 20.0 -> -7.0
            expenseChange > 0.0 -> -3.0
            else -> 0.0
        }
    }

    return score
        .coerceIn(0.0, 100.0)
        .toInt()
}

@Composable
private fun FinancialHealthCard(
    score: Int,
    current: ReportCalculator.PeriodSummary,
    previous: ReportCalculator.PeriodSummary,
) {
    val status = when {
        score >= 85 -> "عالی"
        score >= 70 -> "خوب"
        score >= 55 -> "متوسط"
        score >= 40 -> "نیازمند توجه"
        else -> "بحرانی"
    }

    val message = when {
        score >= 85 ->
            "وضعیت مالی بسیار مطلوب است و درآمد نسبت به هزینه‌ها کنترل خوبی دارد."

        score >= 70 ->
            "وضعیت مالی مناسب است؛ با کنترل هزینه‌ها می‌توان امتیاز را بهتر کرد."

        score >= 55 ->
            "وضعیت مالی قابل قبول است، اما بهتر است روند هزینه‌ها کنترل شود."

        score >= 40 ->
            "فشار مالی قابل توجه است؛ بررسی هزینه‌ها و افزایش درآمد پیشنهاد می‌شود."

        else ->
            "هشدار مالی: هزینه‌ها نسبت به درآمد بالا هستند و نیاز به مدیریت جدی وجود دارد."
    }

    val trendText = when {
        current.net > previous.net ->
            "خالص مالی نسبت به ماه قبل بهتر شده است."

        current.net < previous.net ->
            "خالص مالی نسبت به ماه قبل ضعیف‌تر شده است."

        else ->
            "خالص مالی نسبت به ماه قبل تغییر محسوسی نداشته است."
    }

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "سلامت مالی",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )

                    Text(
                        text = status,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                }

                Text(
                    text = "$score / 100",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
            }

            LinearProgressIndicator(
                progress = { score / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50)),
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
            )

            AppCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
            ) {
                Text(
                    text = trendText,
                    modifier = Modifier.padding(13.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}
private data class P7MonthlySummary(
    val label: String,
    val income: Long,
    val expense: Long,
    val net: Long,
)

private fun buildP7MonthlySummaries(): List<P7MonthlySummary> {
    val calendar = java.util.Calendar.getInstance()
    val result = mutableListOf<P7MonthlySummary>()

    repeat(6) {
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH)

        val start = calendar.clone() as java.util.Calendar
        start.set(java.util.Calendar.DAY_OF_MONTH, 1)
        start.set(java.util.Calendar.HOUR_OF_DAY, 0)
        start.set(java.util.Calendar.MINUTE, 0)
        start.set(java.util.Calendar.SECOND, 0)
        start.set(java.util.Calendar.MILLISECOND, 0)

        val end = start.clone() as java.util.Calendar
        end.add(java.util.Calendar.MONTH, 1)
        end.add(java.util.Calendar.SECOND, -1)

        val summary = ReportCalculator.periodSummary(
            fromMillis = start.timeInMillis,
            toMillis = end.timeInMillis,
        )

        result.add(
            0,
            P7MonthlySummary(
                label = "${month + 1}/$year",
                income = summary.income,
                expense = summary.expense,
                net = summary.net,
            ),
        )

        calendar.add(java.util.Calendar.MONTH, -1)
    }

    return result
}
@Composable
private fun P7FinancialTrendChart(
    data: List<P7MonthlySummary>,
) {
    if (data.isEmpty()) return

    val minValue = data.minOf {
        minOf(
            it.income,
            it.expense,
            it.net,
        )
    }.coerceAtMost(0L)

    val maxValue = data.maxOf {
        maxOf(
            it.income,
            it.expense,
            it.net,
        )
    }.coerceAtLeast(1L)

    val valueRange =
        (maxValue - minValue).coerceAtLeast(1L)

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "روند مالی ۶ ماه اخیر",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = "مقایسه درآمد، هزینه و خالص جریان مالی",
                style = MaterialTheme.typography.bodySmall,
            )

            val incomeColor = MaterialTheme.colorScheme.primary
            val expenseColor = MaterialTheme.colorScheme.error
            val netColor = MaterialTheme.colorScheme.tertiary
            val gridColor = MaterialTheme.colorScheme.outlineVariant
            val zeroAxisColor = MaterialTheme.colorScheme.outline

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp),
            ) {
                val left = 16f
                val right = size.width - 16f
                val top = 16f
                val bottom = size.height - 28f
                val width = right - left
                val height = bottom - top
                val step = if (data.size > 1) width / (data.size - 1) else width

                fun x(index: Int): Float =
                    left + index * step

                fun y(value: Long): Float =
                    bottom - (
                        (value - minValue).toFloat() /
                            valueRange.toFloat()
                    ) * height

                for (i in 0..4) {
                    val gy = top + height * i / 4f
                    drawLine(
                        color = gridColor,
                        start = androidx.compose.ui.geometry.Offset(left, gy),
                        end = androidx.compose.ui.geometry.Offset(right, gy),
                        strokeWidth = 1f,
                    )
                }

                if (minValue < 0L && maxValue > 0L) {
                    val zeroY = y(0L)

                    drawLine(
                        color = zeroAxisColor,
                        start = androidx.compose.ui.geometry.Offset(left, zeroY),
                        end = androidx.compose.ui.geometry.Offset(right, zeroY),
                        strokeWidth = 2f,
                    )
                }

                fun drawSeries(
                    values: List<Long>,
                    strokeWidth: Float,
                    seriesColor: androidx.compose.ui.graphics.Color,
                ) {
                    if (values.isEmpty()) return

                    val path = androidx.compose.ui.graphics.Path()

                    values.forEachIndexed { index, value ->
                        val px = x(index)
                        val py = y(value)

                        if (index == 0) {
                            path.moveTo(px, py)
                        } else {
                            path.lineTo(px, py)
                        }
                    }

                    drawPath(
                        path = path,
                        color = seriesColor,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth),
                    )

                    values.forEachIndexed { index, value ->
                        drawCircle(
                            color = seriesColor,
                            radius = 5f,
                            center = androidx.compose.ui.geometry.Offset(
                                x(index),
                                y(value),
                            ),
                        )
                    }
                }

                drawSeries(data.map { it.income }, 5f, incomeColor)
                drawSeries(data.map { it.expense }, 4f, expenseColor)
                drawSeries(data.map { it.net }, 3f, netColor)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "●",
                        color = incomeColor,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "درآمد",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "●",
                        color = expenseColor,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "هزینه",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "●",
                        color = netColor,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "خالص",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                data.forEach { item ->
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
@Composable
private fun P7ExpenseDonutChart(
    categories: List<ReportCalculator.CategorySummary>,
) {
    val totalExpense = categories.sumOf { it.expense }

    if (totalExpense <= 0L) {
        EmptyCard("در این ماه هزینه‌ای برای نمایش نمودار وجود ندارد.")
        return
    }

    val topCategories = categories
        .sortedByDescending { it.expense }
        .take(6)

    val otherAmount = (totalExpense - topCategories.sumOf { it.expense }).coerceAtLeast(0L)

    val chartItems = buildList {
        addAll(topCategories)
        if (otherAmount > 0L) {
            add(
                ReportCalculator.CategorySummary(
                    category = "سایر",
                    income = 0L,
                    expense = otherAmount,
                    transactionCount = 0,
                )
            )
        }
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val outlineColor = MaterialTheme.colorScheme.outline
    val colors = listOf(
        primaryColor,
        errorColor,
        tertiaryColor,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.primaryContainer,
        outlineColor,
        MaterialTheme.colorScheme.surfaceVariant,
    )

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = "ترکیب هزینه‌ها",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "سهم هر دسته از هزینه‌های این ماه",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Canvas(
                        modifier = Modifier.size(170.dp),
                    ) {
                        val strokeWidth = 28.dp.toPx()

                        drawArc(
                            color = trackColor,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = strokeWidth,
                            ),
                        )

                        var startAngle = -90f

                        chartItems.forEachIndexed { index, item ->
                            val sweep =
                                (item.expense.toFloat() / totalExpense.toFloat()) * 360f

                            drawArc(
                                color = colors[index % colors.size],
                                startAngle = startAngle + 1.5f,
                                sweepAngle = (sweep - 3f).coerceAtLeast(0f),
                                useCenter = false,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(
                                    width = strokeWidth,
                                ),
                            )

                            startAngle += sweep
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "هزینه",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Text(
                            text = formatMoney(totalExpense),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    chartItems.take(5).forEachIndexed { index, item ->
                        val percent =
                            item.expense.toDouble() / totalExpense.toDouble() * 100.0

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(
                                        colors[index % colors.size],
                                        RoundedCornerShape(50),
                                    ),
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(
                                    text = item.category,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                )

                                Text(
                                    text = formatMoney(item.expense),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }

                            Text(
                                text = "${formatPercent(percent)}٪",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun P7AccountBalanceChart(
    accounts: List<ReportCalculator.AccountSummary>,
) {
    if (accounts.isEmpty()) {
        EmptyCard("حسابی برای نمایش وجود ندارد.")
        return
    }

    val sortedAccounts = accounts
        .sortedByDescending { it.balance }
        .take(6)

    val maxBalance = sortedAccounts
        .maxOfOrNull { kotlin.math.abs(it.balance) }
        ?.coerceAtLeast(1L)
        ?: 1L

    val totalPositiveBalance = sortedAccounts
        .filter { it.balance > 0L }
        .sumOf { it.balance }

    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val outlineColor = MaterialTheme.colorScheme.outlineVariant

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = "مقایسه موجودی حساب‌ها",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "توزیع موجودی فعلی بین حساب‌های شما",
                style = MaterialTheme.typography.bodyMedium,
                color = onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(18.dp))

            sortedAccounts.forEachIndexed { index, account ->
                val balance = account.balance

                val progress =
                    (kotlin.math.abs(balance).toFloat() / maxBalance.toFloat())
                        .coerceIn(0f, 1f)

                val share =
                    if (balance > 0L && totalPositiveBalance > 0L) {
                        balance.toDouble() / totalPositiveBalance.toDouble() * 100.0
                    } else {
                        0.0
                    }

                val barColor =
                    if (balance < 0L) errorColor else primaryColor

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(
                                    color = if (balance < 0L) {
                                        errorColor.copy(alpha = 0.12f)
                                    } else {
                                        primaryColor.copy(alpha = 0.12f)
                                    },
                                    shape = RoundedCornerShape(9.dp),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = barColor,
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = account.account.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                            )

                            if (account.account.bankName.isNotBlank()) {
                                Text(
                                    text = account.account.bankName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = onSurfaceVariant,
                                    maxLines = 1,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(
                            horizontalAlignment = Alignment.End,
                        ) {
                            Text(
                                text = formatMoney(balance),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = barColor,
                            )

                            if (share > 0.0) {
                                Text(
                                    text = "${formatPercent(share)}٪",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = onSurfaceVariant,
                                )
                            } else if (balance < 0L) {
                                Text(
                                    text = "منفی",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = errorColor,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(7.dp))

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp),
                    ) {
                        val radius = 8.dp.toPx()

                        drawRoundRect(
                            color = trackColor,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                                radius,
                                radius,
                            ),
                        )

                        drawRoundRect(
                            color = outlineColor.copy(alpha = 0.35f),
                            size = androidx.compose.ui.geometry.Size(
                                width = size.width,
                                height = 1.dp.toPx(),
                            ),
                        )

                        drawRoundRect(
                            color = barColor,
                            size = androidx.compose.ui.geometry.Size(
                                width = size.width * progress,
                                height = size.height,
                            ),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                                radius,
                                radius,
                            ),
                        )
                    }
                }
            }

            if (accounts.size > 6) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "۶ حساب با بیشترین موجودی نمایش داده شده‌اند.",
                    style = MaterialTheme.typography.bodySmall,
                    color = onSurfaceVariant,
                )
            }
        }
    }
}
@Composable
private fun AnalyticsCard(
    income: Long,
    expense: Long,
    transactionCount: Int,
    categories: List<ReportCalculator.CategorySummary>,
) {
    val expenseCategories = categories
        .filter { it.expense > 0L }
        .sortedByDescending { it.expense }

    val totalExpense = expenseCategories.sumOf { it.expense }
    val topCategory = expenseCategories.firstOrNull()

    val averageExpense = if (transactionCount > 0) {
        expense / transactionCount
    } else {
        0L
    }

    val expenseRatio = if (income > 0L) {
        (expense.toDouble() / income.toDouble()) * 100.0
    } else {
        0.0
    }

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "تحلیل عملکرد مالی",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MetricBox(
                    title = "بیشترین هزینه",
                    value = topCategory?.category ?: "—",
                    modifier = Modifier.weight(1f),
                )

                MetricBox(
                    title = "میانگین تراکنش",
                    value = formatMoney(averageExpense),
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MetricBox(
                    title = "مبلغ بیشترین دسته",
                    value = formatMoney(topCategory?.expense ?: 0L),
                    modifier = Modifier.weight(1f),
                )

                MetricBox(
                    title = "هزینه نسبت به درآمد",
                    value = if (income > 0L) {
                        formatPercent(expenseRatio)
                    } else {
                        "—"
                    },
                    modifier = Modifier.weight(1f),
                )
            }

            if (expenseCategories.isNotEmpty() && totalExpense > 0L) {
                Text(
                    text = "توزیع هزینه‌ها",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )

                expenseCategories.take(6).forEach { category ->
                    ExpenseBar(
                        title = category.category,
                        amount = category.expense,
                        total = totalExpense,
                    )
                }
            } else {
                Text(
                    text = "برای این بازه هزینه‌ای ثبت نشده است.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MetricBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    AppCard(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
    ) {
        Column(
            modifier = Modifier.padding(13.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun ExpenseBar(
    title: String,
    amount: Long,
    total: Long,
) {
    val fraction = if (total > 0L) {
        (amount.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    val percent = fraction * 100f

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
            )

            Text(
                text = "  •  ",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(7.dp))
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
    }
}

private fun formatPercent(value: Double): String {
    val rounded = String.format("%.1f", value)
    return "٪"
}
@Composable
private fun HeroFinancialCard(
    totalBalance: Long,
    income: Long,
    expense: Long,
    net: Long,
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "تصویر کلی وضعیت مالی",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = formatMoney(totalBalance),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
            )

            Text(
                text = "موجودی کل فعلی",
                style = MaterialTheme.typography.bodySmall,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MiniMetric(
                    modifier = Modifier.weight(1f),
                    title = "درآمد",
                    value = formatMoney(income),
                )

                MiniMetric(
                    modifier = Modifier.weight(1f),
                    title = "هزینه",
                    value = formatMoney(expense),
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "خالص دوره",
                        fontWeight = FontWeight.Bold,
                    )

                    Text(
                        text = formatMoney(net),
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniMetric(
    modifier: Modifier,
    title: String,
    value: String,
) {
    AppCard(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun SmartAlertsCard(
    alerts: List<SmartFinancialAlert>,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val warningColor = MaterialTheme.colorScheme.tertiary
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = "هشدارهای هوشمند",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "تحلیل خودکار وضعیت مالی این ماه",
                style = MaterialTheme.typography.bodyMedium,
                color = onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(14.dp))

            alerts.forEachIndexed { index, alert ->

                val alertColor = when (alert.level) {
                    3 -> errorColor
                    2 -> warningColor
                    1 -> warningColor
                    else -> primaryColor
                }

                val backgroundColor =
                    alertColor.copy(alpha = 0.10f)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = backgroundColor,
                                            )
                        .padding(14.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(
                                color = alertColor.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(11.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = when (alert.level) {
                                3 -> "!"
                                2 -> "!"
                                1 -> "•"
                                else -> "✓"
                            },
                            color = alertColor,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = alert.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = alertColor,
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = alert.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = onSurfaceVariant,
                        )
                    }
                }

                if (index < alerts.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun SmartInsightCard(
    income: Long,
    expense: Long,
    net: Long,
    transactionCount: Int,
    categories: List<ReportCalculator.CategorySummary>,
) {
    val message = when {
        transactionCount == 0 ->
            "در این بازه هنوز تراکنشی برای تحلیل وجود ندارد."

        income == 0L && expense > 0L ->
            "در این دوره هزینه ثبت شده اما درآمدی ثبت نشده است."

        expense == 0L && income > 0L ->
            "در این دوره درآمد ثبت شده و هزینه‌ای ثبت نشده است."

        net > 0L ->
            "وضعیت این دوره مثبت است؛ درآمد شما از هزینه بیشتر بوده است."

        net < 0L ->
            "هزینه‌های این دوره از درآمد بیشتر بوده؛ بهتر است بخش‌های پرهزینه را بررسی کنید."

        else ->
            "درآمد و هزینه این دوره تقریباً برابر هستند."
    }

    val topCategory = categories
        .maxByOrNull { maxOf(it.income, it.expense) }

    AppCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "تحلیل هوشمند مالی",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
            )

            if (topCategory != null) {
                Text(
                    text = "دسته شاخص: ${topCategory.category}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
) {
    Text(
        text = title,
        modifier = Modifier.padding(
            start = 4.dp,
            top = 4.dp,
        ),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun FlowCard(
    income: Long,
    expense: Long,
    transferIn: Long,
    transferOut: Long,
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            FlowLine("درآمد", income)
            FlowLine("هزینه", expense)
            FlowLine("انتقال ورودی", transferIn)
            FlowLine("انتقال خروجی", transferOut)
        }
    }
}

@Composable
private fun FlowLine(
    label: String,
    value: Long,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label)

        Text(
            text = formatMoney(value),
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun MonthComparisonCard(
    current: ReportCalculator.PeriodSummary,
    previous: ReportCalculator.PeriodSummary,
) {
    val incomeChange = percentageChange(
        previous.income,
        current.income,
    )

    val expenseChange = percentageChange(
        previous.expense,
        current.expense,
    )

    val netChange = percentageChange(
        previous.net,
        current.net,
    )

    val insight = when {
        current.income > previous.income &&
            current.expense < previous.expense ->
            "عملکرد این ماه بهتر شده؛ درآمد افزایش و هزینه کاهش یافته است."

        current.income > previous.income &&
            current.expense > previous.expense ->
            "درآمد افزایش یافته، اما هزینه‌ها نیز بیشتر شده‌اند."

        current.income < previous.income &&
            current.expense < previous.expense ->
            "هزینه‌ها کاهش یافته‌اند، اما درآمد نیز نسبت به ماه قبل کمتر شده است."

        current.income < previous.income &&
            current.expense > previous.expense ->
            "هشدار مالی: درآمد کاهش و هزینه‌ها افزایش یافته‌اند."

        else ->
            "وضعیت مالی این ماه نسبت به ماه قبل تغییر محسوسی ندارد."
    }

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "مقایسه با ماه قبل",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            ComparisonRow(
                title = "درآمد",
                current = current.income,
                previous = previous.income,
                change = incomeChange,
            )

            ComparisonRow(
                title = "هزینه",
                current = current.expense,
                previous = previous.expense,
                change = expenseChange,
            )

            ComparisonRow(
                title = "خالص",
                current = current.net,
                previous = previous.net,
                change = netChange,
            )

            AppCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
            ) {
                Text(
                    text = insight,
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun ComparisonRow(
    title: String,
    current: Long,
    previous: Long,
    change: Double?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = formatMoney(current),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "ماه قبل: ${formatMoney(previous)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = when {
                    change == null -> "جدید"
                    change > 0.0 -> "▲ ${formatPercent(change)}"
                    change < 0.0 -> "▼ ${formatPercent(kotlin.math.abs(change))}"
                    else -> "بدون تغییر"
                },
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

private fun percentageChange(
    previous: Long,
    current: Long,
): Double? {
    if (previous == 0L) {
        return if (current == 0L) 0.0 else null
    }

    return ((current - previous).toDouble() / previous.toDouble()) * 100.0
}

private data class CategoryGrowth(
    val category: String,
    val currentExpense: Long,
    val previousExpense: Long,
    val change: Long,
    val percent: Double?,
)
@Composable
private fun CategoryGrowthCard(
    currentCategories: List<ReportCalculator.CategorySummary>,
    previousCategories: List<ReportCalculator.CategorySummary>,
) {

    val previousByCategory =
        previousCategories.associateBy { it.category }

    val currentByCategory =
        currentCategories.associateBy { it.category }

    val allCategories =
        (currentByCategory.keys + previousByCategory.keys)
            .distinct()

    val growthItems =
        allCategories
            .map { category ->
                val currentExpense =
                    currentByCategory[category]?.expense ?: 0L

                val previousExpense =
                    previousByCategory[category]?.expense ?: 0L

                CategoryGrowth(
                    category = category,
                    currentExpense = currentExpense,
                    previousExpense = previousExpense,
                    change = currentExpense - previousExpense,
                    percent = percentageChange(
                        previousExpense,
                        currentExpense,
                    ),
                )
            }
            .filter {
                it.currentExpense > 0L || it.previousExpense > 0L
            }
            .sortedByDescending {
                it.change
            }
            .take(5)

    val biggestIncrease =
        growthItems
            .filter { it.change > 0L }
            .maxByOrNull { it.change }

    val biggestDecrease =
        growthItems
            .filter { it.change < 0L }
            .minByOrNull { it.change }

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "رشد هزینه‌ها",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = "مقایسه دسته‌های هزینه با ماه قبل",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (growthItems.isEmpty()) {
                Text(
                    text = "برای مقایسه دسته‌ها داده کافی وجود ندارد.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {

                biggestIncrease?.let { item ->
                    AppCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = "بیشترین افزایش",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )

                            Text(
                                text = item.category,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                            )

                            Text(
                                text = "+${formatMoney(item.change)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                            )

                            Text(
                                text = item.percent?.let {
                                    "رشد ${formatPercent(it)}"
                                } ?: "هزینه جدید",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }

                growthItems.forEach { item ->
                    CategoryGrowthRow(item)
                }

                biggestDecrease?.let { item ->
                    Text(
                        text = "بهترین کاهش هزینه: ${item.category} با ${formatMoney(kotlin.math.abs(item.change))}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryGrowthRow(
    item: CategoryGrowth,
) {
    val category = item.category
    val currentExpense = item.currentExpense
    val previousExpense = item.previousExpense
    val change = item.change
    val percent = item.percent

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = category,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = formatMoney(currentExpense),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "ماه قبل: ${formatMoney(previousExpense)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = when {
                    change > 0L ->
                        "▲ ${percent?.let { formatPercent(it) } ?: "جدید"}"

                    change < 0L ->
                        "▼ ${formatPercent(kotlin.math.abs(percent ?: 0.0))}"

                    else ->
                        "بدون تغییر"
                },
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
@Composable
private fun CategoryRow(
    item: ReportCalculator.CategorySummary,
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = item.category,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )

            if (item.income > 0L) {
                Text(
                    text = "درآمد: ${formatMoney(item.income)}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            if (item.expense > 0L) {
                Text(
                    text = "هزینه: ${formatMoney(item.expense)}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Text(
                text = "تعداد: ${item.transactionCount.toString().toFaDigits()}",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun AccountRow(
    item: ReportCalculator.AccountSummary,
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = item.account.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )

            SummaryLine("موجودی فعلی", formatMoney(item.balance))

            if (item.income > 0L) {
                SummaryLine("درآمد دوره", formatMoney(item.income))
            }

            if (item.expense > 0L) {
                SummaryLine("هزینه دوره", formatMoney(item.expense))
            }

            if (item.transferIn > 0L) {
                SummaryLine("انتقال ورودی", formatMoney(item.transferIn))
            }

            if (item.transferOut > 0L) {
                SummaryLine("انتقال خروجی", formatMoney(item.transferOut))
            }
        }
    }
}

@Composable
private fun SummaryLine(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun EmptyCard(message: String) {
    AppEmptyState(
        modifier = Modifier.fillMaxWidth(),
        title = message,
    )
}

private fun calculateRange(
    period: ReportPeriod,
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
        ReportPeriod.TODAY -> {
            startOfDay(from)
            endOfDay(to)
        }

        ReportPeriod.THIS_WEEK -> {
            startOfDay(from)

            val dayOfWeek = from.get(Calendar.DAY_OF_WEEK)
            val firstDay = from.firstDayOfWeek
            val daysFromFirst = (dayOfWeek - firstDay + 7) % 7

            from.add(Calendar.DAY_OF_MONTH, -daysFromFirst)
            startOfDay(from)
            endOfDay(to)
        }

        ReportPeriod.THIS_MONTH -> {
            from.set(Calendar.DAY_OF_MONTH, 1)
            startOfDay(from)
            endOfDay(to)
        }

        ReportPeriod.LAST_MONTH -> {
            from.set(Calendar.DAY_OF_MONTH, 1)
            from.add(Calendar.MONTH, -1)
            startOfDay(from)

            to.set(Calendar.DAY_OF_MONTH, 1)
            to.add(Calendar.MONTH, -1)
            to.set(
                Calendar.DAY_OF_MONTH,
                to.getActualMaximum(Calendar.DAY_OF_MONTH)
            )
            endOfDay(to)
        }
    }

    return from.timeInMillis to to.timeInMillis
}

private fun formatMoney(
    value: Long,
): String {
    return value.toPersianCurrency()
}

private fun String.toFaDigits(): String {
    return this
        .replace('0', '۰')
        .replace('1', '۱')
        .replace('2', '۲')
        .replace('3', '۳')
        .replace('4', '۴')
        .replace('5', '۵')
        .replace('6', '۶')
        .replace('7', '۷')
        .replace('8', '۸')
        .replace('9', '۹')
}


























































