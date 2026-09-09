package com.example.walletapp.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import org.json.JSONArray
import org.json.JSONObject

enum class ReminderKind {
    RECEIVE,
    PAY
}

enum class RepeatFrequency(val label: String) {
    ONCE("یک بار"),
    DAILY("هر روز"),
    WEEKLY("هر هفته"),
    MONTHLY("هر ماه")
}

enum class ReminderPriority(val label: String) {
    LOW("کم"),
    MEDIUM("متوسط"),
    HIGH("زیاد"),
    URGENT("فوری")
}

enum class ReminderCategory(val label: String) {
    BILL("قبض"),
    INSTALLMENT("قسط"),
    DEBT("بدهی"),
    SALARY("حقوق"),
    PURCHASE("خرید"),
    OTHER("سایر")
}

data class Reminder(
    val id: String,
    val title: String,
    val amount: Long,
    val kind: ReminderKind,
    val description: String,
    val repeat: RepeatFrequency,
    val triggerAtMillis: Long,
    val isActive: Boolean = true,
    val priority: ReminderPriority = ReminderPriority.MEDIUM,
    val category: ReminderCategory = ReminderCategory.OTHER,
    val isCompleted: Boolean = false
)

object ReminderStore {

    val reminders = mutableStateListOf<Reminder>()

    private var context: Context? = null
    private var loaded = false

    private const val PREFS = "wallet_app_prefs"
    private const val KEY = "reminders_json"

    fun attachContext(ctx: Context) {
        if (context == null) {
            context = ctx.applicationContext
        }

        if (!loaded) {
            loaded = true
            load()
        }
    }

    private fun load() {
        val ctx = context ?: return

        val json = ctx.getSharedPreferences(
            PREFS,
            Context.MODE_PRIVATE
        ).getString(KEY, null) ?: return

        runCatching {
            val array = JSONArray(json)

            reminders.clear()

            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)

                reminders.add(
                    Reminder(
                        id = item.getString("id"),
                        title = item.getString("title"),
                        amount = item.optLong("amount", 0L),
                        kind = runCatching {
                            ReminderKind.valueOf(
                                item.optString("kind", ReminderKind.PAY.name)
                            )
                        }.getOrDefault(ReminderKind.PAY),
                        description = item.optString("description", ""),
                        repeat = runCatching {
                            RepeatFrequency.valueOf(
                                item.optString("repeat", RepeatFrequency.ONCE.name)
                            )
                        }.getOrDefault(RepeatFrequency.ONCE),
                        triggerAtMillis = item.optLong(
                            "triggerAtMillis",
                            System.currentTimeMillis()
                        ),
                        isActive = item.optBoolean("isActive", true),
                        priority = runCatching {
                            ReminderPriority.valueOf(
                                item.optString(
                                    "priority",
                                    ReminderPriority.MEDIUM.name
                                )
                            )
                        }.getOrDefault(ReminderPriority.MEDIUM),
                        category = runCatching {
                            ReminderCategory.valueOf(
                                item.optString(
                                    "category",
                                    ReminderCategory.OTHER.name
                                )
                            )
                        }.getOrDefault(ReminderCategory.OTHER),
                        isCompleted = item.optBoolean(
                            "isCompleted",
                            false
                        )
                    )
                )
            }
        }
    }

    private fun save() {
        val ctx = context ?: return

        val array = JSONArray()

        reminders.forEach {
            val item = JSONObject()

            item.put("id", it.id)
            item.put("title", it.title)
            item.put("amount", it.amount)
            item.put("kind", it.kind.name)
            item.put("description", it.description)
            item.put("repeat", it.repeat.name)
            item.put("triggerAtMillis", it.triggerAtMillis)
            item.put("isActive", it.isActive)
            item.put("priority", it.priority.name)
            item.put("category", it.category.name)
            item.put("isCompleted", it.isCompleted)

            array.put(item)
        }

        ctx.getSharedPreferences(
            PREFS,
            Context.MODE_PRIVATE
        )
            .edit()
            .putString(KEY, array.toString())
            .apply()
    }

    fun add(reminder: Reminder) {
        reminders.add(0, reminder)
        save()
    }

    fun update(reminder: Reminder) {
        val index = reminders.indexOfFirst {
            it.id == reminder.id
        }

        if (index >= 0) {
            reminders[index] = reminder
            save()
        }
    }

    fun remove(id: String) {
        reminders.removeAll {
            it.id == id
        }
        save()
    }

    fun getById(id: String): Reminder? {
        return reminders.firstOrNull {
            it.id == id
        }
    }

    fun setCompleted(
        id: String,
        completed: Boolean
    ) {
        getById(id)?.let {
            update(
                it.copy(
                    isCompleted = completed,
                    isActive = if (completed) false else it.isActive
                )
            )
        }
    }

    fun weekTotal(
        kind: ReminderKind
    ): Long {
        return reminders
            .filter {
                it.isActive &&
                    !it.isCompleted &&
                    it.kind == kind
            }
            .sumOf {
                it.amount
            }
    }

    fun monthTotal(
        kind: ReminderKind
    ): Long {
        return reminders
            .filter {
                it.isActive &&
                    !it.isCompleted &&
                    it.kind == kind
            }
            .sumOf {
                it.amount
            }
    }
}
