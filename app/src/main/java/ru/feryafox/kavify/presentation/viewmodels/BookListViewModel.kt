package ru.feryafox.kavify.presentation.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.feryafox.kavify.data.models.Book
import javax.inject.Inject

@HiltViewModel
class BookListViewModel @Inject constructor() : ViewModel() {
    val books = listOf(
        Book(1, "Дюна", "Фрэнк Герберт"),
        Book(2, "1984", "Джордж Оруэлл"),
        Book(3, "Бойцовский клуб", "Чак Паланик")
    )
}
