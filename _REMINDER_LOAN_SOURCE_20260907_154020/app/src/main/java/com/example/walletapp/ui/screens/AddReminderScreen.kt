package com.example.walletapp.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.walletapp.data.Reminder
import com.example.walletapp.data.ReminderKind
import com.example.walletapp.data.ReminderStore
import com.example.walletapp.data.RepeatFrequency
import com.example.walletapp.reminders.ReminderScheduler
import java.util.Calendar
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderScreen(
    existingReminder: Reminder? = null,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val context = LocalContext.current

    var title by remember {
        mutableStateOf(existingReminder?.title ?: "")
    }

    var amountText by remember {
        mutableStateOf(
            existingReminder?.amount?.takeIf { it > 0 }?.toString() ?: ""
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
        val calendar = Calendar.getInstance().apply {
            timeInMillis = selectedDateMillis
        }

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCalendar = Calendar.getInstance().apply {
                    timeInMillis = selectedDateMillis
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }

                selectedDateMillis = newCalendar.timeInMillis
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
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
            errorMessage = "عنوان یادآور را وارد کنید."
            return
        }

        val amount = amountText
            .replace(",", "")
            .replace("٬", "")
            .trim()
            .toLongOrNull() ?: 0L

        val calendar = Calendar.getInstance().apply {
            timeInMillis = selectedDateMillis
            set(Calendar.HOUR_OF_DAY, selectedHour)
            set(Calendar.MINUTE, selectedMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (calendar.timeInMillis <= System.currentTimeMillis() &&
            repeat == RepeatFrequency.ONCE
        ) {
            errorMessage = "برای یادآور یک‌بار، تاریخ و زمان باید در آینده باشد."
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

        if (existingReminder != null) {
            ReminderScheduler.cancel(context, existingReminder)
        }

        if (existingReminder != null) {
            ReminderStore.update(reminder)
        } else {
            ReminderStore.add(reminder)
        }

        ReminderScheduler.schedule(context, reminder)

        onSaved()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (existingReminder == null)
                            "افزودن یادآور"
                        else
                            "ویرایش یادآور"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "بازگشت"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        kind = ReminderKind.PAY
                    }
                ) {
                    Text("پرداخت")
                }

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        kind = ReminderKind.RECEIVE
                    }
                ) {
                    Text("دریافت")
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("عنوان یادآور")
                },
                singleLine = true
            )

            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    amountText = it.filter { ch ->
                        ch.isDigit() || ch == ',' || ch == '٬'
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("مبلغ")
                },
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = {
                    description = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("توضیحات")
                },
                minLines = 3
            )

            Text("تکرار")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                RepeatButton(
                    text = "یک بار",
                    selected = repeat == RepeatFrequency.ONCE
                ) {
                    repeat = RepeatFrequency.ONCE
                }

                RepeatButton(
                    text = "روزانه",
                    selected = repeat == RepeatFrequency.DAILY
                ) {
                    repeat = RepeatFrequency.DAILY
                }

                RepeatButton(
                    text = "هفتگی",
                    selected = repeat == RepeatFrequency.WEEKLY
                ) {
                    repeat = RepeatFrequency.WEEKLY
                }

                RepeatButton(
                    text = "ماهانه",
                    selected = repeat == RepeatFrequency.MONTHLY
                ) {
                    repeat = RepeatFrequency.MONTHLY
                }
            }

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    chooseDate()
                }
            ) {
                val c = Calendar.getInstance().apply {
                    timeInMillis = selectedDateMillis
                }

                Text(
                    "تاریخ: ${c.get(Calendar.YEAR)}/${c.get(Calendar.MONTH) + 1}/${c.get(Calendar.DAY_OF_MONTH)}"
                )
            }

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    chooseTime()
                }
            ) {
                Text(
                    "ساعت: %02d:%02d".format(
                        selectedHour,
                        selectedMinute
                    )
                )
            }

            errorMessage?.let {
                Text(it)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    save()
                }
            ) {
                Text("ذخیره یادآور")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RepeatButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    if (selected) {
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onClick
        ) {
            Text(text)
        }
    } else {
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onClick
        ) {
            Text(text)
        }
    }
}