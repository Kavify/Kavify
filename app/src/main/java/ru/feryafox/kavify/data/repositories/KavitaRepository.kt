package ru.feryafox.kavify.data.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.feryafox.kavita4j.Kavita4J
import ru.feryafox.kavita4j.components.Kavita4JAuth

class KavitaRepository(
    private val client: Kavita4J
){
    var authCredentials: Kavita4JAuth.Kavita4JAuthCredentials
        get() = client.auth().credentials
        set(value) {
            client.auth().loadCredentials(value)
        }

    fun setBaseUrl(baseUrl: String) = client.setBaseUrl(baseUrl)

    suspend fun login(username: String, password: String) =  withContext(Dispatchers.IO) {client.auth().login(username, password) }

    suspend fun login(apiKey: String) = withContext(Dispatchers.IO) { client.auth().login(apiKey) }
}