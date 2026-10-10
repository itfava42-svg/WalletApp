package com.example.walletapp.sms

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Persists metadata belonging to transactions created from bank SMS.
 *
 * Transaction itself intentionally remains unchanged.
 * This store keeps the SMS-specific information separately and links
 * it to the transaction through transactionId.
 */
object SmsTransactionMetadataStore {

    private const val PREFS = "wallet_app_sms_metadata"
    private const val KEY_METADATA = "transaction_metadata"

    data class Metadata(
        val transactionId: String,
        val sender: String,
        val rawBody: String,
        val smsDate: Long,
        val originalBank: String,
        val accountNumber: String? = null,
        val cardNumber: String? = null,
        val cardLast4: String? = null,
        val balance: Long? = null,
    )

    private var appContext: Context? = null
    private val metadataMap = mutableMapOf<String, Metadata>()
    private var loaded = false

    fun attachContext(context: Context) {
        if (appContext == null) appContext = context.applicationContext
        if (!loaded) {
            loaded = true
            load()
        }
    }

    fun save(
        transactionId: String,
        sender: String,
        rawBody: String,
        smsDate: Long,
        originalBank: String,
        accountNumber: String? = null,
        cardNumber: String? = null,
        cardLast4: String? = null,
        balance: Long? = null,
    ) {
        val safeCard = SmsLearningStore.lastFourDigitsForStorage(cardLast4 ?: cardNumber)
        metadataMap[transactionId] = Metadata(
            transactionId = transactionId,
            sender = sender,
            rawBody = SmsLearningStore.normalizePatternForStorage(rawBody),
            smsDate = smsDate,
            originalBank = originalBank,
            accountNumber = SmsLearningStore.lastFourDigitsForStorage(accountNumber),
            cardNumber = safeCard,
            cardLast4 = safeCard,
            balance = balance,
        )
        persist()
    }

    fun get(transactionId: String): Metadata? = metadataMap[transactionId]

    fun remove(transactionId: String) {
        metadataMap.remove(transactionId)
        persist()
    }

    fun getAll(): List<Metadata> = metadataMap.values.toList()

    private fun persist() {
        val context = appContext ?: return
        val array = JSONArray()
        metadataMap.values.forEach { item ->
            val json = JSONObject()
            json.put("transactionId", item.transactionId)
            json.put("sender", item.sender)
            json.put("rawBody", item.rawBody)
            json.put("smsDate", item.smsDate)
            json.put("originalBank", item.originalBank)
            json.put("accountNumber", item.accountNumber ?: "")
            json.put("cardNumber", item.cardNumber ?: "")
            json.put("cardLast4", item.cardLast4 ?: "")
            json.put("balance", item.balance ?: JSONObject.NULL)
            array.put(json)
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_METADATA, array.toString()).apply()
    }

    private fun load() {
        val context = appContext ?: return
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_METADATA, null) ?: return

        var sanitizedLegacyBody = false
        var sanitizedLegacyMetadata = false
        runCatching {
            val array = JSONArray(raw)
            for (index in 0 until array.length()) {
                val json = array.getJSONObject(index)
                val transactionId = json.optString("transactionId")
                if (transactionId.isBlank()) continue

                val storedBody = json.optString("rawBody")
                val safeBody = SmsLearningStore.normalizePatternForStorage(storedBody)
                if (safeBody != storedBody) sanitizedLegacyBody = true

                val oldAccount = json.optString("accountNumber").takeIf { it.isNotBlank() }
                val safeAccount = SmsLearningStore.lastFourDigitsForStorage(oldAccount)
                if (safeAccount != oldAccount) sanitizedLegacyMetadata = true

                val oldCard = json.optString("cardNumber").takeIf { it.isNotBlank() }
                val oldCardLast4 = json.optString("cardLast4").takeIf { it.isNotBlank() }
                val safeCard = SmsLearningStore.lastFourDigitsForStorage(oldCardLast4 ?: oldCard)
                if (safeCard != oldCard || safeCard != oldCardLast4) sanitizedLegacyMetadata = true

                metadataMap[transactionId] = Metadata(
                    transactionId = transactionId,
                    sender = json.optString("sender"),
                    rawBody = safeBody,
                    smsDate = json.optLong("smsDate"),
                    originalBank = json.optString("originalBank"),
                    accountNumber = safeAccount,
                    cardNumber = safeCard,
                    cardLast4 = safeCard,
                    balance = if (json.has("balance") && !json.isNull("balance")) json.optLong("balance") else null,
                )
            }
        }.onSuccess {
            if (sanitizedLegacyBody || sanitizedLegacyMetadata) persist()
        }
    }

    fun clear() {
        metadataMap.clear()
        persist()
    }
}
