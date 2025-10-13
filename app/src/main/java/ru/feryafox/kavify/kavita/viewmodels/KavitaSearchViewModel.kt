package ru.feryafox.kavify.kavita.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.feryafox.kavify.kavita.repository.KavitaRepository
import ru.feryafox.kavita4j.models.responses.search.SeriesItem
import javax.inject.Inject

@HiltViewModel
class KavitaSearchViewModel @Inject constructor(
    private val repository: KavitaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Empty)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
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
                    val series = response.responseModel().series
                    if (series.isEmpty()) {
                        _uiState.value = SearchUiState.Empty
                    } else {
                        _uiState.value = SearchUiState.Success(series)
                    }
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
}

sealed class SearchUiState {
    object Loading : SearchUiState()
    object Empty : SearchUiState()
    data class Success(val results: List<SeriesItem>) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}
