package ru.feryafox.kavify.data.repositories

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.feryafox.kavita4j.Kavita4J
import ru.feryafox.kavita4j.components.Kavita4JAuth
import ru.feryafox.kavita4j.http.HttpClientResponse
import ru.feryafox.kavita4j.models.responses.account.User
import ru.feryafox.kavita4j.models.responses.search.SearchResultGroup

class KavitaRepository(
    private val client: Kavita4J
){
    var authCredentials: Kavita4JAuth.Kavita4JAuthCredentials
        get() = client.auth().credentials
        set(value) {
            client.auth().loadCredentials(value)
        }

    val apiKey: String
        get() = client.auth().credentials.apiKey

    var baseUrl: String
        get() = client.
        set(value) {
            client.setBaseUrl(value)
        }

    fun setBaseUrl(baseUrl: String) = client.setBaseUrl(baseUrl)

    suspend fun login(username: String, password: String): HttpClientResponse<User> =  withContext(Dispatchers.IO) {client.auth().login(username, password) }

    suspend fun login(apiKey: String): HttpClientResponse<User> = withContext(Dispatchers.IO) { client.auth().login(apiKey) }

    suspend fun search(query: String, includeChapterAndFiles: Boolean = false): HttpClientResponse<SearchResultGroup> = withContext(Dispatchers.IO) { client.search().search(query, includeChapterAndFiles) }
}