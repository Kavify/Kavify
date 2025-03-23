package ru.feryafox.kavify.data.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.feryafox.kavita4j.Kavita4J

class KavitaRepository(
    private val client: Kavita4J
){
    fun setBaseUrl(baseUrl: String) = client.setBaseUrl(baseUrl)

    suspend fun login(username: String, password: String) = withContext(Dispatchers.IO) { client.auth().login(username, password) }

    fun login(apiToken: String) = client.auth().login(apiToken)

    fun getClient() = client
}