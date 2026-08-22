package com.example.walletapp.sms

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import com.example.walletapp.data.TransactionType
import com.example.walletapp.data.toPersianCurrency

/**
 * Shows a small floating card on top of whatever the user is currently
 * doing (requires the "display over other apps" / SYSTEM_ALERT_WINDOW
 * permission). Falls back to doing nothing if the permission hasn't been
 * granted — the caller should also post a regular notification as a
 * fallback in that case.
 *
 * Built with plain Android views rather than Compose because attaching a
 * ComposeView to a raw WindowManager overlay (outside of any Activity)
 * requires manually wiring a lifecycle/SavedState owner, which is a lot of
 * extra machinery for a simple, short-lived popup like this.
 */
object OverlayPopupManager {

    private const val AUTO_DISMISS_MS = 6000L

    fun canShowOverlay(context: Context): Boolean =
        Settings.canDrawOverlays(context.applicationContext)

    fun showTransactionPopup(
        context: Context,
        bankName: String,
        amount: Long?,
        merchant: String?,
        type: TransactionType,
    ) {
        val appContext = context.applicationContext
        if (!canShowOverlay(appContext)) return

        val windowManager = appContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val density = appContext.resources.displayMetrics.density
        fun dp(value: Int) = (value * density).toInt()

        val isExpense = type == TransactionType.EXPENSE
        val accentColor = if (isExpense) Color.parseColor("#BA1A1A") else Color.parseColor("#006A62")

        val container = LinearLayout(appContext).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(14))
            background = GradientDrawable().apply {
                cornerRadius = dp(16).toFloat()
                setColor(Color.WHITE)
                setStroke(dp(1), Color.parseColor("#BACAC6"))
            }
            elevation = dp(8).toFloat()
        }

        val titleRow = LinearLayout(appContext).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val bankLabel = TextView(appContext).apply {
            text = bankName
            setTextColor(Color.parseColor("#131B2E"))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        }
        titleRow.addView(bankLabel)
        container.addView(titleRow)

        if (amount != null) {
            val amountLabel = TextView(appContext).apply {
                val sign = if (isExpense) "-" else "+"
                text = "$sign${amount.toPersianCurrency()} تومان"
                setTextColor(accentColor)
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                setPadding(0, dp(4), 0, 0)
            }
            container.addView(amountLabel)
        }

        if (!merchant.isNullOrBlank()) {
            val merchantLabel = TextView(appContext).apply {
                text = merchant
                setTextColor(Color.parseColor("#3B4A47"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                setPadding(0, dp(2), 0, 0)
            }
            container.addView(merchantLabel)
        }

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            android.graphics.PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = dp(56)
        }

        container.setOnClickListener {
            runCatching { windowManager.removeView(container) }
        }

        runCatching { windowManager.addView(container, params) }

        Handler(Looper.getMainLooper()).postDelayed({
            runCatching { windowManager.removeView(container) }
        }, AUTO_DISMISS_MS)
    }
}