package ru.feryafox.kavify.yokailib_ext.ui.screens

import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.feryafox.kavify.yokailib_ext.downloader.Downloader
import ru.feryafox.kavify.yokailib_ext.models.BookDetail
import ru.feryafox.kavify.yokailib_ext.models.CurrentBookDetail
import ru.feryafox.kavify.yokailib_ext.ui.components.BookCard
import ru.feryafox.kavify.yokailib_ext.ui.components.BookDetailContent
import ru.feryafox.kavify.yokailib_ext.ui.configs.BaseSearchScreenConfig
import ru.feryafox.kavify.yokailib_ext.ui.viewmodels.BaseSearchScreenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <B : BookDetail, C : CurrentBookDetail> BaseBookSearchScreen(
    viewModel: BaseSearchScreenViewModel<B, C>,
    screenConfig: BaseSearchScreenConfig,
    downloader: Downloader,
    bookDetailComponent: @Composable (B, () -> Unit) -> Unit = { book, onClick ->
        BookCard(book = book, onClick = onClick)
    },
    currentBookDetailComponent: @Composable (C, Downloader) -> Unit = { book, dl ->
        BookDetailContent(book, dl)
    }
) {
    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedBook by remember { mutableStateOf<BookDetail?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearching) {
                        TextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                viewModel.searchBook(searchQuery)
                            },
                            placeholder = { Text("Поиск...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                                unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                                cursorColor = MaterialTheme.colorScheme.onPrimary,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                    } else {
                        Text(screenConfig.title)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    if (isSearching) {
                        IconButton(onClick = {
                            searchQuery = ""
                            viewModel.searchBook("")
                            isSearching = false
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Закрыть поиск",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    } else {
                        IconButton(onClick = {
                            isSearching = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Поиск",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            items(viewModel.books) { book ->
                bookDetailComponent(
                    book
                ) {
                    selectedBook = book
                    viewModel.selectCurrent(book)
                    coroutineScope.launch {
                        sheetState.show()
                    }
                }
            }
        }
    }

    selectedBook?.let { book ->
        ModalBottomSheet(
            onDismissRequest = {
                coroutineScope.launch {
                    sheetState.hide()
                }.invokeOnCompletion {
                    selectedBook = null
                }
            },
            sheetState = sheetState
        ) {
            if (viewModel.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                viewModel.currentBook?.let { book ->
                    currentBookDetailComponent(
                        book,
                        downloader
                    )
                }
            }
        }
    }
}
