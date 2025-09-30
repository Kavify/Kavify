// File: ru/feryafox/kavify/kavify/settings/KavifySettings.kt
package ru.feryafox.kavify.kavify.settings

import android.util.Log
import ru.feryafox.kavify.kavify.KAVIFY_ID
import ru.feryafox.kavify.kavify.storages.KavifyStorage.Companion.API_KEY_FIELD
import ru.feryafox.kavify.kavify.storages.KavifyStorage.Companion.DOWNLOADING_DIR_FIELD
import ru.feryafox.kavify.kavify.storages.KavifyStorage.Companion.IS_PASSWORD_OR_TOKEN_FIELD
import ru.feryafox.kavify.kavify.storages.KavifyStorage.Companion.PASSWORD_FIELD
import ru.feryafox.kavify.kavify.storages.KavifyStorage.Companion.URL_FIELD
import ru.feryafox.kavify.kavify.storages.KavifyStorage.Companion.USERNAME_FIELD

import ru.feryafox.yokailib.settings.base.CategorySettings
import ru.feryafox.yokailib.settings.base.OnUpdateBehavior
import ru.feryafox.yokailib.settings.bindables.BindableBooleanField
import ru.feryafox.yokailib.settings.bindables.bind
import ru.feryafox.yokailib.settings.defaults.*

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KavifySettings @Inject constructor() : CategorySettings(
    id = KAVIFY_ID,
    title = "Kavify",
    fields = listOf(
        URL_FIELD_SETTING,
        USERNAME_SETTING,
        PASSWORD_FIELD_SETTING,
        API_KEY_SETTING,
        IS_PASSWORD_OR_TOKEN_FIELD_SETTING,
        DOWNLOAD_DIR_FIELD_SETTING,
    )
) {
    companion object {
        val URL_FIELD_SETTING = UrlField(
            title = "URL сервера",
            required = true,
            field = URL_FIELD,
            isOnUpdateBehavior = OnUpdateBehavior.ON_CHANGED,
            validateUrl = true
        ) { newUrl ->
            Log.i("KavifySettings", "Server URL updated: $newUrl")
        }

        val USERNAME_SETTING = StringField(
            title = "Имя пользователя",
            required = true,
            field = USERNAME_FIELD,
            isOnUpdateBehavior = OnUpdateBehavior.ON_CHANGED
        ) { newValue ->
            Log.i("KavifySettings", "Username updated: $newValue")
        }

        val PASSWORD_FIELD_SETTING = PasswordField(
            title = "Пароль",
            required = true,
            field = PASSWORD_FIELD,
            isOnUpdateBehavior = OnUpdateBehavior.ON_CHANGED
        ) { newPass ->
            Log.i("KavifySettings", "Password updated: $newPass")
        }

        val API_KEY_SETTING = PasswordField(
            title = "API Key",
            required = true,
            field = API_KEY_FIELD,
            isOnUpdateBehavior = OnUpdateBehavior.ON_CHANGED
        ) { newPass ->
            Log.i("KavifySettings", "API Key updated: $newPass")
        }

        val IS_PASSWORD_OR_TOKEN_FIELD_SETTING = BindableBooleanField(
            title = "Токен или логин/пароль",
            field = IS_PASSWORD_OR_TOKEN_FIELD,
            isOnUpdateBehavior = OnUpdateBehavior.ON_CHANGED,
            immediatelyOnUpdate = {},
            bindScope = bind {
                USERNAME_SETTING disabledWhen true
                PASSWORD_FIELD_SETTING disabledWhen true
                API_KEY_SETTING enabledWhen  true
            },
            onUpdate = {}
        )

        val DOWNLOAD_DIR_FIELD_SETTING = DirectoryField(
            title = "Директория загрузки",
            field = DOWNLOADING_DIR_FIELD,
            isOnUpdateBehavior = OnUpdateBehavior.ON_CHANGED
        ) { newValue ->
            Log.i("KavifySettings", "Working directory updated: $newValue")
        }
    }
}
