package ru.feryafox.kavify.data.repositories

import android.content.SharedPreferences
import ru.feryafox.kavita4j.components.Kavita4JAuth.Kavita4JAuthCredentials
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

const val IS_AUTHED_KEY = "is_authed"
const val BASE_URL_KEY = "base_url"
const val USERNAME_KEY = "username"
const val PASSWORD_KEY = "password"
const val API_KEY_KEY = "api_key"
const val ACCESS_TOKEN_KEY = "access_token"
const val REFRESH_TOKEN_KEY = "refresh_token"

@Singleton
class PreferencesManager @Inject constructor(
    private val prefs: SharedPreferences
){

    var isAuthed: Boolean
        get() = prefs.getBoolean(IS_AUTHED_KEY, false)
        set(value) = prefs.edit() {
            putBoolean(IS_AUTHED_KEY, value)
        }

    var baseUrl: String
        get() = prefs.getString(BASE_URL_KEY, "") ?: ""
        set(value) = prefs.edit() {
            putString(BASE_URL_KEY, value)
        }

    fun saveAuthCredentials(
        kavita4JAuthCredentials: Kavita4JAuthCredentials
    ) = prefs.edit() {
        putString(USERNAME_KEY, kavita4JAuthCredentials.username)
            .putString(PASSWORD_KEY, kavita4JAuthCredentials.password)
            .putString(API_KEY_KEY, kavita4JAuthCredentials.apiKey)
            .putString(ACCESS_TOKEN_KEY, kavita4JAuthCredentials.accessToken)
            .putString(REFRESH_TOKEN_KEY, kavita4JAuthCredentials.refreshToken)
    }

    fun deleteAuthCredentials() = prefs.edit() {
        putString(USERNAME_KEY, "")
            .putString(PASSWORD_KEY, "")
            .putString(API_KEY_KEY, "")
            .putString(ACCESS_TOKEN_KEY, "")
            .putString(REFRESH_TOKEN_KEY, "")
            .putBoolean(IS_AUTHED_KEY, false)
    }
}