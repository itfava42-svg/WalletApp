package com.example.walletapp.ui.screens

import com.example.walletapp.ui.components.JalaliDatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                    .padding(start = 20.dp, end = 12.dp, top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (existingReminder == null) "افزودن یادآوری" else "ویرایش یادآوری",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = "اطلاعات یادآوری را وارد کنید",
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
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Spacer(modifier = Modifier.height(2.dp))

            /*
             * Preview card
             */
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(RoundedCornerShape(18.dp))
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
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text =
                                title.ifBlank {
                                    "عنوان یادآوری"
                                },
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )

                        Spacer(modifier = Modifier.height(5.dp))

                        Text(
                            text =
                                if (kind == ReminderKind.PAY)
                                    "یادآوری پرداخت"
                                else
                                    "یادآوری دریافت",
                            fontSize = 12.sp,
                            color = selectedKindColor
                        )
                    }

                    if (amountText.isNotBlank()) {
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = amountText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextDark
                            )

                            Text(
                                text = "مبلغ",
                                fontSize = 11.sp,
                                color = TextGray
                            )
                        }
                    }
                }
            }

            SectionTitle(
                icon = Icons.Default.NotificationsActive,
                title = "نوع یادآوری"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
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

            SectionTitle(
                icon = Icons.Default.Description,
                title = "اطلاعات یادآوری"
            )

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    errorMessage = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                label = {
                    Text("عنوان یادآوری")
                },
                placeholder = {
                    Text("مثلاً پرداخت قبض برق")
                },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    amountText = it.filter { ch ->
                        ch.isDigit() ||
                            ch == ',' ||
                            ch == '،' ||
                            ch == '٬'
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                label = {
                    Text("مبلغ")
                },
                placeholder = {
                    Text("مثلاً ۵,۰۰۰,۰۰۰")
                },
                suffix = {
                    Text("ریال")
                },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            OutlinedTextField(
                value = description,
                onValueChange = {
                    description = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 104.dp),
                label = {
                    Text("توضیحات")
                },
                placeholder = {
                    Text("توضیحات تکمیلی اختیاری")
                },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(18.dp),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            SectionTitle(
                icon = Icons.Default.CalendarMonth,
                title = "زمان یادآوری"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
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

            SectionTitle(
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Red.copy(alpha = 0.10f)
                    )
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(14.dp),
                        color = Red,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                     .height(52.dp),
                onClick = {
                    save()
                },
                shape = MaterialTheme.shapes.large
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text =
                        if (existingReminder == null)
                            "ذخیره یادآوری"
                        else
                            "ذخیره تغییرات",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
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
private fun SectionTitle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(
            top = 4.dp,
            bottom = 2.dp
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Purple,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
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
    Card(
        modifier = modifier
            .height(112.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                if (selected)
                    color.copy(alpha = 0.12f)
                else
                    MaterialTheme.colorScheme.surface
        ),
        border =
            if (selected)
                androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    color
                )
            else
                null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(color.copy(alpha = 0.13f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (selected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                Text(
                    text = subtitle,
                    color = TextGray,
                    fontSize = 10.sp
                )
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
    Card(
        modifier = modifier
            .height(92.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = TextGray
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = value,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
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
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(17.dp),
        color =
            if (selected)
                color.copy(alpha = 0.10f)
            else
                MaterialTheme.colorScheme.surface,
        border =
            if (selected)
                androidx.compose.foundation.BorderStroke(
                    1.2.dp,
                    color
                )
            else
                androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Color(0xFFE8E8EE)
                )
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 15.dp,
                vertical = 12.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (selected)
                            color
                        else
                            Color(0xFFD2D2DA)
                    )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Text(
                    text = subtitle,
                    color = TextGray,
                    fontSize = 10.sp
                )
            }

            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(21.dp)
                )
            }
        }
    }
}










