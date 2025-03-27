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
import javax.inject.Inject

@HiltViewModel
class BookListViewModel @Inject constructor(
    kavitaRepository: KavitaRepository
) : ViewModel() {
    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books

    fun searchBook(query: String) {
        viewModelScope.launch {
            _books.value = if (!query.isEmpty())


        }
    }
}
