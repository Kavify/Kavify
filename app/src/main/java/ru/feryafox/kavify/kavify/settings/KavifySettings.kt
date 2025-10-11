// File: ru/feryafox/kavify/kavify/settings/KavifySettings.kt
package ru.feryafox.kavify.kavify.settings

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.feryafox.kavify.kavify.KAVIFY_ID
import ru.feryafox.kavify.kavify.storages.KavifyStorage.Companion.API_KEY_FIELD
import ru.feryafox.kavify.kavify.storages.KavifyStorage.Companion.DOWNLOADING_DIR_FIELD
import ru.feryafox.kavify.kavify.storages.KavifyStorage.Companion.IS_PASSWORD_OR_TOKEN_FIELD
import ru.feryafox.kavify.kavify.storages.KavifyStorage.Companion.PASSWORD_FIELD
import ru.feryafox.kavify.kavify.storages.KavifyStorage.Companion.URL_FIELD
import ru.feryafox.kavify.kavify.storages.KavifyStorage.Companion.USERNAME_FIELD
import ru.feryafox.kavify.kavita.api.Kavita4JManager
import ru.feryafox.yokailib.settings.base.CategorySettings
import ru.feryafox.yokailib.settings.base.OnUpdateBehavior
import ru.feryafox.yokailib.settings.base.SettingField
import ru.feryafox.yokailib.settings.bindables.BindableBooleanField
import ru.feryafox.yokailib.settings.bindables.bind
import ru.feryafox.yokailib.settings.defaults.*
import ru.feryafox.yokailib.utils.AsyncValidatable
import ru.feryafox.yokailib.utils.ValidationResult
import ru.feryafox.yokailib.utils.ValidationRule

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KavifySettings @Inject constructor(
    private val kavita4JManager: Kavita4JManager,
) : CategorySettings(
    id = KAVIFY_ID,
    title = "Kavify"
), AsyncValidatable {

    val urlFieldSetting: UrlField = UrlField(
        title = "URL сервера",
        required = true,
        field = URL_FIELD,
        isOnUpdateBehavior = OnUpdateBehavior.ON_SAVED,
        validateUrl = true
    ) { newUrl ->
        Log.i("KavifySettings", "Server URL updated: $newUrl")
    }
    val usernameSetting: StringField = StringField(
        title = "Имя пользователя",
        required = true,
        field = USERNAME_FIELD,
        isOnUpdateBehavior = OnUpdateBehavior.ON_SAVED
    ) { newValue ->
        Log.i("KavifySettings", "Username updated: $newValue")
    }
    val passwordFieldSetting: PasswordField = PasswordField(
        title = "Пароль",
        required = true,
        field = PASSWORD_FIELD,
        isOnUpdateBehavior = OnUpdateBehavior.ON_SAVED
    ) { newPass ->
        Log.i("KavifySettings", "Password updated: $newPass")
    }
    val apiKeySetting: PasswordField = PasswordField(
        title = "API Key",
        required = true,
        field = API_KEY_FIELD,
        isOnUpdateBehavior = OnUpdateBehavior.ON_SAVED
    ) { newPass ->
        Log.i("KavifySettings", "API Key updated: $newPass")
    }
    val isPasswordOrTokenFieldSetting: BindableBooleanField = BindableBooleanField(
        title = "Токен или логин/пароль",
        field = IS_PASSWORD_OR_TOKEN_FIELD,
        isOnUpdateBehavior = OnUpdateBehavior.ON_CHANGED,
        immediatelyOnUpdate = {},
        bindScope = bind {
            usernameSetting disabledWhen true
            passwordFieldSetting disabledWhen true
            apiKeySetting enabledWhen  true
        },
        onUpdate = {}
    )
    val downloadDirFieldSetting: DirectoryField = DirectoryField(
        title = "Директория загрузки",
        field = DOWNLOADING_DIR_FIELD,
        isOnUpdateBehavior = OnUpdateBehavior.ON_CHANGED
    ) { newValue ->
        Log.i("KavifySettings", "Working directory updated: $newValue")
    }

    override val fields: List<SettingField<*>> = listOf(
        urlFieldSetting,
        usernameSetting,
        passwordFieldSetting,
        apiKeySetting,
        isPasswordOrTokenFieldSetting,
        downloadDirFieldSetting,
    )

    /**
     * Правила валидации для обычной синхронной проверки.
     * Включает kavita4JManager для проверки аутентификации.
     */
    override val validationRules: List<ValidationRule> = listOf(
        kavita4JManager
    )

    /**
     * Асинхронная валидация настроек.
     * Выполняет сохранение настроек и реаутентификацию с таймаутом 30 секунд.
     */
    override suspend fun validateAsync(): ValidationResult = withContext(Dispatchers.IO) {
        Log.d("KavifySettings", "Starting async validation with reAuth")

        // Сохраняем все поля
        save()

        // Выполняем реаутентификацию с таймаутом 30 секунд
        val success = kavita4JManager.reAuthWithTimeout(30000)

        if (success) {
            Log.i("KavifySettings", "Async validation successful")
            ValidationResult.Success
        } else {
            // Получаем результат валидации из kavita4JManager
            val validationResult = kavita4JManager.validate()
            val errorMessage = validationResult.errorMessage?.ifEmpty {
                "Ошибка аутентификации"
            } ?: "Ошибка аутентификации"

            Log.e("KavifySettings", "Async validation failed: $errorMessage")

            // Возвращаем Failure с информацией об ошибке
            ValidationResult.Failure(
                listOf(
                    ru.feryafox.yokailib.utils.ValidationFailure(
                        message = errorMessage
                    )
                )
            )
        }
    }
}
