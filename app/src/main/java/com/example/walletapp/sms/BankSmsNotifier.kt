package com.example.walletapp.sms

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.walletapp.data.TransactionType
import com.example.walletapp.data.toPersianCurrency

object BankSmsNotifier {
    private const val CHANNEL_ID = "bank_sms_channel"

    fun notify(
        context: Context,
        bankName: String,
        amount: Long,
        merchant: String?,
        type: TransactionType,
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "تراکنش‌های بانکی",
                NotificationManager.IMPORTANCE_HIGH,
            )
            manager.createNotificationChannel(channel)
        }

        val sign = if (type == TransactionType.EXPENSE) "-" else "+"
        val title = merchant?.takeIf { it.isNotBlank() } ?: bankName
        val text = "$sign${amount.toPersianCurrency()} تومان" + if (merchant != null) " • $bankName" else ""

        // Small vector icon reference kept generic; Android requires a valid
        // drawable resource id here — using the app's launcher icon avoids
        // needing an extra drawable resource for this MVP.
        val smallIcon = context.applicationInfo.icon

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(smallIcon)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        runCatching {
            manager.notify(System.currentTimeMillis().toInt(), notification)
        }
    }
}