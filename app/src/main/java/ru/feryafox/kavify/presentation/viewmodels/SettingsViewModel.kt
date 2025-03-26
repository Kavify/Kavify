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


    fun logout() {
       preferencesManager.deleteAuthCredentials()
    }
//    init {
//        baseUrlSetting = preferencesManager.baseUrl
//        println(preferencesManager.baseUrl)
//    }
}