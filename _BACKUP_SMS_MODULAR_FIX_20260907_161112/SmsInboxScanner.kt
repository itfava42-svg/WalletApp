package com.example.walletapp.sms

import android.content.Context
import android.net.Uri
import com.example.walletapp.data.TransactionStore

/**
 * Scans the device SMS inbox inside a selected time range.
 *
 * Already processed SMS row IDs are persisted so the same SMS
 * cannot create duplicate transactions.
 */
object SmsInboxScanner {

    private const val PREFS = "wallet_app_prefs"
    private const val KEY_PROCESSED_IDS = "processed_sms_ids"

    private val INBOX_URI: Uri = Uri.parse("content://sms/inbox")

    /**
     * Imports bank transactions from SMS messages between
     * [fromMillis] (inclusive) and [toMillis] (exclusive).
     */
    fun scanAndImport(
        context: Context,
        fromMillis: Long,
        toMillis: Long,
    ): Int {

        TransactionStore.attachContext(context)

        val prefs =
            context.getSharedPreferences(
                PREFS,
                Context.MODE_PRIVATE
            )

        val processed =
            (
                prefs.getStringSet(
                    KEY_PROCESSED_IDS,
                    emptySet()
                ) ?: emptySet()
            ).toMutableSet()

        var addedCount = 0

        val cursor =
            runCatching {
                context.contentResolver.query(
                    INBOX_URI,
                    arrayOf(
                        "_id",
                        "address",
                        "body",
                        "date",
                    ),
                    "date >= ? AND date < ?",
                    arrayOf(
                        fromMillis.toString(),
                        toMillis.toString(),
                    ),
                    "date DESC",
                )
            }.getOrNull()
                ?: return 0

        cursor.use {

            val idIdx = it.getColumnIndex("_id")
            val addressIdx = it.getColumnIndex("address")
            val bodyIdx = it.getColumnIndex("body")
            val dateIdx = it.getColumnIndex("date")

            if (
                idIdx < 0 ||
                addressIdx < 0 ||
                bodyIdx < 0 ||
                dateIdx < 0
            ) {
                return@use
            }

            while (it.moveToNext()) {

                val id =
                    it.getString(idIdx)
                        ?: continue

                if (id in processed) {
                    continue
                }

                val smsDate =
                    runCatching {
                        it.getLong(dateIdx)
                    }.getOrNull()
                        ?: continue

                if (smsDate < fromMillis || smsDate >= toMillis) {
                    continue
                }

                val address =
                    it.getString(addressIdx)
                        ?: continue

                val body =
                    it.getString(bodyIdx)
                        ?: continue

                val parsed =
                    BankSmsParser.parse(
                        address,
                        body
                    )

                val amount = parsed?.amount

                if (
                    parsed != null &&
                    amount != null
                ) {
                    TransactionStore.addFromBankSms(
                        bankName = parsed.bankName,
                        amount = amount,
                        merchant = parsed.merchant,
                        type = parsed.type,
                    )

                    addedCount++
                }

                /*
                 * Mark the SMS as processed even when it isn't
                 * a bank transaction. This preserves the original
                 * duplicate-prevention behavior.
                 */
                processed.add(id)
            }
        }

        prefs.edit()
            .putStringSet(
                KEY_PROCESSED_IDS,
                processed
            )
            .apply()

        return addedCount
    }

    /**
     * Backward-compatible scan method.
     *
     * Scans the last 30 days.
     */
    fun scanAndImport(context: Context): Int {

        val now = System.currentTimeMillis()

        val thirtyDaysAgo =
            now - (30L * 24L * 60L * 60L * 1000L)

        return scanAndImport(
            context = context,
            fromMillis = thirtyDaysAgo,
            toMillis = now + 1L,
        )
    }
}
