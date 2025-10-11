package ru.feryafox.kavify.kavita.api

import ru.feryafox.yokailib.utils.ValidationRule
import ru.feryafox.yokailib.utils.ValidationRuleResult

/**
 * Типы ошибок аутентификации
 */
enum class AuthErrorType {
    NO_ERROR,
    NO_ACCESS,
    INVALID_ACCESS_TOKEN,
    INVALID_REFRESH_TOKEN,
    INVALID_API_KEY,
    INVALID_CREDENTIALS,
    NETWORK_ERROR,
    UNKNOWN_ERROR
}

/**
 * ValidationRule с поддержкой разных типов ошибок
 */
class AuthValidationRule : ValidationRule {
    private var currentErrorType: AuthErrorType = AuthErrorType.NO_ERROR

    override val message: String
        get() = when (currentErrorType) {
            AuthErrorType.NO_ERROR -> ""
            AuthErrorType.NO_ACCESS -> "Нет доступа к серверу. Проверьте URL и подключение к интернету"
            AuthErrorType.INVALID_ACCESS_TOKEN -> "Недействительный access token. Необходимо повторно войти"
            AuthErrorType.INVALID_REFRESH_TOKEN -> "Недействительный refresh token. Необходимо повторно войти"
            AuthErrorType.INVALID_API_KEY -> "Неверный API ключ. Проверьте правильность ключа"
            AuthErrorType.INVALID_CREDENTIALS -> "Неверное имя пользователя или пароль"
            AuthErrorType.NETWORK_ERROR -> "Ошибка сети. Проверьте подключение к интернету"
            AuthErrorType.UNKNOWN_ERROR -> "Неизвестная ошибка. Попробуйте позже"
        }

    override fun validate(): ValidationRuleResult {
        return ValidationRuleResult(
            isValid = currentErrorType == AuthErrorType.NO_ERROR,
            errorMessage = message,
        )
    }

    /**
     * Устанавливает текущий тип ошибки
     */
    fun setError(errorType: AuthErrorType) {
        currentErrorType = errorType
    }

    /**
     * Очищает ошибку
     */
    fun clearError() {
        currentErrorType = AuthErrorType.NO_ERROR
    }

    /**
     * Проверяет, есть ли ошибка
     */
    fun hasError(): Boolean {
        return currentErrorType != AuthErrorType.NO_ERROR
    }

    /**
     * Возвращает текущий тип ошибки
     */
    fun getCurrentErrorType(): AuthErrorType {
        return currentErrorType
    }
}
