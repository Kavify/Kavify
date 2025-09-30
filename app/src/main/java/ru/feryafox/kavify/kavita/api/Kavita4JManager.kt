package ru.feryafox.kavify.kavita.api

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import ru.feryafox.kavify.kavify.storages.KavifyStorage
import ru.feryafox.kavify.kavify.storages.KavifyStorage.Companion.API_KEY_FIELD
import ru.feryafox.kavify.kavify.storages.KavifyStorage.Companion.IS_PASSWORD_OR_TOKEN_FIELD
import ru.feryafox.kavify.kavify.storages.KavifyStorage.Companion.PASSWORD_FIELD
import ru.feryafox.kavify.kavify.storages.KavifyStorage.Companion.URL_FIELD
import ru.feryafox.kavify.kavify.storages.KavifyStorage.Companion.USERNAME_FIELD
import ru.feryafox.kavita4j.Kavita4J
import ru.feryafox.kavita4j.components.Kavita4JAuth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Kavita4JManager @Inject constructor(
    private val kavifyStorage: KavifyStorage,
) {
    private val TAG = "KavitaApiManager"
    private lateinit var _client: Kavita4J

    val client: Kavita4J
        get() = _client

    init {
        updateClientConfiguration()
    }

    private fun updateClientConfiguration() {
        try {
            val url = URL_FIELD.field
            val username = USERNAME_FIELD.field
            val password = PASSWORD_FIELD.field
            val apiKey = API_KEY_FIELD.field
            val isTokenAuth = IS_PASSWORD_OR_TOKEN_FIELD.field

            if (url.isEmpty()) {
                Log.w(TAG, "Base URL is empty")
                return
            }
            if (this::_client.isInitialized) {
                _client = Kavita4J(url)
            } else {
                _client.auth().baseUrl = url
            }

            if (isTokenAuth && apiKey.isNotEmpty()) {
                // Используем API токен
                client.auth().loadCredentials(
                    Kavita4JAuth.Kavita4JAuthCredentials(
                        apiKey = apiKey

                    )
                )
                Log.d(TAG, "Updated API key authentication")
            } else if (!isTokenAuth && username.isNotEmpty() && password.isNotEmpty()) {
                // Используем логин/пароль - здесь нужно будет выполнить login
                Log.d(TAG, "Username/password authentication configured")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating client configuration", e)
        }
    }

    /**
     * Отслеживает изменения в настройках и обновляет клиент
     */
    private fun observeConfigurationChanges() {
        // Комбинируем все потоки настроек
        val configurationFlow: Flow<Unit> = combine(
            URL_FIELD.flow,
            USERNAME_FIELD.flow,
            PASSWORD_FIELD.flow,
            API_KEY_FIELD.flow,
            IS_PASSWORD_OR_TOKEN_FIELD.flow
        ) { _, _, _, _, _ ->
            // Когда любая настройка изменилась, обновляем конфигурацию
            updateClientConfiguration()
        }
    }

    /**
     * Проверяет, готов ли клиент к использованию
     */
    fun isConfigured(): Boolean {
        val url = URL_FIELD.getValue()
        val isTokenAuth = IS_PASSWORD_OR_TOKEN_FIELD.getValue()
        val apiKey = API_KEY_FIELD.getValue()
        val username = USERNAME_FIELD.getValue()
        val password = PASSWORD_FIELD.getValue()

        return url.isNotEmpty() && (
            (isTokenAuth && apiKey.isNotEmpty()) ||
            (!isTokenAuth && username.isNotEmpty() && password.isNotEmpty())
        )
    }

    /**
     * Выполняет аутентификацию на основе текущих настроек
     */
    suspend fun authenticate(): Boolean {
        return try {
            val isTokenAuth = IS_PASSWORD_OR_TOKEN_FIELD.getValue()

            if (isTokenAuth) {
                val apiKey = API_KEY_FIELD.getValue()
                if (apiKey.isNotEmpty()) {
                    val response = client.auth().login(apiKey)
                    val success = response.isSuccess
                    Log.d(TAG, "API key authentication: ${if (success) "success" else "failed"}")
                    success
                } else {
                    Log.w(TAG, "API key is empty")
                    false
                }
            } else {
                val username = USERNAME_FIELD.getValue()
                val password = PASSWORD_FIELD.getValue()

                if (username.isNotEmpty() && password.isNotEmpty()) {
                    val response = client.auth().login(username, password)
                    val success = response.isSuccess
                    Log.d(TAG, "Username/password authentication: ${if (success) "success" else "failed"}")
                    success
                } else {
                    Log.w(TAG, "Username or password is empty")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Authentication failed", e)
            false
        }
    }

    /**
     * Возвращает текущую директорию для загрузок
     */
    fun getDownloadDirectory(): String {
        return kavifyStorage.DOWNLOADING_DIR_FIELD.getValue()
    }
}
