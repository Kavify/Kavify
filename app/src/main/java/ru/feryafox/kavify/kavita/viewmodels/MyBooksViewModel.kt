package ru.feryafox.kavify.kavita.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.feryafox.kavify.kavita.repository.KavitaRepository
import ru.feryafox.kavita4j.models.responses.series.SeriesDto
import javax.inject.Inject

@HiltViewModel
class MyBooksViewModel @Inject constructor(
    private val repository: KavitaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MyBooksUiState>(MyBooksUiState.Loading)
    val uiState: StateFlow<MyBooksUiState> = _uiState.asStateFlow()

    init {
        loadBooks()
    }

    fun loadBooks() {
        viewModelScope.launch {
            _uiState.value = MyBooksUiState.Loading
            try {
                // Try to get onDeck books first
                val onDeckResponse = repository.getOnDeckSeries(1, 20, 0)

                if (onDeckResponse.statusCode() == 200 &&
                    onDeckResponse.responseModel() != null &&
                    onDeckResponse.responseModel().seriesDto.isNotEmpty()) {
                    _uiState.value = MyBooksUiState.Success(onDeckResponse.responseModel().seriesDto)
                } else {
                    // If no onDeck books, get recently updated
                    val recentlyUpdatedResponse = repository.getRecentlyUpdatedSeries()
                    if (recentlyUpdatedResponse.statusCode() == 200 &&
                        recentlyUpdatedResponse.responseModel() != null &&
                        recentlyUpdatedResponse.responseModel().items.isNotEmpty()) {
                        // Get full series info for recently updated items
                        val seriesList = mutableListOf<SeriesDto>()
                        recentlyUpdatedResponse.responseModel().items.take(10).forEach { item ->
                            val seriesResponse = repository.getOnDeckSeries(1, 1, 0)
                            // For now, just show empty state
                        }
                        _uiState.value = MyBooksUiState.Empty
                    } else {
                        _uiState.value = MyBooksUiState.Error(recentlyUpdatedResponse.errorMessage() ?: "Unknown error")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = MyBooksUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun refresh() {
        loadBooks()
    }
}

sealed class MyBooksUiState {
    object Loading : MyBooksUiState()
    object Empty : MyBooksUiState()
    data class Success(val books: List<SeriesDto>) : MyBooksUiState()
    data class Error(val message: String) : MyBooksUiState()
}
