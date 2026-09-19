package com.example.walletapp.data

/**
 * Builds authentication headers for Sync HTTP requests.
 *
 * The token is supplied at runtime.
 * No secret is stored in source code.
 */
object TransactionSyncAuthHeaders {

    const val AUTHORIZATION =
        "Authorization"

    const val BEARER_PREFIX =
        "Bearer "

    fun authorizationValue(
        accessToken: String
    ): String {

        require(
            accessToken.isNotBlank()
        ) {
            "Access token must not be blank."
        }

        return BEARER_PREFIX +
            accessToken.trim()
    }

    fun build(
        accessToken: String?
    ): Map<String, String> {

        if (
            accessToken.isNullOrBlank()
        ) {
            return emptyMap()
        }

        return mapOf(
            AUTHORIZATION to
                authorizationValue(
                    accessToken
                )
        )
    }
}
