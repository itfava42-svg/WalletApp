package com.example.walletapp.ui.screens

import com.example.walletapp.ui.components.AppIconButton

import com.example.walletapp.ui.components.JalaliDatePickerDialog
import com.example.walletapp.ui.components.AppCard
import com.example.walletapp.ui.components.AppPrimaryButton
import com.example.walletapp.ui.components.AppSectionHeader
import com.example.walletapp.ui.components.AppTextField
import com.example.walletapp.ui.theme.AppDimens
import com.example.walletapp.ui.theme.AppTextStyles
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.walletapp.data.Reminder
import com.example.walletapp.data.ReminderKind
import com.example.walletapp.data.RepeatFrequency
import com.example.walletapp.reminders.ReminderManager
import com.example.walletapp.data.JalaliCalendar
import com.example.walletapp.loan.MatureColors
import java.util.Calendar
import java.util.UUID

private val Purple = MatureColors.Primary
private val Blue = MatureColors.PrimaryLight
private val Green = MatureColors.Success
private val Orange = MatureColors.Secondary
private val Red = MatureColors.PrimaryDark
private val Background = MatureColors.Surface
private val TextDark = MatureColors.TextPrimary
private val TextGray = MatureColors.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderScreen(
    existingReminder: Reminder? = null,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    var title by remember {
        mutableStateOf(existingReminder?.title ?: "")
    }

    var amountText by remember {
        mutableStateOf(
            existingReminder?.amount
                ?.takeIf { it > 0 }
                ?.toString()
                ?: ""
        )
    }

    var description by remember {
        mutableStateOf(existingReminder?.description ?: "")
    }

    var kind by remember {
        mutableStateOf(existingReminder?.kind ?: ReminderKind.PAY)
    }

    var repeat by remember {
        mutableStateOf(
            existingReminder?.repeat ?: RepeatFrequency.ONCE
        )
    }

    val initialCalendar = Calendar.getInstance().apply {
        if (existingReminder != null) {
            timeInMillis = existingReminder.triggerAtMillis
        }
    }

    var selectedDateMillis by remember {
        mutableStateOf(initialCalendar.timeInMillis)
    }

    var showJalaliDatePicker by remember {
        mutableStateOf(false)
    }

    var selectedHour by remember {
        mutableIntStateOf(initialCalendar.get(Calendar.HOUR_OF_DAY))
    }

    var selectedMinute by remember {
        mutableIntStateOf(initialCalendar.get(Calendar.MINUTE))
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    fun chooseDate() {
        showJalaliDatePicker = true
    }

    fun chooseTime() {
        TimePickerDialog(
            context,
            { _, hour, minute ->
                selectedHour = hour
                selectedMinute = minute
            },
            selectedHour,
            selectedMinute,
            true
        ).show()
    }

    fun save() {
        val cleanTitle = title.trim()

        if (cleanTitle.isBlank()) {
            errorMessage = "عنوان یادآوری را وارد کنید."
            return
        }

        val amount = amountText
            .replace(",", "")
            .replace("٬", "")
            .replace("،", "")
            .trim()
            .toLongOrNull() ?: 0L

        val calendar = Calendar.getInstance().apply {
            timeInMillis = selectedDateMillis
            set(Calendar.HOUR_OF_DAY, selectedHour)
            set(Calendar.MINUTE, selectedMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (
            calendar.timeInMillis <= System.currentTimeMillis() &&
            repeat == RepeatFrequency.ONCE
        ) {
            errorMessage =
                "برای یادآوری یک‌بار، تاریخ و زمان باید در آینده باشد."
            return
        }

        val reminder = Reminder(
            id = existingReminder?.id ?: UUID.randomUUID().toString(),
            title = cleanTitle,
            amount = amount,
            kind = kind,
            description = description.trim(),
            repeat = repeat,
            triggerAtMillis = calendar.timeInMillis,
            isActive = true
        )

        ReminderManager.save(
            context = context,
            reminder = reminder,
            existingReminder = existingReminder,
        )

        onSaved()
    }

    val jalaliDate = remember(selectedDateMillis) {
        JalaliCalendar.fromEpochMillisSlashed(selectedDateMillis)
    }

    val selectedKindColor =
        if (kind == ReminderKind.PAY) Red else Green

    Scaffold(
        containerColor = Background,

        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(start = AppDimens.PagePadding, end = AppDimens.ItemGap, top = AppDimens.ItemGap, bottom = AppDimens.TightGap),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (existingReminder == null) "افزودن یادآوری" else "ویرایش یادآوری",
                        style = AppTextStyles.headlineSm,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(AppDimens.TightGap))
                    Text(
                        text = "اطلاعات یادآوری را وارد کنید",
                        style = AppTextStyles.bodyMd,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                AppIconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "بازگشت",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppDimens.PagePadding),
            verticalArrangement = Arrangement.spacedBy(AppDimens.CardGap)
        ) {

            Spacer(modifier = Modifier.height(AppDimens.TightGap))

            /*
             * Preview card
             */
            AppCard(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppDimens.CardPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(AppDimens.FeatureIconBox)
                            .clip(RoundedCornerShape(AppDimens.SmallCardRadius))
                            .background(
                                selectedKindColor.copy(alpha = 0.12f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector =
                                if (kind == ReminderKind.PAY)
                                    Icons.Default.Payment
                                else
                                    Icons.Default.CallReceived,
                            contentDescription = null,
                            tint = selectedKindColor,
                            modifier = Modifier.size(AppDimens.FeatureIcon)
                        )
                    }

                    Spacer(modifier = Modifier.width(AppDimens.CardGap))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text =
                                title.ifBlank {
                                    "عنوان یادآوری"
                                },
                            style = AppTextStyles.titleMd,
                            color = TextDark
                        )

                        Spacer(modifier = Modifier.height(5.dp))

                        Text(
                            text =
                                if (kind == ReminderKind.PAY)
                                    "یادآوری پرداخت"
                                else
                                    "یادآوری دریافت",
                            style = AppTextStyles.labelMd,
                            color = selectedKindColor
                        )
                    }

                    if (amountText.isNotBlank()) {
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = amountText,
                                style = AppTextStyles.titleMd,
                                color = TextDark
                            )

                            Text(
                                text = "مبلغ",
                                style = AppTextStyles.labelSmall,
                                color = TextGray
                            )
                        }
                    }
                }
            }

            AppSectionHeader(
                icon = Icons.Default.NotificationsActive,
                title = "نوع یادآوری"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppDimens.CardGap)
            ) {

                ReminderTypeTile(
                    modifier = Modifier.weight(1f),
                    title = "پرداخت",
                    subtitle = "باید پرداخت کنم",
                    icon = Icons.Default.Payment,
                    selected = kind == ReminderKind.PAY,
                    color = Red
                ) {
                    kind = ReminderKind.PAY
                }

                ReminderTypeTile(
                    modifier = Modifier.weight(1f),
                    title = "دریافت",
                    subtitle = "باید دریافت کنم",
                    icon = Icons.Default.CallReceived,
                    selected = kind == ReminderKind.RECEIVE,
                    color = Green
                ) {
                    kind = ReminderKind.RECEIVE
                }
            }

            AppSectionHeader(
                icon = Icons.Default.Description,
                title = "اطلاعات یادآوری"
            )

            AppTextField(
                value = title,
                onValueChange = {
                    title = it
                    errorMessage = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = "عنوان یادآوری",
                placeholder = {
                    Text("مثلاً پرداخت قبض برق")
                },
                singleLine = true,
            )

            AppTextField(
                value = amountText,
                onValueChange = {
                    amountText = it.filter { ch ->
                        ch.isDigit() ||
                            ch == ',' ||
                            ch == '،' ||
                            ch == '٬'
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = "مبلغ",
                placeholder = {
                    Text("مثلاً ۵,۰۰۰,۰۰۰")
                },
                suffix = {
                    Text("ریال")
                },
                singleLine = true,
            )

            AppTextField(
                value = description,
                onValueChange = {
                    description = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 104.dp),
                label = "توضیحات",
                placeholder = {
                    Text("توضیحات تکمیلی اختیاری")
                },
                singleLine = false,
                minLines = 3,
                maxLines = 5,
            )

            AppSectionHeader(
                icon = Icons.Default.CalendarMonth,
                title = "زمان یادآوری"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppDimens.CardGap)
            ) {

                InfoTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.CalendarMonth,
                    title = "تاریخ",
                    value = jalaliDate,
                    color = Blue
                ) {
                    chooseDate()
                }

                InfoTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Schedule,
                    title = "ساعت",
                    value = "%02d:%02d".format(
                        selectedHour,
                        selectedMinute
                    ),
                    color = Orange
                ) {
                    chooseTime()
                }
            }

            AppSectionHeader(
                icon = Icons.Default.Repeat,
                title = "تکرار یادآوری"
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                RepeatTile(
                    title = "یک‌بار",
                    subtitle = "فقط در تاریخ انتخاب‌شده",
                    selected = repeat == RepeatFrequency.ONCE,
                    color = Purple
                ) {
                    repeat = RepeatFrequency.ONCE
                }

                RepeatTile(
                    title = "روزانه",
                    subtitle = "هر روز در ساعت مشخص",
                    selected = repeat == RepeatFrequency.DAILY,
                    color = Blue
                ) {
                    repeat = RepeatFrequency.DAILY
                }

                RepeatTile(
                    title = "هفتگی",
                    subtitle = "هر هفته در همین زمان",
                    selected = repeat == RepeatFrequency.WEEKLY,
                    color = Green
                ) {
                    repeat = RepeatFrequency.WEEKLY
                }

                RepeatTile(
                    title = "ماهانه",
                    subtitle = "هر ماه در همین زمان",
                    selected = repeat == RepeatFrequency.MONTHLY,
                    color = Orange
                ) {
                    repeat = RepeatFrequency.MONTHLY
                }
            }

            errorMessage?.let { message ->
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = Red.copy(alpha = 0.10f),
                ) {
                    Text(
                        text = message,
                        color = Red,
                        style = AppTextStyles.bodyMd,
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppDimens.TightGap))

            AppPrimaryButton(
                text =
                    if (existingReminder == null)
                        "ذخیره یادآوری"
                    else
                        "ذخیره تغییرات",
                onClick = { save() },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                    )
                },
            )

            Spacer(modifier = Modifier.height(AppDimens.SectionGap))
        }
    }

    if (showJalaliDatePicker) {
        JalaliDatePickerDialog(
            initialDate = JalaliCalendar.fromEpochMillisSlashed(selectedDateMillis),
            onDismissRequest = {
                showJalaliDatePicker = false
            },
            onDateSelected = { selectedDate ->
                val asciiDate = selectedDate.map { ch ->
                    when {
                        ch.code in 0x06F0..0x06F9 ->
                            ('0'.code + (ch.code - 0x06F0)).toChar()
                        ch.code in 0x0660..0x0669 ->
                            ('0'.code + (ch.code - 0x0660)).toChar()
                        else -> ch
                    }
                }.joinToString("")

                val parts = asciiDate.split("/")
                val year = parts.getOrNull(0)?.toIntOrNull()
                val month = parts.getOrNull(1)?.toIntOrNull()
                val day = parts.getOrNull(2)?.toIntOrNull()

                if (year != null && month != null && day != null) {
                    selectedDateMillis =
                        JalaliCalendar.jalaliToEpochMillis(year, month, day)
                }

                showJalaliDatePicker = false
            }
        )
    }
}

@Composable
private fun ReminderTypeTile(
    modifier: Modifier,
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    AppCard(
        modifier = modifier.height(112.dp),
        containerColor = if (selected) color.copy(alpha = 0.12f)
        else MaterialTheme.colorScheme.surface,
        border = if (selected) androidx.compose.foundation.BorderStroke(1.5.dp, color) else null,
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(AppDimens.IconBox)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(AppDimens.TileRadius))
                        .background(color.copy(alpha = 0.13f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = color)
                }

                Spacer(modifier = Modifier.width(AppDimens.ItemGap))

                if (selected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(AppDimens.IconSmall)
                    )
                }
            }

            Column {
                Text(text = title, style = AppTextStyles.titleMd)
                Text(text = subtitle, color = TextGray, style = AppTextStyles.labelSmall)
            }
        }
    }
}

@Composable
private fun InfoTile(
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    color: Color,
    onClick: () -> Unit
) {
    AppCard(
        modifier = modifier.height(92.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(AppDimens.IconBox)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(AppDimens.TileRadius))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color)
            }

            Spacer(modifier = Modifier.width(AppDimens.CardGap))

            Column {
                Text(text = title, style = AppTextStyles.labelSmall, color = TextGray)
                Spacer(modifier = Modifier.height(AppDimens.TightGap))
                Text(
                    text = value,
                    style = AppTextStyles.titleSm,
                    color = TextDark
                )
            }
        }
    }
}

@Composable
private fun RepeatTile(
    title: String,
    subtitle: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = if (selected) color.copy(alpha = 0.10f)
        else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            if (selected) 1.2.dp else 1.dp,
            if (selected) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
        ),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(50))
                    .background(
                        if (selected) color
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                    )
            )

            Spacer(modifier = Modifier.width(AppDimens.CardGap))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = AppTextStyles.titleSm)
                Text(text = subtitle, color = TextGray, style = AppTextStyles.labelSmall)
            }

            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(AppDimens.IconSmall)
                )
            }
        }
    }
}
