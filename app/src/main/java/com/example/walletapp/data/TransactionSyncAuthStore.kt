package com.example.walletapp.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Secure local storage for the Sync access token.
 *
 * The encryption key is generated and protected by Android Keystore.
 *
 * No token is hardcoded into the APK.
 * No token is written to Logcat.
 * No network operation is performed here.
 */
object TransactionSyncAuthStore {

    private const val KEYSTORE_PROVIDER =
        "AndroidKeyStore"

    private const val KEY_ALIAS =
        "wallet_app_sync_auth_key"

    private const val PREFS_NAME =
        "wallet_app_sync_auth"

    private const val KEY_ACCESS_TOKEN =
        "encrypted_access_token"

    private const val TRANSFORMATION =
        "AES/GCM/NoPadding"

    private const val GCM_TAG_LENGTH_BITS =
        128

    private const val IV_LENGTH_BYTES =
        12

    private fun prefs(
        context: Context
    ) =
        context.applicationContext
            .getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )

    private fun getOrCreateKey(): SecretKey {

        val keyStore =
            KeyStore.getInstance(
                KEYSTORE_PROVIDER
            ).apply {
                load(null)
            }

        val existing =
            keyStore.getKey(
                KEY_ALIAS,
                null
            )

        if (existing is SecretKey) {
            return existing
        }

        val generator =
            KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                KEYSTORE_PROVIDER
            )

        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or
                    KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(
                    KeyProperties.BLOCK_MODE_GCM
                )
                .setEncryptionPaddings(
                    KeyProperties.ENCRYPTION_PADDING_NONE
                )
                .build()
        )

        return generator.generateKey()
    }

    fun saveAccessToken(
        context: Context,
        accessToken: String
    ) {

        require(
            accessToken.isNotBlank()
        ) {
            "Access token must not be blank."
        }

        val token =
            accessToken.trim()

        val cipher =
            Cipher.getInstance(
                TRANSFORMATION
            )

        cipher.init(
            Cipher.ENCRYPT_MODE,
            getOrCreateKey()
        )

        val encrypted =
            cipher.doFinal(
                token.toByteArray(
                    StandardCharsets.UTF_8
                )
            )

        val iv =
            cipher.iv

        require(
            iv.size == IV_LENGTH_BYTES
        ) {
            "Unexpected AES-GCM IV length."
        }

        val storedValue =
            Base64.encodeToString(
                iv,
                Base64.NO_WRAP
            ) +
                "." +
                Base64.encodeToString(
                    encrypted,
                    Base64.NO_WRAP
                )

        prefs(context)
            .edit()
            .putString(
                KEY_ACCESS_TOKEN,
                storedValue
            )
            .apply()
    }

    fun getAccessToken(
        context: Context
    ): String? {

        val stored =
            prefs(context)
                .getString(
                    KEY_ACCESS_TOKEN,
                    null
                )
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: return null

        val parts =
            stored.split(
                ".",
                limit = 2
            )

        if (parts.size != 2) {
            clearAccessToken(context)
            return null
        }

        return try {

            val iv =
                Base64.decode(
                    parts[0],
                    Base64.NO_WRAP
                )

            val encrypted =
                Base64.decode(
                    parts[1],
                    Base64.NO_WRAP
                )

            if (
                iv.size !=
                    IV_LENGTH_BYTES
            ) {
                clearAccessToken(context)
                return null
            }

            val cipher =
                Cipher.getInstance(
                    TRANSFORMATION
                )

            cipher.init(
                Cipher.DECRYPT_MODE,
                getOrCreateKey(),
                GCMParameterSpec(
                    GCM_TAG_LENGTH_BITS,
                    iv
                )
            )

            String(
                cipher.doFinal(
                    encrypted
                ),
                StandardCharsets.UTF_8
            )
                .takeIf {
                    it.isNotBlank()
                }

        } catch (_: Throwable) {

            clearAccessToken(context)
            null
        }
    }

    fun hasAccessToken(
        context: Context
    ): Boolean =
        !getAccessToken(context)
            .isNullOrBlank()

    fun clearAccessToken(
        context: Context
    ) {

        prefs(context)
            .edit()
            .remove(
                KEY_ACCESS_TOKEN
            )
            .apply()
    }
}
