package ru.feryafox.kavify.domain.servicies

import ru.feryafox.kavify.data.models.Book
import ru.feryafox.kavify.data.models.Series
import ru.feryafox.kavify.data.models.Series.Companion.from
import ru.feryafox.kavify.data.models.from
import ru.feryafox.kavify.data.repositories.KavitaRepository
import ru.feryafox.kavify.data.repositories.PreferencesManager
import javax.inject.Inject

class Kavita4JService @Inject constructor(
    private val kavitaRepository: KavitaRepository,
    private val preferencesManager: PreferencesManager
) {
    fun setBaseUrl(
        baseUrl: String
    ) {
        kavitaRepository.baseUrl = baseUrl
        preferencesManager.baseUrl = baseUrl
    }

    suspend fun searchBook(
        query: String,
        includeChapterAndFiles: Boolean = false
    ): List<Series> {
        val response = kavitaRepository.search(query, includeChapterAndFiles)

        return if (response.isSuccess) {
            response.responseModel().series.from(
                kavitaRepository.baseUrl,
                kavitaRepository.apiKey
            )

        } else {
            emptyList()
        }
    }
}