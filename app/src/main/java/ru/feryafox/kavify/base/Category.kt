package ru.feryafox.kavify.base

import androidx.compose.runtime.Composable

interface Category {
    val title: String
    val items: List<CategoryItem>
    val id: String
}

interface CategoryItem {
    val title: String
    val id: String
    val content: @Composable () -> Unit
}
