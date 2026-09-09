package com.example.walletapp.sms

import android.content.Context
import java.security.MessageDigest

/**
 * Prevents the same SMS from being imported more than once,
 * regardless of whether it arrives through the live receiver
 * or the inbox scanner.
 */
object SmsProcessedStore {

    private const val PREFS = "wallet_app_sms_processed"
    private const val KEY_PROCESSED = "processed_sms_fingerprints"
    private const val MAX_ITEMS = 5000

    private var appContext: Context? = null
    private var loaded = false

    private val processed = linkedSetOf<String>()

    @Synchronized
    fun attachContext(context: Context) {
        if (appContext == null) {
            appContext = context.applicationContext
        }

        if (!loaded) {
            loaded = true

            val stored =
                appContext
                    ?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    ?.getStringSet(KEY_PROCESSED, emptySet())
                    ?: emptySet()

            processed.clear()
            processed.addAll(stored)
        }
    }

    /**
     * Atomically claims an SMS fingerprint.
     *
     * Returns true only for the first processing attempt.
     * Any repeated delivery of the same SMS returns false.
     */
    @Synchronized
    fun tryClaim(
        context: Context,
        sender: String?,
        body: String,
        smsDate: Long,
    ): Boolean {

        attachContext(context)

        val fingerprint =
            fingerprint(
                sender = sender,
                body = body,
                smsDate = smsDate,
            )

        if (fingerprint in processed) {
            return false
        }

        processed.add(fingerprint)

        while (processed.size > MAX_ITEMS) {
            val first = processed.firstOrNull() ?: break
            processed.remove(first)
        }

        persist()

        return true
    }

    @Synchronized
    fun release(
        context: Context,
        sender: String?,
        body: String,
        smsDate: Long,
    ) {
        attachContext(context)

        processed.remove(
            fingerprint(
                sender = sender,
                body = body,
                smsDate = smsDate,
            )
        )

        persist()
    }

    private fun fingerprint(
        sender: String?,
        body: String,
        smsDate: Long,
    ): String {

        val normalizedSender =
            sender
                ?.trim()
                ?.replace("\\s+".toRegex(), "")
                ?.lowercase()
                ?: ""

        val normalizedBody =
            body
                .replace("\r", "")
                .replace("\n", "")
                .replace("\\s+".toRegex(), " ")
                .trim()

        val source =
            "$normalizedSender|$smsDate|$normalizedBody"

        val digest =
            MessageDigest
                .getInstance("SHA-256")
                .digest(source.toByteArray(Charsets.UTF_8))

        return digest.joinToString("") {
            "%02x".format(it)
        }
    }

    private fun persist() {
        val context = appContext ?: return

        context
            .getSharedPreferences(
                PREFS,
                Context.MODE_PRIVATE,
            )
            .edit()
            .putStringSet(
                KEY_PROCESSED,
                processed.toSet(),
            )
            .apply()
    }

    @Synchronized
    fun clear(context: Context) {
        attachContext(context)
        processed.clear()
        persist()
    }
}
