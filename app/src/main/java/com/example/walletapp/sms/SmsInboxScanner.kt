package com.example.walletapp.sms

import android.content.Context
import android.net.Uri
import com.example.walletapp.data.TransactionStore

object SmsInboxScanner {

    private const val PREFS = "wallet_app_prefs"
    private const val KEY_PROCESSED_IDS = "processed_sms_ids"

    private val INBOX_URI: Uri =
        Uri.parse("content://sms/inbox")

    fun scanAndImport(
        context: Context,
        fromMillis: Long,
        toMillis: Long,
    ): Int {

        TransactionStore.attachContext(context)
        SmsTransactionMetadataStore.attachContext(context)
        SmsProcessedStore.attachContext(context)

        val prefs =
            context.getSharedPreferences(
                PREFS,
                Context.MODE_PRIVATE,
            )

        val processed =
            (
                prefs.getStringSet(
                    KEY_PROCESSED_IDS,
                    emptySet(),
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

            val idIdx =
                it.getColumnIndex("_id")

            val addressIdx =
                it.getColumnIndex("address")

            val bodyIdx =
                it.getColumnIndex("body")

            val dateIdx =
                it.getColumnIndex("date")

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

                if (
                    smsDate < fromMillis ||
                    smsDate >= toMillis
                ) {
                    continue
                }

                val address =
                    it.getString(addressIdx)
                        ?: continue

                val body =
                    it.getString(bodyIdx)
                        ?: continue

                /*
                 * The live SmsReceiver and this scanner now share
                 * the exact same fingerprint.
                 */
                val claimed =
                    SmsProcessedStore.tryClaim(
                        context = context,
                        sender = address,
                        body = body,
                        smsDate = smsDate,
                    )

                if (!claimed) {
                    processed.add(id)
                    continue
                }

                val decision =
                    SmsDecisionEngine.decide(
                        context = context,
                        sender = address,
                        body = body,
                    )

                if (decision.ignored) {
                    processed.add(id)
                    continue
                }

                val parsed =
                    decision.parsed

                val amount =
                    parsed?.amount

                if (
                    parsed != null &&
                    amount != null
                ) {

                    try {

                        val transaction =
                            TransactionStore.addFromBankSms(
                                bankName = parsed.bankName,
                                amount = amount,
                                merchant = parsed.merchant,
                                type = parsed.type,
                            )

                        SmsTransactionMetadataStore.save(
                            transactionId = transaction.id,
                            sender = address,
                            rawBody = body,
                            smsDate = smsDate,
                            originalBank = parsed.bankName,
                        )

                        addedCount++

                    } catch (error: Throwable) {

                        SmsProcessedStore.release(
                            context = context,
                            sender = address,
                            body = body,
                            smsDate = smsDate,
                        )

                        throw error
                    }
                } else {

                    /*
                     * Parser did not recognize the SMS as a transaction.
                     * Release it so future parser improvements can retry it.
                     */
                    SmsProcessedStore.release(
                        context = context,
                        sender = address,
                        body = body,
                        smsDate = smsDate,
                    )
                }

                processed.add(id)
            }
        }

        prefs
            .edit()
            .putStringSet(
                KEY_PROCESSED_IDS,
                processed,
            )
            .apply()

        if (addedCount > 0) {
            SmsTransactionQueue.showNextIfNeeded(context)
        }

        return addedCount
    }

    fun scanAndImport(
        context: Context
    ): Int {

        val now =
            System.currentTimeMillis()

        val thirtyDaysAgo =
            now -
                (
                    30L *
                        24L *
                        60L *
                        60L *
                        1000L
                )

        return scanAndImport(
            context = context,
            fromMillis = thirtyDaysAgo,
            toMillis = now + 1L,
        )
    }
}
