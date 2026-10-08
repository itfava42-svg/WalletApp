package com.example.walletapp.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.walletapp.data.Reminder

object ReminderScheduler {

    private fun pendingIntent(
        context: Context,
        reminder: Reminder
    ): PendingIntent {
        val intent = Intent(
            context,
            ReminderReceiver::class.java
        ).apply {
            putExtra(
                ReminderReceiver.EXTRA_REMINDER_ID,
                reminder.id
            )
        }

        return PendingIntent.getBroadcast(
            context,
            reminder.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun canScheduleExactAlarms(
        context: Context
    ): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return true
        }

        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        return alarmManager.canScheduleExactAlarms()
    }

    fun schedule(
        context: Context,
        reminder: Reminder
    ) {
        if (!reminder.isActive) return

        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val pending = pendingIntent(context, reminder)

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            alarmManager.canScheduleExactAlarms()
        ) {
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminder.triggerAtMillis,
                    pending
                )
                return
            } catch (_: SecurityException) {
                // The special access can be revoked between the permission
                // check and the exact-alarm call. Fall through to the safe
                // inexact alarm instead of losing the reminder.
            }
        }

        // Safe fallback for Android 12+ when exact-alarm access is absent,
        // denied, or revoked during scheduling. set() never fires before the
        // requested trigger time and avoids the SecurityException risk of
        // an exact-alarm API without special access.
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            reminder.triggerAtMillis,
            pending
        )
    }

    fun cancel(
        context: Context,
        reminder: Reminder
    ) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.cancel(
            pendingIntent(
                context,
                reminder
            )
        )
    }
}
