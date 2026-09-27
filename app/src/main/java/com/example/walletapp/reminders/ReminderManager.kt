package com.example.walletapp.reminders

import android.content.Context
import com.example.walletapp.data.Reminder
import com.example.walletapp.data.ReminderStore

object ReminderManager {

    fun save(
        context: Context,
        reminder: Reminder,
        existingReminder: Reminder?,
    ) {
        if (existingReminder != null) {
            ReminderScheduler.cancel(
                context,
                existingReminder,
            )

            ReminderStore.update(
                reminder,
            )
        } else {
            ReminderStore.add(
                reminder,
            )
        }

        ReminderScheduler.schedule(
            context,
            reminder,
        )
    }

    /**
     * Domain boundary for removing a reminder.
     *
     * The store remains the authoritative owner of reminder state.
     */
    fun remove(
        id: String,
    ) {
        ReminderStore.remove(id)
    }

    /**
     * Removes a reminder and cancels its scheduled alarm.
     */
    fun remove(
        context: Context,
        id: String,
    ) {
        ReminderStore.getById(id)?.let { reminder ->
            ReminderScheduler.cancel(
                context,
                reminder,
            )
        }
        ReminderStore.remove(id)
    }

    /**
     * Domain boundary for changing reminder completion state.
     */
    fun setCompleted(
        id: String,
        completed: Boolean,
    ) {
        ReminderStore.setCompleted(
            id,
            completed,
        )
    }

    /**
     * Marks a reminder complete and cancels its pending alarm.
     */
    fun setCompleted(
        context: Context,
        id: String,
        completed: Boolean,
    ) {
        if (completed) {
            ReminderStore.getById(id)?.let { reminder ->
                ReminderScheduler.cancel(
                    context,
                    reminder,
                )
            }
        }

        ReminderStore.setCompleted(
            id,
            completed,
        )
    }
}

