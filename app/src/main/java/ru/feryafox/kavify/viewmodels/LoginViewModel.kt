package ru.feryafox.kavify.viewmodels

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.feryafox.kavify.data.repositories.KavitaRepository
import ru.feryafox.kavify.data.repositories.PreferencesManager
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: KavitaRepository,
    private val preferences: PreferencesManager
) : ViewModel() {
    var errorMessage by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(false)

    fun login(server: String, username: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                repository.setBaseUrl(server)
                repository.login(username, password)
                preferences.saveAuthCredentials(repository.getClient().auth().credentials)
                preferences.saveBaseUrl(server)
                onSuccess()
            } catch (e: Exception) {
                errorMessage = "Ошибка входа: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun loginWithApiKey(server: String, apiKey: String, onSuccess: () -> Unit) {
        onSuccess()
    }
}