package com.example.walletapp.sms

import android.content.Context
import android.net.Uri
import com.example.walletapp.data.TransactionStore

/**
 * Scans the device's SMS inbox for bank-transaction messages that were
 * missed by [SmsReceiver] (e.g. arrived before the app was installed, or
 * while the process was killed) and imports them into [TransactionStore].
 *
 * Keeps a persisted set of already-processed SMS row IDs in SharedPreferences
 * so re-running the scan doesn't create duplicate transactions.
 */
object SmsInboxScanner {
    private const val PREFS = "wallet_app_prefs"
    private const val KEY_PROCESSED_IDS = "processed_sms_ids"
    private val INBOX_URI: Uri = Uri.parse("content://sms/inbox")

    /** Returns the number of newly-imported transactions. */
    fun scanAndImport(context: Context): Int {
        TransactionStore.attachContext(context)

        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val processed = (prefs.getStringSet(KEY_PROCESSED_IDS, emptySet()) ?: emptySet()).toMutableSet()

        var addedCount = 0

        val cursor = runCatching {
            context.contentResolver.query(
                INBOX_URI,
                arrayOf("_id", "address", "body"),
                null,
                null,
                "date DESC",
            )
        }.getOrNull() ?: return 0

        cursor.use {
            val idIdx = it.getColumnIndex("_id")
            val addressIdx = it.getColumnIndex("address")
            val bodyIdx = it.getColumnIndex("body")
            if (idIdx < 0 || addressIdx < 0 || bodyIdx < 0) return@use

            while (it.moveToNext()) {
                val id = it.getString(idIdx) ?: continue
                if (id in processed) continue

                val address = it.getString(addressIdx)
                val body = it.getString(bodyIdx) ?: continue

                val parsed = BankSmsParser.parse(address, body)
                val amount = parsed?.amount
                if (parsed != null && amount != null) {
                    TransactionStore.addFromBankSms(
                        bankName = parsed.bankName,
                        amount = amount,
                        merchant = parsed.merchant,
                        type = parsed.type,
                    )
                    addedCount++
                }
                processed.add(id)
            }
        }

        prefs.edit().putStringSet(KEY_PROCESSED_IDS, processed).apply()
        return addedCount
    }
}