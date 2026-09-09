package com.example.walletapp.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.walletapp.data.ReminderStore
import com.example.walletapp.data.RepeatFrequency
import java.util.Calendar

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_REMINDER_ID = "reminder_id"
    }

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {
        val id = intent.getStringExtra(
            EXTRA_REMINDER_ID
        ) ?: return

        ReminderStore.attachContext(context)

        val reminder = ReminderStore.getById(id)
            ?: return

        if (!reminder.isActive || reminder.isCompleted) {
            return
        }

        ReminderNotifier.notify(
            context,
            reminder
        )

        if (reminder.repeat == RepeatFrequency.ONCE) {
            ReminderStore.update(
                reminder.copy(
                    isActive = false
                )
            )
            return
        }

        val calendar = Calendar.getInstance().apply {
            timeInMillis = reminder.triggerAtMillis

            when (reminder.repeat) {
                RepeatFrequency.DAILY ->
                    add(Calendar.DAY_OF_MONTH, 1)

                RepeatFrequency.WEEKLY ->
                    add(Calendar.DAY_OF_MONTH, 7)

                RepeatFrequency.MONTHLY -> {
                    val originalDay =
                        get(Calendar.DAY_OF_MONTH)

                    add(Calendar.MONTH, 1)

                    val maxDay =
                        getActualMaximum(Calendar.DAY_OF_MONTH)

                    set(
                        Calendar.DAY_OF_MONTH,
                        minOf(originalDay, maxDay)
                    )
                }

                RepeatFrequency.ONCE -> Unit
            }
        }

        var nextMillis = calendar.timeInMillis

        if (nextMillis <= System.currentTimeMillis()) {
            nextMillis = System.currentTimeMillis() + 60_000L
        }

        val next = reminder.copy(
            triggerAtMillis = nextMillis
        )

        ReminderStore.update(next)

        ReminderScheduler.schedule(
            context,
            next
        )
    }
}
