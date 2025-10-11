package ru.feryafox.kavify.kavify.storages.models

import kotlinx.serialization.Serializable
import ru.feryafox.kavita4j.components.Kavita4JAuth

@Serializable
data class Kavita4JAuthCredentialsStorage(
    val username: String = "",
    val password: String = "",
    val apiKey: String = "",
    val accessToken: String = "",
    val refreshToken: String = "",
) {
    fun toCredentials(): Kavita4JAuth.Kavita4JAuthCredentials {
        return Kavita4JAuth.Kavita4JAuthCredentials(
            username, password, apiKey, accessToken, refreshToken
        )
    }

    fun isEmpty(): Boolean {
        return username.isEmpty() &&
                password.isEmpty() &&
                apiKey.isEmpty() &&
                accessToken.isEmpty() &&
                refreshToken.isEmpty()
    }

    companion object {
        fun fromCredentials(credentials: Kavita4JAuth.Kavita4JAuthCredentials): Kavita4JAuthCredentialsStorage {
            return Kavita4JAuthCredentialsStorage(
                credentials.username,
                credentials.password,
                credentials.apiKey,
                credentials.accessToken,
                credentials.refreshToken
            )
        }
    }
}
