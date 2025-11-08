package ru.feryafox.kavify.kavita.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.feryafox.kavify.kavita.repository.KavitaRepository
import ru.feryafox.kavify.kavita.ui.models.Book
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
                val onDeckResponse = repository.getOnDeckSeries(1, 20, 0)

                if (onDeckResponse.statusCode() == 200 && onDeckResponse.responseModel() != null) {
                    val books = onDeckResponse.responseModel().seriesDto.map { seriesDto ->
                        val seriesDetailResponse = repository.getSeriesDetail(seriesDto.id)
                        val coverUrl = if (seriesDetailResponse.isSuccess) {
                            val volumeId = seriesDetailResponse.responseModel()?.volumes?.firstOrNull()?.id ?: 0
                            repository.getSeriesCoverUrl(volumeId)
                        } else {
                            null
                        }
                        Book(series = seriesDto, coverUrl = coverUrl)
                    }
                    if (books.isNotEmpty()) {
                        _uiState.value = MyBooksUiState.Success(books)
                    } else {
                        _uiState.value = MyBooksUiState.Empty
                    }
                } else {
                    _uiState.value = MyBooksUiState.Error(onDeckResponse.errorMessage() ?: "Unknown error")
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
    data class Success(val books: List<Book>) : MyBooksUiState()
    data class Error(val message: String) : MyBooksUiState()
}
