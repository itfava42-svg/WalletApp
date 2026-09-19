package com.example.walletapp.data

import android.content.Context
import java.util.UUID

object TransactionSyncIdentityStore {

    private const val PREFS_NAME =
        "wallet_app_sync_prefs"

    private const val KEY_DEVICE_ID =
        "device_id"

    private const val KEY_UPDATED_AFTER =
        "updated_after"

    private const val KEY_UPDATED_AFTER_ID =
        "updated_after_id"

    @Volatile
    private var cachedDeviceId: String? = null

    fun deviceId(
        context: Context
    ): String {

        cachedDeviceId?.let {
            return it
        }

        synchronized(this) {

            cachedDeviceId?.let {
                return it
            }

            val prefs =
                context.applicationContext
                    .getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                    )

            val existing =
                prefs.getString(
                    KEY_DEVICE_ID,
                    null
                )?.takeIf {
                    it.isNotBlank()
                }

            val id =
                existing
                    ?: UUID.randomUUID()
                        .toString()
                        .also {
                            prefs.edit()
                                .putString(
                                    KEY_DEVICE_ID,
                                    it
                                )
                                .apply()
                        }

            cachedDeviceId = id

            return id
        }
    }

    fun cursor(
        context: Context
    ): TransactionSyncCursor {

        val prefs =
            context.applicationContext
                .getSharedPreferences(
                    PREFS_NAME,
                    Context.MODE_PRIVATE
                )

        return TransactionSyncCursor(
            updatedAt =
                prefs.getLong(
                    KEY_UPDATED_AFTER,
                    0L
                ).coerceAtLeast(0L),
            id =
                prefs.getString(
                    KEY_UPDATED_AFTER_ID,
                    ""
                ) ?: ""
        )
    }

    fun updatedAfter(
        context: Context
    ): Long =
        cursor(context).updatedAt

    fun saveCursor(
        context: Context,
        value: TransactionSyncCursor
    ) {

        require(value.updatedAt >= 0L) {
            "Sync cursor must not be negative."
        }

        val current =
            cursor(context)

        if (
            compareCursor(
                value,
                current
            ) < 0
        ) {
            return
        }

        context.applicationContext
            .getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )
            .edit()
            .putLong(
                KEY_UPDATED_AFTER,
                value.updatedAt
            )
            .putString(
                KEY_UPDATED_AFTER_ID,
                value.id
            )
            .apply()
    }

    fun saveUpdatedAfter(
        context: Context,
        value: Long
    ) {
        saveCursor(
            context,
            TransactionSyncCursor(
                updatedAt = value,
                id = ""
            )
        )
    }

    fun clearCache() {
        cachedDeviceId = null
    }

    private fun compareCursor(
        a: TransactionSyncCursor,
        b: TransactionSyncCursor
    ): Int {

        val timeCompare =
            a.updatedAt.compareTo(
                b.updatedAt
            )

        if (timeCompare != 0) {
            return timeCompare
        }

        return a.id.compareTo(
            b.id
        )
    }
}
