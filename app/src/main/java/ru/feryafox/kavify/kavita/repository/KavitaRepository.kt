package ru.feryafox.kavify.kavita.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.feryafox.kavita4j.Kavita4J
import ru.feryafox.kavita4j.components.Kavita4JAuth
import ru.feryafox.kavita4j.http.HttpClientResponse
import ru.feryafox.kavita4j.models.responses.BinaryResponse
import ru.feryafox.kavita4j.models.responses.UrlResponse
import ru.feryafox.kavita4j.models.responses.account.User
import ru.feryafox.kavita4j.models.responses.search.SearchResultGroup
import ru.feryafox.kavita4j.models.responses.series.SeriesDetail
import ru.feryafox.kavita4j.models.responses.series.Series
import ru.feryafox.kavita4j.models.responses.series.RecentlyAddedItems
import ru.feryafox.kavita4j.models.responses.series.SeriesList
import ru.feryafox.kavita4j.models.responses.series.SeriesDtoList
import ru.feryafox.kavita4j.models.requests.series.FilterV2

class KavitaRepository(
    val client: Kavita4J
){
    var authCredentials: Kavita4JAuth.Kavita4JAuthCredentials
        get() = client.auth().credentials
        set(value) {
            client.auth().loadCredentials(value)
        }

    val apiKey: String
        get() = client.auth().credentials.apiKey

    var baseUrl: String
        get() = client.auth().baseUrl
        set(value) {
            client.auth().baseUrl = value
        }

    suspend fun login(username: String, password: String): HttpClientResponse<User> = withContext(Dispatchers.IO) {client.auth().login(username, password) }

    suspend fun login(apiKey: String): HttpClientResponse<User> = withContext(Dispatchers.IO) { client.auth().login(apiKey) }

    suspend fun search(query: String, includeChapterAndFiles: Boolean = false): HttpClientResponse<SearchResultGroup> = withContext(Dispatchers.IO) { client.search().search(query, includeChapterAndFiles) }

    suspend fun getSeries(seriesId: Int): HttpClientResponse<Series> = withContext(Dispatchers.IO) { client.series().getSeries(seriesId) }

    suspend fun getSeriesMetadata(seriesId: Int) = withContext(Dispatchers.IO) { client.series().getMetadata(seriesId) }

    suspend fun getSeriesDetail(seriesId: Int): HttpClientResponse<SeriesDetail> = withContext(Dispatchers.IO) { client.series().getSeriesDetail(seriesId) }

    suspend fun getSeriesCoverUrl(seriesId: Int): String = withContext(Dispatchers.IO) { client.image().seriesCoverLink(seriesId).url }

    suspend fun getVolumeCoverUrl(volumeId: Int): String = withContext(Dispatchers.IO) { client.image().volumeCoverLink(volumeId).url }

    suspend fun getChapterCoverUrl(chapterId: Int): String = withContext(Dispatchers.IO) { client.image().chapterCoverLink(chapterId).url }

    // Binary image methods
    suspend fun getSeriesCover(seriesId: Int): BinaryResponse = withContext(Dispatchers.IO) { client.image().seriesCover(seriesId) }

    suspend fun getVolumeCover(volumeId: Int): BinaryResponse = withContext(Dispatchers.IO) { client.image().volumeCover(volumeId) }

    suspend fun getChapterCover(chapterId: Int): BinaryResponse = withContext(Dispatchers.IO) { client.image().chapterCover(chapterId) }

    suspend fun getLibraryCover(libraryId: Int): BinaryResponse = withContext(Dispatchers.IO) { client.image().libraryCover(libraryId) }

    suspend fun getCollectionCover(collectionId: Int): BinaryResponse = withContext(Dispatchers.IO) { client.image().collectionCover(collectionId) }

    suspend fun getReadinglistCover(readingListId: Int): BinaryResponse = withContext(Dispatchers.IO) { client.image().readinglistCover(readingListId) }

    suspend fun getPersonCover(personId: Int): BinaryResponse = withContext(Dispatchers.IO) { client.image().personCover(personId) }

    suspend fun getPersonCoverByName(name: String): BinaryResponse = withContext(Dispatchers.IO) { client.image().personCoverByName(name) }

    suspend fun getBookmark(chapterId: Int, pageNum: Int): BinaryResponse = withContext(Dispatchers.IO) { client.image().bookmark(chapterId, pageNum) }

    suspend fun getWebImage(url: String): BinaryResponse = withContext(Dispatchers.IO) { client.image().web(url) }

    suspend fun getPublisherImage(publisherName: String): BinaryResponse = withContext(Dispatchers.IO) { client.image().publisher(publisherName) }

    suspend fun getVolumeDownloadUrl(volumeId: Int): UrlResponse = withContext(Dispatchers.IO) { client.download().volumeLink(volumeId) }

    suspend fun downloadVolume(volumeId: Int): BinaryResponse = withContext(Dispatchers.IO) { client.download().volume(volumeId) }

    suspend fun getChapterDownloadUrl(chapterId: Int): UrlResponse = withContext(Dispatchers.IO) { client.download().chapterLink(chapterId) }

    suspend fun downloadChapter(chapterId: Int): BinaryResponse = withContext(Dispatchers.IO) { client.download().chapter(chapterId) }

    suspend fun getSeriesDownloadUrl(seriesId: Int): UrlResponse = withContext(Dispatchers.IO) { client.download().seriesLink(seriesId) }

    suspend fun downloadSeries(seriesId: Int): BinaryResponse = withContext(Dispatchers.IO) { client.download().series(seriesId) }

    suspend fun getAccountInfo(): HttpClientResponse<User> = withContext(Dispatchers.IO) {
        client.account().refreshAccount()
    }

    suspend fun getOnDeckSeries(libraryId: Int, pageSize: Int, page: Int): HttpClientResponse<SeriesDtoList> = withContext(Dispatchers.IO) {
        client.series().onDeck(libraryId, pageSize, page)
    }

    suspend fun getRecentlyUpdatedSeries(): HttpClientResponse<RecentlyAddedItems> = withContext(Dispatchers.IO) {
        client.series().recentlyUpdateSeries()
    }

    suspend fun getNewlyAddedSeries(pageNumber: Int, pageSize: Int): HttpClientResponse<SeriesList> = withContext(Dispatchers.IO) {
        client.series().recentlyAddedV2(pageNumber, pageSize, FilterV2())
    }

    suspend fun searchSeries(query: String, includeChapterAndFiles: Boolean = false) = withContext(Dispatchers.IO) {
        client.search().search(query, includeChapterAndFiles)
    }

    suspend fun getSeriesDetailWithVolumes(seriesId: Int) = withContext(Dispatchers.IO) {
        client.series().getSeriesDetail(seriesId)
    }
}