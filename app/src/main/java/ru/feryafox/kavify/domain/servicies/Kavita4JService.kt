package ru.feryafox.kavify.domain.servicies

import ru.feryafox.kavify.data.models.Book
import ru.feryafox.kavify.data.models.DownloadLink
import ru.feryafox.kavify.data.models.Series
import ru.feryafox.kavify.data.models.SeriesInfo
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

    suspend fun getSeriesDetail(
        book: Book
    ): SeriesInfo {
        val metadata = kavitaRepository.getSeriesMetadata(book.id)
        val responseDetail = kavitaRepository.getSeriesDetail(book.id)
        val seriesCoverLink = kavitaRepository.getSeriesCoverUrl(book.id)

        return SeriesInfo(
            book.title,
            metadata.responseModel().summary,
            seriesCoverLink.url,
            responseDetail.responseModel().volumes.map {
                DownloadLink(it.name, it.id)
            }
        )
    }
}