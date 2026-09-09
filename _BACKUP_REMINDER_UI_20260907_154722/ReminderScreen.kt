package com.example.walletapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.walletapp.data.Reminder
import com.example.walletapp.data.ReminderStore
import com.example.walletapp.reminders.ReminderScheduler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen(
    onBack: () -> Unit,
    onAddReminder: () -> Unit,
    onEditReminder: (Reminder) -> Unit,
) {
    val context = LocalContext.current

    var reminders by remember {
        mutableStateOf(ReminderStore.reminders.toList())
    }

    var reminderToDelete by remember {
        mutableStateOf<Reminder?>(null)
    }

    fun refresh() {
        reminders = ReminderStore.reminders.toList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("یادآورها") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "بازگشت"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddReminder
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "افزودن یادآور"
                )
            }
        }
    ) { paddingValues ->

        if (reminders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "هنوز یادآوری ثبت نشده است",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(onClick = onAddReminder) {
                        Text("افزودن یادآور")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                }

                items(
                    items = reminders,
                    key = { it.id }
                ) { reminder ->

                    ReminderCard(
                        reminder = reminder,
                        onEdit = {
                            onEditReminder(reminder)
                        },
                        onDelete = {
                            reminderToDelete = reminder
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    reminderToDelete?.let { reminder ->

        AlertDialog(
            onDismissRequest = {
                reminderToDelete = null
            },
            title = {
                Text("حذف یادآور")
            },
            text = {
                Text("آیا از حذف «${reminder.title}» مطمئن هستید؟")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        ReminderScheduler.cancel(context, reminder)
                        ReminderStore.remove(reminder.id)
                        reminderToDelete = null
                        refresh()
                    }
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        reminderToDelete = null
                    }
                ) {
                    Text("انصراف")
                }
            }
        )
    }
}

@Composable
private fun ReminderCard(
    reminder: Reminder,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = reminder.title,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (reminder.kind.name == "RECEIVE")
                            "دریافت"
                        else
                            "پرداخت",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (reminder.amount > 0) {
                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "مبلغ: ${reminder.amount}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = reminder.repeat.label,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "ویرایش"
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف"
                    )
                }
            }

            if (reminder.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = reminder.description,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}