package ru.feryafox.kavify.kavita.api

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.feryafox.kavify.kavify.storages.KavifyStorage
import ru.feryafox.kavify.kavify.storages.KavifyStorage.Companion.API_KEY_FIELD
import ru.feryafox.kavify.kavify.storages.KavifyStorage.Companion.IS_PASSWORD_OR_TOKEN_FIELD
import ru.feryafox.kavify.kavify.storages.KavifyStorage.Companion.KAVITA_4J_AUTH_CREDENTIALS_FIELD
import ru.feryafox.kavify.kavify.storages.KavifyStorage.Companion.PASSWORD_FIELD
import ru.feryafox.kavify.kavify.storages.KavifyStorage.Companion.URL_FIELD
import ru.feryafox.kavify.kavify.storages.KavifyStorage.Companion.USERNAME_FIELD
import ru.feryafox.kavify.kavify.storages.models.Kavita4JAuthCredentialsStorage
import ru.feryafox.kavita4j.Kavita4J
import ru.feryafox.kavita4j.exceptions.account.IncorrectCredentialsException
import ru.feryafox.yokailib.utils.ValidationRule
import java.io.IOException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Kavita4JManager @Inject constructor(
    private val kavifyStorage: KavifyStorage,
) : ValidationRule {
    private val TAG = "KavitaApiManager"
    private lateinit var _client: Kavita4J

    private val authValidation = AuthValidationRule()

    // Добавляем scope для корутин
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Флаг для отслеживания процесса реаутентификации
    @Volatile
    private var isReAuthInProgress = false

    val client: Kavita4J
        get() = _client

    override val message: String
        get() = authValidation.message

    override fun validate(): ru.feryafox.yokailib.utils.ValidationRuleResult {
        // Если идет процесс реаутентификации, возвращаем успешный результат
        // чтобы не показывать ошибки до завершения
        if (isReAuthInProgress) {
            return ru.feryafox.yokailib.utils.ValidationRuleResult(
                isValid = true,
                errorMessage = ""
            )
        }
        return authValidation.validate()
    }

    val validationState: AuthValidationRule
        get() = authValidation

    init {
        // Инициализируем только клиент без сетевых вызовов
        initClientWithoutNetwork()
        // Запускаем проверку аутентификации асинхронно
        scope.launch {
            initClientConfiguration()
        }
    }

    private fun initClientWithoutNetwork() {
        val url = URL_FIELD.field
        if (url.isEmpty()) {
            Log.w(TAG, "Base URL is empty")
            authValidation.setError(AuthErrorType.NO_ACCESS)
            return
        }
        _client = Kavita4J(url)
    }

    private suspend fun initClientConfiguration() = withContext(Dispatchers.IO) {
        try {
            val url = URL_FIELD.field

            if (url.isEmpty()) {
                Log.w(TAG, "Base URL is empty")
                authValidation.setError(AuthErrorType.NO_ACCESS)
                return@withContext
            }

            _client.auth().baseUrl = url

            val credentials = KAVITA_4J_AUTH_CREDENTIALS_FIELD.field

            if (!credentials.isEmpty()) {
                client.auth().loadCredentials(credentials.toCredentials())
                Log.d(TAG, "Loaded saved credentials")
            } else {
                reAuth().takeIf { it }?.let {
                    return@withContext
                }
            }

            runCatching { client.auth().verifyAuth() }
                .onSuccess {
                    authValidation.clearError()
                }
                .onFailure { exception ->
                    handleAuthException(exception)
                    Log.e(TAG, "Auth verification failed", exception)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating client configuration", e)
            handleAuthException(e)
        }
    }

    suspend fun reAuth(): Boolean = withContext(Dispatchers.IO) {
        isReAuthInProgress = true
        try {
            Log.d(TAG, "Re-authenticating with Kavita server...")
            val url = URL_FIELD.field
            val username = USERNAME_FIELD.field
            val password = PASSWORD_FIELD.field
            val apiKey = API_KEY_FIELD.field
            val isTokenAuth = IS_PASSWORD_OR_TOKEN_FIELD.field

            // Проверяем, что URL не пустой
            if (url.isEmpty()) {
                Log.e(TAG, "Cannot authenticate: URL is empty")
                authValidation.setError(AuthErrorType.NO_ACCESS)
                return@withContext false
            }

            try {
                client.auth().baseUrl = url

                if (isTokenAuth) {
                    if (apiKey.isEmpty()) {
                        Log.e(TAG, "Cannot authenticate: API Key is empty")
                        authValidation.setError(AuthErrorType.INVALID_API_KEY)
                        return@withContext false
                    }
                    try {
                        client.auth().login(apiKey)
                        Log.d(TAG, "Loaded API key credentials")
                    } catch (npe: NullPointerException) {
                        // Library bug: response.responseModel() может быть null даже при успешном ответе
                        Log.w(TAG, "NPE during login, but checking if credentials were actually set", npe)
                        // Проверяем, установились ли credentials несмотря на NPE
                        if (client.auth().accessToken.isNullOrEmpty()) {
                            Log.e(TAG, "Login failed: access token is null")
                            authValidation.setError(AuthErrorType.INVALID_API_KEY)
                            return@withContext false
                        }
                        Log.d(TAG, "Credentials were set despite NPE, continuing...")
                    }
                } else {
                    if (username.isEmpty() || password.isEmpty()) {
                        Log.e(TAG, "Cannot authenticate: Username or password is empty")
                        authValidation.setError(AuthErrorType.INVALID_CREDENTIALS)
                        return@withContext false
                    }
                    try {
                        client.auth().login(username, password)
                        Log.d(TAG, "Loaded username/password credentials")
                    } catch (npe: NullPointerException) {
                        // TODO разобраться с этим багом
                        // Library bug: response.responseModel() может быть null даже при успешном ответе
                        Log.w(TAG, "NPE during login, but checking if credentials were actually set", npe)
                        // Проверяем, установились ли credentials несмотря на NPE
                        if (client.auth().accessToken.isNullOrEmpty()) {
                            Log.e(TAG, "Login failed: access token is null")
                            authValidation.setError(AuthErrorType.INVALID_CREDENTIALS)
                            return@withContext false
                        }
                        Log.d(TAG, "Credentials were set despite NPE, continuing...")
                    }
                }

                saveCredentials()
                authValidation.clearError()
                Log.d(TAG, "Authentication successful")
                true
            } catch (e: NullPointerException) {
                // Обрабатываем NullPointerException, который может возникнуть внутри библиотеки Kavita4J
                Log.e(TAG, "Authentication failed: Library returned null", e)
                if (isTokenAuth) {
                    authValidation.setError(AuthErrorType.INVALID_API_KEY)
                } else {
                    authValidation.setError(AuthErrorType.INVALID_CREDENTIALS)
                }
                false
            } catch (e: IncorrectCredentialsException) {
                if (isTokenAuth) {
                    authValidation.setError(AuthErrorType.INVALID_API_KEY)
                } else {
                    authValidation.setError(AuthErrorType.INVALID_CREDENTIALS)
                }
                Log.e(TAG, "Authentication failed: ${authValidation.message}", e)
                false
            } catch (e: Exception) {
                handleAuthException(e)
                Log.e(TAG, "Authentication failed with exception", e)
                false
            }
        } finally {
            isReAuthInProgress = false
        }
    }

    /**
     * Выполняет реаутентификацию с таймаутом.
     * @param timeoutMillis Максимальное время ожидания в миллисекундах (по умолчанию 30 секунд)
     * @return true если аутентификация успешна, false если произошла ошибка или таймаут
     */
    suspend fun reAuthWithTimeout(timeoutMillis: Long = 30000): Boolean = withContext(Dispatchers.IO) {
        try {
            kotlinx.coroutines.withTimeout(timeoutMillis) {
                reAuth()
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Log.e(TAG, "Authentication timeout after ${timeoutMillis}ms")
            authValidation.setError(AuthErrorType.NETWORK_ERROR)
            false
        }
    }

    private fun saveCredentials() {
        KAVITA_4J_AUTH_CREDENTIALS_FIELD.field = Kavita4JAuthCredentialsStorage
            .fromCredentials(_client.auth().credentials)
        Log.d(TAG, "Saved authentication credentials")
    }

    private fun handleAuthException(exception: Throwable) {
        when (exception) {
            is IncorrectCredentialsException -> {
                authValidation.setError(AuthErrorType.INVALID_CREDENTIALS)
            }
            is UnknownHostException, is IOException -> {
                authValidation.setError(AuthErrorType.NETWORK_ERROR)
            }
            else -> {
                if (exception.message?.contains("access token", ignoreCase = true) == true) {
                    authValidation.setError(AuthErrorType.INVALID_ACCESS_TOKEN)
                } else if (exception.message?.contains("refresh token", ignoreCase = true) == true) {
                    authValidation.setError(AuthErrorType.INVALID_REFRESH_TOKEN)
                } else if (exception.message?.contains("api key", ignoreCase = true) == true) {
                    authValidation.setError(AuthErrorType.INVALID_API_KEY)
                } else {
                    authValidation.setError(AuthErrorType.UNKNOWN_ERROR)
                }
            }
        }
    }

    fun getDownloadDirectory(): String {
        return KavifyStorage.DOWNLOADING_DIR_FIELD.field
    }
}
