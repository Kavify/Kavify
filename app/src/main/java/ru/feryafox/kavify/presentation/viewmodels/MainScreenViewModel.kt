package ru.feryafox.kavify.presentation.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.feryafox.kavify.base.Category
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    categories: Set<@JvmSuppressWildcards Category>
) : ViewModel() {

    val categories: List<Category> = categories.sortedBy { it.title }

}
