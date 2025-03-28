package ru.feryafox.kavify.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.feryafox.kavify.data.models.Book
import ru.feryafox.kavify.data.repositories.KavitaRepository
import ru.feryafox.kavify.domain.servicies.Kavita4JService
import javax.inject.Inject

@HiltViewModel
class BookListViewModel @Inject constructor(
    private val kavitaService: Kavita4JService
) : ViewModel() {
    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books

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
}
