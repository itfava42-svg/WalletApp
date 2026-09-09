package com.example.walletapp.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.walletapp.data.ReminderStore

class ReminderBootReceiver :
    BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        if (
            intent.action !=
                Intent.ACTION_BOOT_COMPLETED &&
            intent.action !=
                Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            return
        }

        ReminderStore.attachContext(
            context
        )

        ReminderStore.reminders
            .filter {
                it.isActive
            }
            .forEach {

                ReminderScheduler.schedule(
                    context,
                    it
                )
            }
    }
}