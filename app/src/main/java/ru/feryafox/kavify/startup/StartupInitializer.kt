package ru.feryafox.kavify.startup

import javax.inject.Inject
import javax.inject.Singleton
import ru.feryafox.kavify.data.repositories.KavitaRepository
import ru.feryafox.kavify.data.repositories.PreferencesManager
import ru.feryafox.kavita4j.components.Kavita4JAuth.Kavita4JAuthCredentials

@Singleton
class StartupInitializer @Inject constructor(
    private val repository: KavitaRepository,
    private val preferences: PreferencesManager
) {
    fun initialize() {
        repository.baseUrl = preferences.baseUrl

        repository.authCredentials = preferences.getAuthCredentials()
    }
}
