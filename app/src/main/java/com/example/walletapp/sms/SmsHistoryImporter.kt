package com.example.walletapp.sms

import android.content.Context
import android.provider.Telephony
import com.example.walletapp.data.TransactionStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SmsHistoryImporter {
    data class Progress(
        val scanned: Int,
        val recognized: Int,
        val imported: Int,
        val duplicates: Int,
        val ignored: Int,
        val unsupported: Int,
    )

    suspend fun import(
        context: Context,
        accountId: String,
        limit: Int,
        onProgress: (Progress) -> Unit = {},
    ): Progress = withContext(Dispatchers.IO) {
        require(limit in 1..5000) { "Import limit must be between 1 and 5000." }

        TransactionStore.attachContext(context)
        SmsProcessedManager.attachContext(context)

        var scanned = 0
        var recognized = 0
        var imported = 0
        var duplicates = 0
        var ignored = 0
        var unsupported = 0

        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
        )

        val cursor = context.contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            projection,
            null,
            null,
            "${Telephony.Sms.DATE} DESC LIMIT $limit",
        ) ?: return@withContext Progress(0, 0, 0, 0, 0, 0)

        cursor.use { c ->
            val addressIndex = c.getColumnIndex(Telephony.Sms.ADDRESS)
            val bodyIndex = c.getColumnIndex(Telephony.Sms.BODY)
            val dateIndex = c.getColumnIndex(Telephony.Sms.DATE)

            if (addressIndex < 0 || bodyIndex < 0 || dateIndex < 0) {
                return@withContext Progress(0, 0, 0, 0, 0, 0)
            }

            while (c.moveToNext()) {
                scanned++
                val sender = c.getString(addressIndex)
                val body = c.getString(bodyIndex).orEmpty()
                val smsDate = c.getLong(dateIndex)

                val decision = SmsDecisionEngine.decide(context, sender, body)

                if (decision.ignored) {
                    ignored++
                } else {
                    val parsed = decision.parsed
                    val amount = parsed?.amount

                    if (parsed == null) {
                        unsupported++
                    } else if (amount == null || amount <= 0L || parsed.isTransfer) {
                        unsupported++
                    } else {
                        recognized++
                        val sourceId = SmsProcessedManager.sourceId(
                            sender = sender.orEmpty(),
                            smsDate = smsDate,
                            body = body,
                        )

                        val alreadyExists = TransactionStore.transactions.any {
                            it.sourceId == sourceId
                        }

                        if (alreadyExists || !SmsProcessedManager.tryClaim(
                                context, sender, body, smsDate
                            )
                        ) {
                            duplicates++
                        } else {
                            try {
                                val category =
                                    decision.matchedPattern?.let {
                                        SmsLearningManager.findPatternRule(
                                            sender = sender.orEmpty(),
                                            messageBody = body,
                                        )?.category
                                    } ?: SmsLearningManager.getSenderCategory(
                                        sender.orEmpty()
                                    )

                                TransactionStore.addImportedBankSms(
                                    accountId = accountId,
                                    bankName = parsed.bankName,
                                    amount = amount,
                                    merchant = parsed.merchant,
                                    type = parsed.type,
                                    smsDate = smsDate,
                                    sourceId = sourceId,
                                    category = category,
                                )
                                imported++
                            } catch (_: Throwable) {
                                SmsProcessedManager.release(
                                    context, sender, body, smsDate
                                )
                                unsupported++
                            }
                        }
                    }
                }

                onProgress(
                    Progress(
                        scanned, recognized, imported, duplicates, ignored, unsupported
                    )
                )
            }
        }

        Progress(scanned, recognized, imported, duplicates, ignored, unsupported)
    }
}
