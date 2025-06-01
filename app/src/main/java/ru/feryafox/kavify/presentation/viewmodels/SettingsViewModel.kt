package ru.feryafox.kavify.presentation.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.feryafox.kavify.base.settings.base.CategorySettings
import ru.feryafox.kavify.ext.kavita.repository.PreferencesManager
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    categories: Set<@JvmSuppressWildcards CategorySettings>
) : ViewModel() {
    val categories: List<CategorySettings> = categories.sortedBy { it.title }
}