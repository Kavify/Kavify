package ru.feryafox.kavify.presentation.viewmodels

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
import ru.feryafox.kavify.domain.exceptions.KavifyException
import ru.feryafox.kavify.domain.servicies.AuthService
import ru.feryafox.kavify.domain.servicies.Kavita4JService
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: KavitaRepository,
    private val preferences: PreferencesManager,
    private val authService: AuthService,
    private val kavita4JService: Kavita4JService
) : ViewModel() {
    var errorMessage by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(false)

    fun login(
        server: String,
        username: String,
        password: String,
        apiKey: String,
        useApiKey: Boolean,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                kavita4JService.setBaseUrl(server)
                authService.login(
                    username = username,
                    password = password,
                    apikey = apiKey,
                    useApiKey = useApiKey
                )
                onSuccess()
            } catch (e: KavifyException) {
                errorMessage = e.message
            }
            catch (e: Exception) {
                errorMessage = "Ошибка входа: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}