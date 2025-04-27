package ru.feryafox.kavify.presentation.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.feryafox.kavify.data.models.Book
import ru.feryafox.kavify.data.models.SeriesInfo
import ru.feryafox.kavify.domain.servicies.Kavita4JService
import ru.feryafox.kavita4j.models.responses.series.SeriesDetail
import javax.inject.Inject

@HiltViewModel
class BookListViewModel @Inject constructor(
    private val kavitaService: Kavita4JService
) : ViewModel() {
    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books
    var isLoading by mutableStateOf(false)
        private set
    var seriesDetail by mutableStateOf<SeriesInfo?>(null)
        private set

    fun searchBook(query: String) {
        viewModelScope.launch {
            _books.value = if (query.isNotBlank()) {
                kavitaService.searchBook(query)
                    .map { series ->
                        Book(
                            id = series.seriesId,
                            title = series.name,
                            author = "Автор неизвестен",
                            coverUrl = series.coverUrl
                        )
                    }
            } else {
                emptyList()
            }
        }
    }

    fun loadSeriesDetail(book: Book) {
        viewModelScope.launch {
            isLoading = true
            try {
                val series = kavitaService.getSeriesDetail(book)
                seriesDetail = series
            }
            finally {
                isLoading = false
            }
        }
    }
}
