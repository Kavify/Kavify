package ru.feryafox.kavify.domain.servicies

import ru.feryafox.kavify.data.repositories.KavitaRepository
import ru.feryafox.kavify.data.repositories.PreferencesManager
import javax.inject.Inject

class AuthService @Inject constructor(
    private val kavitaRepository: KavitaRepository,
    private val preferencesManager: PreferencesManager
){
    suspend fun login(
        username: String,
        password: String,
        apikey: String,
        useApiKey: Boolean
    ): Boolean {
        val isSuccess: Boolean = if (useApiKey) {
            kavitaRepository.login(apiKey = apikey).isSuccess
        } else {
            kavitaRepository.login(username = username, password = password).isSuccess
        }

        preferencesManager.saveAuthCredentials(kavitaRepository.authCredentials)
        preferencesManager.isAuthed = true

        return isSuccess
    }
}