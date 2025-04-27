package ru.feryafox.kavify.presentation.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.feryafox.kavify.data.repositories.PreferencesManager
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    var baseUrlSetting by mutableStateOf(preferencesManager.baseUrl)
    var downloadPathSetting by mutableStateOf("")
        private set

    fun logout() {
       preferencesManager.deleteAuthCredentials()
    }

    fun setDownloadPath(path: String) {
        downloadPathSetting = path
        preferencesManager.downloadPath = path // ✅ сохраняем в SharedPreferences
    }
//    init {
//        baseUrlSetting = preferencesManager.baseUrl
//        println(preferencesManager.baseUrl)
//    }
}