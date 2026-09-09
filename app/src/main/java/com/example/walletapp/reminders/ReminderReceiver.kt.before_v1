package com.example.walletapp.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.walletapp.data.ReminderStore
import com.example.walletapp.data.RepeatFrequency
import java.util.Calendar

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_REMINDER_ID =
            "reminder_id"
    }

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        val id =
            intent.getStringExtra(
                EXTRA_REMINDER_ID
            ) ?: return

        ReminderStore.attachContext(context)

        val reminder =
            ReminderStore.getById(id)
                ?: return

        if (!reminder.isActive) return

        ReminderNotifier.notify(
            context,
            reminder
        )

        if (
            reminder.repeat ==
            RepeatFrequency.ONCE
        ) {

            ReminderStore.update(
                reminder.copy(
                    isActive = false
                )
            )

            return
        }

        val calendar =
            Calendar.getInstance().apply {

                timeInMillis =
                    reminder.triggerAtMillis

                when (
                    reminder.repeat
                ) {

                    RepeatFrequency.DAILY ->
                        add(
                            Calendar.DAY_OF_MONTH,
                            1
                        )

                    RepeatFrequency.WEEKLY ->
                        add(
                            Calendar.DAY_OF_MONTH,
                            7
                        )

                    RepeatFrequency.MONTHLY ->
                        add(
                            Calendar.MONTH,
                            1
                        )

                    RepeatFrequency.ONCE -> Unit
                }
            }

        val next =
            reminder.copy(
                triggerAtMillis =
                    calendar.timeInMillis
            )

        ReminderStore.update(next)

        ReminderScheduler.schedule(
            context,
            next
        )
    }
}