package com.example.walletapp.reminders

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.walletapp.data.ReminderStore

class ReminderBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val isBootOrReplacement =
            action == Intent.ACTION_BOOT_COMPLETED ||
                action == Intent.ACTION_MY_PACKAGE_REPLACED
        val isExactAlarmPermissionGranted =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                action == AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED

        if (!isBootOrReplacement && !isExactAlarmPermissionGranted) {
            return
        }

        // The permission-state broadcast is sent on grant, but may arrive after
        // the permission has already been revoked again. Re-check before rescheduling.
        if (isExactAlarmPermissionGranted) {
            val alarmManager =
                context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
                    ?: return
            if (!alarmManager.canScheduleExactAlarms()) {
                return
            }
        }

        ReminderStore.attachContext(context)
        ReminderStore.reminders
            .filter { it.isActive }
            .forEach { reminder ->
                ReminderScheduler.schedule(context, reminder)
            }
    }
}
