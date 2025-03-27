package ru.feryafox.kavify.domain.servicies

import ru.feryafox.kavify.data.models.Book
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
        kavitaRepository.setBaseUrl(baseUrl)
        preferencesManager.baseUrl = baseUrl
    }

    fun searchBook(
        query: String
    ): List<Book> {
        //  STOP HERE
    }
}