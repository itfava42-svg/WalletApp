package com.example.walletapp.ui.screens

import com.example.walletapp.reminders.ReminderManager
import com.example.walletapp.data.AccountStore
import com.example.walletapp.data.LoanStore
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.walletapp.data.Reminder
import com.example.walletapp.loan.MatureColors
import com.example.walletapp.data.ReminderKind
import com.example.walletapp.data.ReminderStore
import com.example.walletapp.data.JalaliCalendar
import java.util.Calendar
import kotlin.math.abs

private enum class ReminderFilter(
    val title: String
) {
    ALL("همه"),
    TODAY("امروز"),
    NEXT_THREE_DAYS("۳ روز آینده"),
    THIS_WEEK("این هفته"),
    OVERDUE("عقب‌افتاده"),
    COMPLETED("انجام‌شده")
}

@OptIn(ExperimentalMaterial3Api::class)
@androidx.compose.runtime.Composable
fun ReminderScreen(
    onBack: () -> Unit,
    onAddReminder: () -> Unit,
    onEditReminder: (Reminder) -> Unit
) {

    val context =
        androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        ReminderStore.attachContext(context)
    }

    var refreshKey by remember {
        mutableStateOf(0)
    }

    var selectedFilter by remember {
        mutableStateOf(
            ReminderFilter.ALL
        )
    }

    val reminders =
        remember(
            refreshKey,
            ReminderStore.reminders.size
        ) {
            ReminderStore.reminders.toList()
        }

    val now =
        System.currentTimeMillis()

    val todayStart =
        startOfDay(now)

    val tomorrowStart =
        todayStart +
            DAY_MILLIS

    val threeDaysEnd =
        todayStart +
            (4L * DAY_MILLIS)

    val weekEnd =
        endOfWeek(now)

    val activeReminders =
        reminders.filter {
            it.isActive &&
                !it.isCompleted
        }

    val overdueCount =
        activeReminders.count {
            isOverdue(it, now)
        }

    val todayCount =
        activeReminders.count {
            isToday(it, todayStart)
        }

    val nextThreeDaysCount =
        activeReminders.count {
            it.triggerAtMillis >= todayStart &&
                it.triggerAtMillis < threeDaysEnd
        }

    val dueAmount =
        activeReminders
            .filter {
                it.triggerAtMillis < threeDaysEnd
            }
            .sumOf {
                it.amount
            }

    val filtered =
        when (selectedFilter) {

            ReminderFilter.ALL ->
                reminders

            ReminderFilter.TODAY ->
                reminders.filter {
                    isToday(
                        it,
                        todayStart
                    ) &&
                        !it.isCompleted
                }

            ReminderFilter.NEXT_THREE_DAYS ->
                reminders.filter {
                    it.isActive &&
                        !it.isCompleted &&
                        it.triggerAtMillis >= todayStart &&
                        it.triggerAtMillis < threeDaysEnd
                }

            ReminderFilter.THIS_WEEK ->
                reminders.filter {
                    it.isActive &&
                        !it.isCompleted &&
                        it.triggerAtMillis >= todayStart &&
                        it.triggerAtMillis < weekEnd
                }

            ReminderFilter.OVERDUE ->
                reminders.filter {
                    it.isActive &&
                        !it.isCompleted &&
                        isOverdue(
                            it,
                            now
                        )
                }

            ReminderFilter.COMPLETED ->
                reminders.filter {
                    it.isCompleted
                }
        }
            .sortedWith(
                compareBy<Reminder> {
                    it.isCompleted
                }.thenBy {
                    it.triggerAtMillis
                }
            )

    val nextReminder =
        activeReminders
            .filter {
                !isOverdue(
                    it,
                    now
                )
            }
            .minByOrNull {
                it.triggerAtMillis
            }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,

        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(start = 20.dp, end = 12.dp, top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "یادآوری‌ها",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = "مدیریت اقساط، پرداخت‌ها و رویدادهای مهم",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "بازگشت",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },

        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddReminder,
                shape = RoundedCornerShape(20.dp),
                containerColor = MatureColors.Primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "یادآوری جدید"
                )
            }
        }

    ) { paddingValues ->

        LazyColumn(

            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        paddingValues
                    ),

            contentPadding =
                androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 18.dp,
                    vertical = 18.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    12.dp
                )
        ) {

            item {

                ReminderHeader(
                    todayCount = todayCount,
                    overdueCount = overdueCount,
                    nextThreeDaysCount =
                        nextThreeDaysCount,
                    dueAmount = dueAmount
                )
            }

            item {

                nextReminder?.let {

                    NextReminderCard(
                        reminder = it,
                        now = now
                    )
                }
            }

            item {

                Text(
                    text = "دسته‌بندی یادآوری‌ها",
                    style =
                        MaterialTheme.typography.titleMedium,
                    fontWeight =
                        FontWeight.Bold,
                    modifier =
                        Modifier.padding(
                            top = 4.dp,
                            bottom = 2.dp
                        )
                )
            }

            item {

                LazyFilterRow(
                    selectedFilter =
                        selectedFilter,
                    onSelected = {
                        selectedFilter = it
                    }
                )
            }

            if (filtered.isEmpty()) {

                item {

                    EmptyReminderCard(
                        filter =
                            selectedFilter,
                        onAddReminder =
                            onAddReminder
                    )
                }

            } else {

                items(
                    items = filtered,
                    key = {
                        it.id
                    }
                ) { reminder ->

                    ReminderItemCard(

                        reminder = reminder,

                        now = now,

                        onComplete = {

                            ReminderManager.setCompleted(
                                context,
                                reminder.id,
                                !reminder.isCompleted
                            )

                            refreshKey++
                        },

                        onEdit = {
                            onEditReminder(
                                reminder
                            )
                        },

                        onDelete = {

                            ReminderManager.remove(
                                context,
                                reminder.id
                            )

                            refreshKey++
                        }
                    )
                }
            }

            item {
                Spacer(
                    modifier =
                        Modifier.height(
                            80.dp
                        )
                )
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun ReminderHeader(
    todayCount: Int,
    overdueCount: Int,
    nextThreeDaysCount: Int,
    dueAmount: Long
) {

    Column {

        Card(
            modifier =
                Modifier.fillMaxWidth(),
            shape =
                RoundedCornerShape(
                    22.dp
                ),
            elevation =
                CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
        ) {

            Column(
                modifier =
                    Modifier.padding(
                        18.dp
                    )
            ) {

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Box(
                        modifier =
                            Modifier
                                .size(48.dp)
                                .clip(
                                    CircleShape
                                )
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer
                                ),
                        contentAlignment =
                            Alignment.Center
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.Alarm,
                            contentDescription =
                                null,
                            tint =
                                MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.size(
                                12.dp
                            )
                    )

                    Column(
                        modifier =
                            Modifier.weight(
                                1f
                            )
                    ) {

                        Text(
                            text =
                                "مرکز یادآوری مالی",
                            style =
                                MaterialTheme.typography.titleLarge,
                            fontWeight =
                                FontWeight.Bold
                        )

                        Text(
                            text =
                                "اقساط، پرداخت‌ها و رویدادهای مهم",
                            style =
                                MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(
                            18.dp
                        )
                )

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            8.dp
                        )
                ) {

                    SummaryBox(
                        modifier =
                            Modifier.weight(1f),
                        title =
                            "امروز",
                        value =
                            todayCount.toString(),
                        icon =
                            Icons.Default.CalendarToday
                    )

                    SummaryBox(
                        modifier =
                            Modifier.weight(1f),
                        title =
                            "۳ روز آینده",
                        value =
                            nextThreeDaysCount.toString(),
                        icon =
                            Icons.Default.Event
                    )

                    SummaryBox(
                        modifier =
                            Modifier.weight(1f),
                        title =
                            "عقب‌افتاده",
                        value =
                            overdueCount.toString(),
                        icon =
                            Icons.Default.Warning
                    )
                }

                if (dueAmount > 0) {

                    Spacer(
                        modifier =
                            Modifier.height(
                                12.dp
                            )
                    )

                    Surface(
                        shape =
                            RoundedCornerShape(
                                14.dp
                            ),
                        tonalElevation =
                            2.dp,
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Row(
                            modifier =
                                Modifier.padding(
                                    12.dp
                                ),
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Default.Schedule,
                                contentDescription =
                                    null,
                                modifier =
                                    Modifier.size(
                                        20.dp
                                    )
                            )

                            Spacer(
                                modifier =
                                    Modifier.size(
                                        8.dp
                                    )
                            )

                            Column {

                                Text(
                                    text =
                                        "مبلغ قابل پرداخت تا ۳ روز آینده",
                                    style =
                                        MaterialTheme.typography.labelMedium
                                )

                                Text(
                                    text =
                                        formatMoney(
                                            dueAmount
                                        ),
                                    fontWeight =
                                        FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun SummaryBox(
    modifier: Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {

    Surface(
        modifier = modifier,
        shape =
            RoundedCornerShape(
                14.dp
            ),
        tonalElevation = 1.dp
    ) {

        Column(
            modifier =
                Modifier.padding(
                    10.dp
                ),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Icon(
                imageVector =
                    icon,
                contentDescription =
                    null,
                modifier =
                    Modifier.size(
                        20.dp
                    )
            )

            Spacer(
                modifier =
                    Modifier.height(
                        4.dp
                    )
            )

            Text(
                text = value,
                fontWeight =
                    FontWeight.Bold
            )

            Text(
                text = title,
                style =
                    MaterialTheme.typography.labelSmall
            )
        }
    }
}

@androidx.compose.runtime.Composable
private fun NextReminderCard(
    reminder: Reminder,
    now: Long
) {

    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.primaryContainer
            )
    ) {

        Column(
            modifier =
                Modifier.padding(
                    16.dp
                )
        ) {

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Icon(
                    imageVector =
                        if (
                            reminder.isAutoGenerated
                        ) {
                            Icons.Default.Event
                        } else {
                            Icons.Default.Notifications
                        },
                    contentDescription =
                        null
                )

                Spacer(
                    modifier =
                        Modifier.size(
                            8.dp
                        )
                )

                Text(
                    text =
                        if (
                            reminder.isAutoGenerated
                        ) {
                            "نزدیک‌ترین قسط"
                        } else {
                            "نزدیک‌ترین یادآوری"
                        },
                    style =
                        MaterialTheme.typography.titleMedium,
                    fontWeight =
                        FontWeight.Bold
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        10.dp
                    )
            )

            Text(
                text =
                    reminder.title,
                style =
                    MaterialTheme.typography.titleLarge,
                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(
                        4.dp
                    )
            )

            Text(
                text =
                    dueText(
                        reminder,
                        now
                    ),
                style =
                    MaterialTheme.typography.bodyLarge,
                fontWeight =
                    FontWeight.SemiBold
            )

            if (reminder.amount > 0) {

                Spacer(
                    modifier =
                        Modifier.height(
                            4.dp
                        )
                )

                Text(
                    text =
                        formatMoney(
                            reminder.amount
                        )
                )
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun LazyFilterRow(
    selectedFilter: ReminderFilter,
    onSelected: (ReminderFilter) -> Unit
) {

    androidx.compose.foundation.lazy.LazyRow(
        horizontalArrangement =
            Arrangement.spacedBy(
                8.dp
            )
    ) {

        items(
            ReminderFilter.entries
        ) { filter ->

            FilterChip(
                selected =
                    selectedFilter == filter,

                onClick = {
                    onSelected(
                        filter
                    )
                },

                label = {
                    Text(
                        text =
                            filter.title
                    )
                }
            )
        }
    }
}

@androidx.compose.runtime.Composable
private fun ReminderItemCard(
    reminder: Reminder,
    now: Long,
    onComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {

    val overdue =
        isOverdue(
            reminder,
            now
        )

    val linkedLoan =
        if (
            reminder.isAutoGenerated &&
                reminder.sourceType == "LOAN_INSTALLMENT"
        ) {
            reminder.sourceId?.let { LoanStore.getById(it) }
        } else {
            null
        }

    val linkedAccount =
        linkedLoan?.accountId?.let { accountId ->
            AccountStore.accounts.firstOrNull { it.id == accountId }
        }

    val bankLabel =
        linkedAccount?.let {
            if (it.bankName.isBlank()) it.name else it.bankName
        }

    val statusText =
        if (reminder.isCompleted) {
            "انجام‌شده"
        } else {
            dueText(
                reminder,
                now
            )
        }

    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {

        Column(
            modifier =
                Modifier.padding(
                    14.dp
                )
        ) {

            Row(
                verticalAlignment =
                    Alignment.Top
            ) {

                Box(
                    modifier =
                        Modifier
                            .size(42.dp)
                            .clip(
                                CircleShape
                            )
                            .background(
                                if (
                                    reminder.isAutoGenerated
                                ) {
                                    MaterialTheme.colorScheme.secondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(
                        imageVector =
                            if (
                                reminder.isAutoGenerated
                            ) {
                                Icons.Default.Event
                            } else {
                                Icons.Default.Notifications
                            },
                        contentDescription =
                            null
                    )
                }

                Spacer(
                    modifier =
                        Modifier.size(
                            10.dp
                        )
                )

                Column(
                    modifier =
                        Modifier.weight(
                            1f
                        )
                ) {

                    Column(
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Text(
                            text = reminder.title,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (reminder.isAutoGenerated) {
                            Spacer(modifier = Modifier.height(7.dp))
                            AssistChip(
                                onClick = {},
                                label = { Text("قسط وام") }
                            )
                        }
                    }

                    if (bankLabel != null) {
                        Spacer(modifier = Modifier.height(7.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            tonalElevation = 1.dp
                        ) {
                            Text(
                                text = "بانک: ${bankLabel}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(
                                    horizontal = 10.dp,
                                    vertical = 6.dp
                                )
                            )
                        }
                    }

                    if (linkedLoan != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "وام: ${linkedLoan.title}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        reminder.sourceInstallmentNumber?.let { installmentNumber ->
                            Text(
                                text = "قسط ${installmentNumber} از ${linkedLoan.totalInstallments}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(
                        modifier =
                            Modifier.height(
                                8.dp
                            )
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        tonalElevation = 1.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(
                                horizontal = 12.dp,
                                vertical = 10.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "وضعیت: " + statusText,
                                color =
                                    if (
                                        overdue &&
                                        !reminder.isCompleted
                                    ) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = "سررسید: " + formatReminderDate(reminder.triggerAtMillis),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (reminder.amount > 0) {
                                Text(
                                    text = "مبلغ قسط: " + formatMoney(reminder.amount),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (
                        reminder.description.isNotBlank()
                    ) {

                        Spacer(
                            modifier =
                                Modifier.height(
                                    3.dp
                                )
                        )

                        Text(
                            text =
                                reminder.description,
                            style =
                                MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow =
                                TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(
                modifier =
                    Modifier.height(
                        10.dp
                    )
            )

            HorizontalDivider()

            Spacer(
                modifier =
                    Modifier.height(
                        4.dp
                    )
            )

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                IconButton(
                    onClick =
                        onComplete
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.CheckCircle,
                        contentDescription =
                            if (
                                reminder.isCompleted
                            ) {
                                "بازگرداندن"
                            } else {
                                "انجام شد"
                            }
                    )
                }

                IconButton(
                    onClick =
                        onEdit
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.Edit,
                        contentDescription =
                            "ویرایش"
                    )
                }

                IconButton(
                    onClick =
                        onDelete
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.Delete,
                        contentDescription =
                            "حذف"
                    )
                }

                Spacer(
                    modifier =
                        Modifier.weight(
                            1f
                        )
                )

                Text(
                    text =
                        JalaliCalendar.fromEpochMillisSlashed(
                            reminder.triggerAtMillis
                        ),
                    style =
                        MaterialTheme.typography.labelMedium
                )
            }
        }
    }


private fun formatReminderDate(triggerAtMillis: Long): String {
    val calendar = Calendar.getInstance().apply { timeInMillis = triggerAtMillis }
    val date = JalaliCalendar.fromGregorian(
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    return persianDigits(
        date.year.toString() + "/" +
            date.month.toString().padStart(2, '0') + "/" +
            date.day.toString().padStart(2, '0')
    )
}
}

@androidx.compose.runtime.Composable
private fun EmptyReminderCard(
    filter: ReminderFilter,
    onAddReminder: () -> Unit
) {

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    onClick =
                        onAddReminder
                ),
        shape =
            RoundedCornerShape(
                20.dp
            )
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        28.dp
                    ),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Icon(
                imageVector =
                    Icons.Default.Notifications,
                contentDescription =
                    null,
                modifier =
                    Modifier.size(
                        42.dp
                    )
            )

            Spacer(
                modifier =
                    Modifier.height(
                        10.dp
                    )
            )

            Text(
                text =
                    when (filter) {
                        ReminderFilter.ALL ->
                            "هنوز یادآوری‌ای ثبت نشده است"