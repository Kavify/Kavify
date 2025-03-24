package ru.feryafox.kavify.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import ru.feryafox.kavify.presentation.viewmodels.BookListViewModel
import ru.feryafox.kavify.presentation.ui.components.BookCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookListScreen(
    navController: NavHostController,
    viewModel: BookListViewModel = hiltViewModel()
) {
    val books = viewModel.books

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Моя библиотека") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            items(books) { book ->
                BookCard(book = book) {
                    navController.navigate("book/${book.id}")
                }
            }
        }
    }
}
