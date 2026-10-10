package com.example.walletapp.sms

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Persists user corrections/preferences learned from SMS transactions.
 *
 * This store is intentionally independent from BankSenderRegistry:
 * - BankSenderRegistry = built-in/default knowledge
 * - SmsLearningStore = user-specific corrections and learned preferences
 *
 * User corrections always have priority over built-in registry values.
 */
object SmsLearningStore {

    private const val PREFS_NAME = "sms_learning_store"

    private const val KEY_SENDER_BANK = "sender_bank"
    private const val KEY_SENDER_TYPE = "sender_type"
    private const val KEY_SENDER_CATEGORY = "sender_category"
    private const val KEY_PATTERN_RULES = "pattern_rules"

    private var appContext: Context? = null

    /**
     * Connects the store to application context.
     */
    fun attachContext(context: Context) {
        if (appContext == null) {
            appContext = context.applicationContext
        }
    }

    private fun prefs() =
        requireNotNull(appContext) {
            "SmsLearningStore.attachContext(context) must be called first"
        }.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ---------------------------------------------------------------------
    // Sender -> Bank
    // ---------------------------------------------------------------------

    fun saveSenderBank(sender: String, bankName: String) {
        val normalizedSender = normalizeSender(sender)

        if (normalizedSender.isBlank() || bankName.isBlank()) return

        val current = readStringMap(KEY_SENDER_BANK).toMutableMap()
        current[normalizedSender] = bankName.trim()
        writeStringMap(KEY_SENDER_BANK, current)
    }

    /**
     * Returns all learned sender -> bank mappings.
     */
    fun getAllSenderBanks(): Map<String, String> =
        readStringMap(KEY_SENDER_BANK)

    /**
     * Clears all learned SMS rules and sender-level learning.
     */
    fun clearAllLearning() {
        prefs()
            .edit()
            .remove(KEY_SENDER_BANK)
            .remove(KEY_SENDER_TYPE)
            .remove(KEY_SENDER_CATEGORY)
            .remove(KEY_PATTERN_RULES)
            .apply()
    }
    fun getSenderBank(sender: String?): String? {
        if (sender == null) return null

        val normalizedSender = normalizeSender(sender)
        if (normalizedSender.isBlank()) return null

        return readStringMap(KEY_SENDER_BANK)[normalizedSender]
    }

    fun removeSenderBank(sender: String) {
        val normalizedSender = normalizeSender(sender)
        if (normalizedSender.isBlank()) return

        val current = readStringMap(KEY_SENDER_BANK).toMutableMap()
        current.remove(normalizedSender)
        writeStringMap(KEY_SENDER_BANK, current)
    }

    // ---------------------------------------------------------------------
    // Sender -> Transaction Type
    // ---------------------------------------------------------------------

    fun saveSenderTransactionType(
        sender: String,
        type: String,
    ) {
        val normalizedSender = normalizeSender(sender)

        if (normalizedSender.isBlank() || type.isBlank()) return

        val current = readStringMap(KEY_SENDER_TYPE).toMutableMap()
        current[normalizedSender] = type.trim()
        writeStringMap(KEY_SENDER_TYPE, current)
    }

    fun getSenderTransactionType(sender: String?): String? {
        if (sender == null) return null

        val normalizedSender = normalizeSender(sender)
        if (normalizedSender.isBlank()) return null

        return readStringMap(KEY_SENDER_TYPE)[normalizedSender]
    }

    // ---------------------------------------------------------------------
    // Sender -> Default Category
    // ---------------------------------------------------------------------

    fun saveSenderCategory(
        sender: String,
        category: String,
    ) {
        val normalizedSender = normalizeSender(sender)

        if (normalizedSender.isBlank() || category.isBlank()) return

        val current = readStringMap(KEY_SENDER_CATEGORY).toMutableMap()
        current[normalizedSender] = category.trim()
        writeStringMap(KEY_SENDER_CATEGORY, current)
    }

    fun getSenderCategory(sender: String?): String? {
        if (sender == null) return null

        val normalizedSender = normalizeSender(sender)
        if (normalizedSender.isBlank()) return null

        return readStringMap(KEY_SENDER_CATEGORY)[normalizedSender]
    }

    // ---------------------------------------------------------------------
    // Sender + Message Pattern -> Learned values
    // ---------------------------------------------------------------------

    /**
     * Saves a learned rule for a specific sender and message pattern.
     *
     * Example:
     * sender = "300017"
     * pattern = "خرید"
     * bankName = "بانک ملی"
     * category = "خرید"
     */
    fun savePatternRule(
        sender: String,
        pattern: String,
        bankName: String? = null,
        category: String? = null,
        transactionType: String? = null,
    ) {
        val normalizedSender = normalizeSender(sender)
        val normalizedPattern = normalizeLearningPattern(pattern)

        if (normalizedSender.isBlank() || normalizedPattern.isBlank()) return

        val rules = readPatternRules().toMutableList()

        val existingIndex = rules.indexOfFirst {
            normalizeSender(it.sender) == normalizedSender &&
                normalizePattern(it.pattern) == normalizedPattern
        }

        val newRule = PatternRule(
            sender = normalizedSender,
            pattern = normalizedPattern,
            bankName = bankName?.trim()?.takeIf { it.isNotBlank() },
            category = category?.trim()?.takeIf { it.isNotBlank() },
            transactionType = transactionType?.trim()?.takeIf { it.isNotBlank() },
        )

        if (existingIndex >= 0) {
            rules[existingIndex] = newRule
        } else {
            rules.add(newRule)
        }

        writePatternRules(rules)
    }

    fun findPatternRule(
        sender: String?,
        messageBody: String,
    ): PatternRule? {
        if (sender == null) return null

        val normalizedSender = normalizeSender(sender)
        val normalizedBody = normalizeLearningPattern(messageBody)

        if (normalizedSender.isBlank() || normalizedBody.isBlank()) {
            return null
        }

        return readPatternRules()
            .asSequence()
            .filter { normalizeSender(it.sender) == normalizedSender }
            .filter { normalizedBody.contains(normalizePattern(it.pattern)) }
            .maxByOrNull { normalizePattern(it.pattern).length }
    }

    fun getAllPatternRules(): List<PatternRule> =
        readPatternRules()

    fun removePatternRule(
        sender: String,
        pattern: String,
    ) {
        val normalizedSender = normalizeSender(sender)
        val normalizedPattern = normalizeLearningPattern(pattern)

        val rules = readPatternRules()
            .filterNot {
                normalizeSender(it.sender) == normalizedSender &&
                    normalizePattern(it.pattern) == normalizedPattern
            }

        writePatternRules(rules)
    }

    // ---------------------------------------------------------------------
    // Internal persistence helpers
    // ---------------------------------------------------------------------

    private fun readStringMap(key: String): Map<String, String> {
        val json = prefs().getString(key, null) ?: return emptyMap()

        return try {
            val objectJson = JSONObject(json)
            val result = mutableMapOf<String, String>()

            objectJson.keys().forEach { mapKey ->
                val value = objectJson.optString(mapKey, "")
                if (mapKey.isNotBlank() && value.isNotBlank()) {
                    result[mapKey] = value
                }
            }

            result
        } catch (_: Exception) {
            emptyMap()
        }
    }

    private fun writeStringMap(
        key: String,
        values: Map<String, String>,
    ) {
        val json = JSONObject()

        values.forEach { (mapKey, value) ->
            if (mapKey.isNotBlank() && value.isNotBlank()) {
                json.put(mapKey, value)
            }
        }

        prefs()
            .edit()
            .putString(key, json.toString())
            .apply()
    }

    private fun readPatternRules(): List<PatternRule> {
        val json = prefs().getString(KEY_PATTERN_RULES, null)
            ?: return emptyList()

        return try {
            val array = JSONArray(json)
            val result = mutableListOf<PatternRule>()

            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue

                val sender = item.optString("sender", "")
                val pattern = item.optString("pattern", "")

                if (sender.isBlank() || pattern.isBlank()) continue

                result.add(
                    PatternRule(
                        sender = sender,
                        pattern = pattern,
                        bankName = item
                            .optString("bankName", "")
                            .takeIf { it.isNotBlank() },

                        category = item
                            .optString("category", "")
                            .takeIf { it.isNotBlank() },

                        transactionType = item
                            .optString("transactionType", "")
                            .takeIf { it.isNotBlank() },
                    )
                )
            }

            result
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun writePatternRules(
        rules: List<PatternRule>,
    ) {
        val array = JSONArray()

        rules.forEach { rule ->
            val item = JSONObject()

            item.put("sender", rule.sender)
            item.put("pattern", rule.pattern)

            rule.bankName?.let {
                item.put("bankName", it)
            }

            rule.category?.let {
                item.put("category", it)
            }

            rule.transactionType?.let {
                item.put("transactionType", it)
            }

            array.put(item)
        }

        prefs()
            .edit()
            .putString(KEY_PATTERN_RULES, array.toString())
            .apply()
    }

    /**
     * Normalizes sender identifiers so +98 / 0098 variations
     * can later be treated consistently.
     */
    private fun normalizeSender(sender: String): String {
        return sender
            .trim()
            .removePrefix("+98")
            .removePrefix("0098")
            .removePrefix("98")
            .trim()
    }

    /**
     * Lightweight normalization for pattern matching.
     * We intentionally do NOT try to normalize the whole SMS aggressively,
     * because the original message must remain untouched for parsing.
     */
    private fun normalizePattern(value: String): String {
        return value
            .trim()
            .replace('\u064A', '\u06CC')
            .replace('\u0643', '\u06A9')
            .replace(Regex("\\s+"), " ")
    }

    /** Returns a reusable learning pattern without retaining message numbers or identifiers. */
    internal fun normalizePatternForStorage(value: String): String =
        normalizeLearningPattern(value)

    /** Keeps only the final four numeric characters of a sensitive identifier. */
    internal fun lastFourDigitsForStorage(value: String?): String? {
        val digits = value
            ?.mapNotNull { char -> Character.digit(char, 10).takeIf { it >= 0 }?.toChar() }
            ?.joinToString("")
            .orEmpty()
        return digits.takeLast(4).takeIf { it.length == 4 }
    }

    private fun normalizeLearningPattern(value: String): String {
        return value
            .trim()
            .replace('\u064A', '\u06CC')
            .replace('\u0643', '\u06A9')
            .replace(Regex("[0-9۰-۹٠-٩]+"), "<NUM>")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
    data class PatternRule(
        val sender: String,
        val pattern: String,
        val bankName: String? = null,
        val category: String? = null,
        val transactionType: String? = null,
    )
}



