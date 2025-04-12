package ru.feryafox.kavify.startup

import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import ru.feryafox.kavify.data.repositories.KavitaRepository
import ru.feryafox.kavify.data.repositories.PreferencesManager
import ru.feryafox.kavify.presentation.ui.Routes
import ru.feryafox.kavita4j.components.Kavita4JAuth.Kavita4JAuthCredentials
import ru.feryafox.kavita4j.exceptions.account.InvalidTokenException

@Singleton
class StartupInitializer @Inject constructor(
    private val repository: KavitaRepository,
    private val preferences: PreferencesManager
) {
    fun initialize(navController: NavHostController) {
        repository.baseUrl = preferences.baseUrl

        repository.authCredentials = preferences.getAuthCredentials()

        if (preferences.isAuthed) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    println(repository.getAccountInfo())
                } catch (e: InvalidTokenException) {
                    val creds = preferences.getAuthCredentials()
                    val loginSuccess = try {
                        if (creds.apiKey?.isNotBlank() == true) {
                            repository.login(apiKey = creds.apiKey).isSuccess
                        } else {
                            repository.login(username = creds.username ?: "", password = creds.password ?: "").isSuccess
                        }
                    } catch (e: Exception) {
                        println("Foo: ")
                        println(e)
                        false
                    }

                    println(loginSuccess)
                    println(repository.getAccountInfo())
                    if (!loginSuccess) {
                        preferences.deleteAuthCredentials()
                        navController.navigate("${Routes.LOGIN.path}?reason=invalid_token") {
                            popUpTo(0)
                        }
                    }
                }
            }
        }
    }
}
