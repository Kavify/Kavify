package ru.feryafox.kavify.data.repositories

import android.content.SharedPreferences
import ru.feryafox.kavita4j.components.Kavita4JAuth.Kavita4JAuthCredentials
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
    private val prefs: SharedPreferences
){
    var isAuthed: Boolean
        get() = prefs.getBoolean("is_authed", false)
        set(value) = prefs.edit()
            .putBoolean("is_authed", value)
            .apply()

    fun saveAuthCredentials(
        kavita4JAuthCredentials: Kavita4JAuthCredentials
    ) = prefs.edit()
        .putString("username", kavita4JAuthCredentials.username)
        .putString("password", kavita4JAuthCredentials.password)
        .putString("api_key", kavita4JAuthCredentials.apiKey)
        .putString("access_token", kavita4JAuthCredentials.accessToken)
        .putString("refresh_token", kavita4JAuthCredentials.refreshToken)
        .apply()

    fun saveBaseUrl(baseUrl: String) = prefs.edit()
        .putString("base_url", baseUrl)
        .apply()
}