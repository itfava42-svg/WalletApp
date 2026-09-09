package com.example.walletapp.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.walletapp.data.Reminder
import com.example.walletapp.data.ReminderKind
import com.example.walletapp.data.toPersianCurrency

object ReminderNotifier {

    private const val CHANNEL_ID =
        "wallet_reminders"

    fun notify(
        context: Context,
        reminder: Reminder
    ) {

        val manager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "یادآورها",
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }

        val type =
            if (
                reminder.kind ==
                ReminderKind.PAY
            ) {
                "پرداخت"
            } else {
                "دریافت"
            }

        val text =
            "$type ${reminder.amount.toPersianCurrency()} تومان" +
                if (
                    reminder.description
                        .isNotBlank()
                ) {
                    " — ${reminder.description}"
                } else {
                    ""
                }

        val notification =
            NotificationCompat.Builder(
                context,
                CHANNEL_ID
            )
                .setSmallIcon(
                    android.R.drawable.ic_dialog_info
                )
                .setContentTitle(
                    reminder.title
                )
                .setContentText(text)
                .setPriority(
                    NotificationCompat.PRIORITY_HIGH
                )
                .setAutoCancel(true)
                .build()

        manager.notify(
            reminder.id.hashCode(),
            notification
        )
    }
}