package ru.feryafox.kavify.kavita.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import ru.feryafox.kavify.kavita.repository.KavitaRepository
import ru.feryafox.kavify.kavita.models.Series
import javax.inject.Inject

@HiltViewModel
class KavitaSearchViewModel @Inject constructor(
    private val repository: KavitaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Loading)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _onDeckSeries = MutableStateFlow<List<Series>>(emptyList())
    val onDeckSeries: StateFlow<List<Series>> = _onDeckSeries.asStateFlow()

    private val _recentlyUpdatedSeries = MutableStateFlow<List<Series>>(emptyList())
    val recentlyUpdatedSeries: StateFlow<List<Series>> = _recentlyUpdatedSeries.asStateFlow()

    private val _newlyAddedSeries = MutableStateFlow<List<Series>>(emptyList())
    val newlyAddedSeries: StateFlow<List<Series>> = _newlyAddedSeries.asStateFlow()

    init {
        loadCarousels()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        // If user clears the query, show carousels again
        if (query.isEmpty()) {
            _uiState.value = SearchUiState.Empty
        }
    }

    fun search() {
        val query = _searchQuery.value.trim()
        if (query.isEmpty()) {
            _uiState.value = SearchUiState.Empty
            return
        }

        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            try {
                val response = repository.searchSeries(query, false)
                if (response.statusCode() == 200 && response.responseModel() != null) {
                    val seriesItems = response.responseModel().series
                    // Fetch cover URLs and author info for each series
                    val seriesWithDetails = seriesItems.map { seriesItem ->
                        async {
                            try {
                                val coverUrl = repository.getSeriesCoverUrl(seriesItem.seriesId)
                                val seriesDetail = repository.getSeriesDetail(seriesItem.seriesId)

                                // Extract authors from series detail
                                val authors = if (seriesDetail.isSuccess()) {
                                    val detail = seriesDetail.responseModel()
                                    val writers = when {
                                        detail?.volumes?.isNotEmpty() == true &&
                                        detail.volumes.first().chapters.isNotEmpty() ->
                                            detail.volumes.first().chapters.first().writers
                                        detail?.specials?.isNotEmpty() == true ->
                                            detail.specials.first().writers
                                        detail?.chapters?.isNotEmpty() == true ->
                                            detail.chapters.first().writers
                                        else -> null
                                    }
                                    writers?.joinToString(", ") { it.name }
                                } else null

                                Triple(seriesItem, coverUrl, authors)
                            } catch (e: Exception) {
                                Triple(seriesItem, null, null)
                            }
                        }
                    }.map { it.await() }

                    val series = seriesWithDetails.map { (seriesItem, coverUrl, authors) ->
                        Series(
                            seriesId = seriesItem.seriesId,
                            name = seriesItem.name,
                            coverUrl = coverUrl,
                            author = authors
                        )
                    }
                    _uiState.value = SearchUiState.Success(series)
                } else {
                    _uiState.value = SearchUiState.Error(response.errorMessage() ?: "Unknown error")
                }
            } catch (e: Exception) {
                _uiState.value = SearchUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _uiState.value = SearchUiState.Empty
    }

    fun loadCarousels() {
        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            try {
                val onDeckDeferred = async { repository.getOnDeckSeries(0, 20, 1) }

                val onDeckResponse = onDeckDeferred.await()

                // OnDeck returns SeriesDtoList with seriesDto field
                if (onDeckResponse.isSuccess()) {
                    val seriesDtos = onDeckResponse.responseModel()?.seriesDto ?: emptyList()
                    // Fetch cover URLs and author info for each series
                    val seriesWithDetails = seriesDtos.map { seriesDto ->
                        async {
                            try {
                                val coverUrl = repository.getSeriesCoverUrl(seriesDto.id)
                                val seriesDetail = repository.getSeriesDetail(seriesDto.id)

                                // Extract authors from series detail
                                val authors = if (seriesDetail.isSuccess()) {
                                    val detail = seriesDetail.responseModel()
                                    val writers = when {
                                        detail?.volumes?.isNotEmpty() == true &&
                                        detail.volumes.first().chapters.isNotEmpty() ->
                                            detail.volumes.first().chapters.first().writers
                                        detail?.specials?.isNotEmpty() == true ->
                                            detail.specials.first().writers
                                        detail?.chapters?.isNotEmpty() == true ->
                                            detail.chapters.first().writers
                                        else -> null
                                    }
                                    writers?.joinToString(", ") { it.name }
                                } else null

                                Triple(seriesDto, coverUrl, authors)
                            } catch (e: Exception) {
                                Triple(seriesDto, null, null)
                            }
                        }
                    }.map { it.await() }

                    val series = seriesWithDetails.map { (seriesDto, coverUrl, authors) ->
                        Series(
                            seriesId = seriesDto.id,
                            name = seriesDto.name,
                            coverUrl = coverUrl,
                            author = authors
                        )
                    }
                    _onDeckSeries.value = series
                }

                // TODO: RecentlyUpdated and NewlyAdded return different types (RecentlyAddedItems and SeriesList)
                // which don't contain SeriesDto directly. Need to implement conversion or fetch individual series.
                _recentlyUpdatedSeries.value = emptyList()
                _newlyAddedSeries.value = emptyList()

                _uiState.value = SearchUiState.Empty
            } catch (e: Exception) {
                _uiState.value = SearchUiState.Error(e.message ?: "Unknown error loading carousels")
            }
        }
    }
}

sealed class SearchUiState {
    object Loading : SearchUiState()
    object Empty : SearchUiState()
    data class Success(val results: List<Series> = emptyList()) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}
