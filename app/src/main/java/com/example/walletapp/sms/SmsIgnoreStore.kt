package com.example.walletapp.sms

import android.content.Context
import org.json.JSONArray

/**
 * Persistent rules for ignoring bank SMS senders or specific message patterns.
 *
 * Sender-level ignore:
 *   Completely ignores future SMS messages from a sender.
 *
 * Pattern-level ignore:
 *   Ignores only messages from a sender that contain a specific pattern.
 *
 * This store is intentionally separate from BankSenderRegistry and
 * SmsLearningStore so each responsibility remains independent.
 */
object SmsIgnoreStore {

    private const val PREFS_NAME = "sms_ignore_store"

    private const val KEY_IGNORED_SENDERS = "ignored_senders"
    private const val KEY_IGNORED_PATTERNS = "ignored_patterns"

    private var appContext: Context? = null

    fun attachContext(context: Context) {
        if (appContext == null) {
            appContext = context.applicationContext
        }
    }

    private fun prefs() =
        requireNotNull(appContext) {
            "SmsIgnoreStore.attachContext(context) must be called first"
        }.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ---------------------------------------------------------------------
    // Sender-level ignore
    // ---------------------------------------------------------------------

    /**
     * Marks a sender as completely ignored.
     *
     * Example:
     * "300017" -> all future SMS from this sender are ignored.
     */
    fun ignoreSender(sender: String) {
        val normalizedSender = normalizeSender(sender)

        if (normalizedSender.isBlank()) return

        val senders = getIgnoredSenders().toMutableSet()
        senders.add(normalizedSender)

        saveIgnoredSenders(senders)
    }

    /**
     * Returns true when the sender has been completely ignored.
     */
    fun isSenderIgnored(sender: String?): Boolean {
        if (sender == null) return false

        val normalizedSender = normalizeSender(sender)

        if (normalizedSender.isBlank()) return false

        return normalizedSender in getIgnoredSenders()
    }

    /**
     * Removes a sender from the complete-ignore list.
     */
    fun unignoreSender(sender: String) {
        val normalizedSender = normalizeSender(sender)

        if (normalizedSender.isBlank()) return

        val senders = getIgnoredSenders().toMutableSet()
        senders.remove(normalizedSender)

        saveIgnoredSenders(senders)
    }

    /**
     * Returns all completely ignored senders.
     */
    /**
     * Clears all sender-level and pattern-level ignore rules.
     */
    fun clearAllIgnored() {
        prefs()
            .edit()
            .remove(KEY_IGNORED_SENDERS)
            .remove(KEY_IGNORED_PATTERNS)
            .apply()
    }
    fun getIgnoredSenders(): Set<String> {
        val json = prefs().getString(KEY_IGNORED_SENDERS, null)
            ?: return emptySet()

        return try {
            val array = JSONArray(json)
            buildSet {
                for (index in 0 until array.length()) {
                    val sender = normalizeSender(
                        array.optString(index, "")
                    )

                    if (sender.isNotBlank()) {
                        add(sender)
                    }
                }
            }
        } catch (_: Exception) {
            emptySet()
        }
    }

    private fun saveIgnoredSenders(
        senders: Set<String>,
    ) {
        val array = JSONArray()

        senders
            .filter { it.isNotBlank() }
            .sorted()
            .forEach { sender ->
                array.put(sender)
            }

        prefs()
            .edit()
            .putString(KEY_IGNORED_SENDERS, array.toString())
            .apply()
    }

    // ---------------------------------------------------------------------
    // Message-pattern ignore
    // ---------------------------------------------------------------------

    /**
     * Ignores messages from a specific sender when the SMS contains
     * the supplied pattern.
     *
     * Example:
     *
     * sender = "300017"
     * pattern = "رمز پویا"
     *
     * Only messages from 300017 containing "رمز پویا" will be ignored.
     */
    fun ignorePattern(
        sender: String,
        pattern: String,
    ) {
        val normalizedSender = normalizeSender(sender)
        val normalizedPattern = normalizePattern(pattern)

        if (
            normalizedSender.isBlank() ||
            normalizedPattern.isBlank()
        ) {
            return
        }

        val rules = getIgnoredPatterns().toMutableList()

        val alreadyExists = rules.any {
            normalizeSender(it.sender) == normalizedSender &&
                normalizePattern(it.pattern) == normalizedPattern
        }

        if (!alreadyExists) {
            rules.add(
                IgnoredPattern(
                    sender = normalizedSender,
                    pattern = normalizedPattern,
                )
            )

            saveIgnoredPatterns(rules)
        }
    }

    /**
     * Returns true when the SMS matches an ignored pattern
     * belonging to the sender.
     */
    fun isPatternIgnored(
        sender: String?,
        messageBody: String,
    ): Boolean {
        if (sender == null) return false

        val normalizedSender = normalizeSender(sender)
        val normalizedBody = normalizePattern(messageBody)

        if (
            normalizedSender.isBlank() ||
            normalizedBody.isBlank()
        ) {
            return false
        }

        return getIgnoredPatterns().any { rule ->
            normalizeSender(rule.sender) == normalizedSender &&
                normalizedBody.contains(
                    normalizePattern(rule.pattern)
                )
        }
    }

    /**
     * Returns the matching ignored pattern, if one exists.
     */
    fun findIgnoredPattern(
        sender: String?,
        messageBody: String,
    ): IgnoredPattern? {
        if (sender == null) return null

        val normalizedSender = normalizeSender(sender)
        val normalizedBody = normalizePattern(messageBody)

        if (
            normalizedSender.isBlank() ||
            normalizedBody.isBlank()
        ) {
            return null
        }

        return getIgnoredPatterns()
            .filter {
                normalizeSender(it.sender) == normalizedSender
            }
            .filter {
                normalizedBody.contains(
                    normalizePattern(it.pattern)
                )
            }
            .maxByOrNull {
                normalizePattern(it.pattern).length
            }
    }

    /**
     * Removes one sender + pattern ignore rule.
     */
    fun unignorePattern(
        sender: String,
        pattern: String,
    ) {
        val normalizedSender = normalizeSender(sender)
        val normalizedPattern = normalizePattern(pattern)

        val rules = getIgnoredPatterns()
            .filterNot {
                normalizeSender(it.sender) == normalizedSender &&
                    normalizePattern(it.pattern) == normalizedPattern
            }

        saveIgnoredPatterns(rules)
    }

    /**
     * Returns all pattern-level ignore rules.
     */
    fun getIgnoredPatterns(): List<IgnoredPattern> {
        val json = prefs().getString(KEY_IGNORED_PATTERNS, null)
            ?: return emptyList()

        return try {
            val array = JSONArray(json)
            val result = mutableListOf<IgnoredPattern>()

            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index)
                    ?: continue

                val sender = normalizeSender(
                    item.optString("sender", "")
                )

                val pattern = normalizePattern(
                    item.optString("pattern", "")
                )

                if (
                    sender.isBlank() ||
                    pattern.isBlank()
                ) {
                    continue
                }

                result.add(
                    IgnoredPattern(
                        sender = sender,
                        pattern = pattern,
                    )
                )
            }

            result
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveIgnoredPatterns(
        rules: List<IgnoredPattern>,
    ) {
        val array = JSONArray()

        rules.forEach { rule ->
            val sender = normalizeSender(rule.sender)
            val pattern = normalizePattern(rule.pattern)

            if (
                sender.isBlank() ||
                pattern.isBlank()
            ) {
                return@forEach
            }

            val item = org.json.JSONObject()
            item.put("sender", sender)
            item.put("pattern", pattern)

            array.put(item)
        }

        prefs()
            .edit()
            .putString(KEY_IGNORED_PATTERNS, array.toString())
            .apply()
    }

    // ---------------------------------------------------------------------
    // Normalization
    // ---------------------------------------------------------------------

    /**
     * Keeps sender normalization consistent with SmsLearningStore
     * and BankSenderRegistry.
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
     * Normalizes Persian Arabic-character variants and whitespace.
     */
    private fun normalizePattern(value: String): String {
        return value
            .trim()
            .replace('\u064A', '\u06CC')
            .replace('\u0643', '\u06A9')
            .replace(Regex("\\s+"), " ")
    }

    data class IgnoredPattern(
        val sender: String,
        val pattern: String,
    )
}

