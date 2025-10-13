package ru.feryafox.kavify.kavita.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.feryafox.kavify.kavita.repository.KavitaRepository
import ru.feryafox.kavita4j.models.responses.series.SeriesDetail
import javax.inject.Inject

@HiltViewModel
class SeriesDetailViewModel @Inject constructor(
    private val repository: KavitaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SeriesDetailUiState>(SeriesDetailUiState.Loading)
    val uiState: StateFlow<SeriesDetailUiState> = _uiState.asStateFlow()

    fun loadSeriesDetail(seriesId: Int) {
        viewModelScope.launch {
            _uiState.value = SeriesDetailUiState.Loading
            try {
                val response = repository.getSeriesDetailWithVolumes(seriesId)
                if (response.statusCode() == 200 && response.responseModel() != null) {
                    _uiState.value = SeriesDetailUiState.Success(response.responseModel())
                } else {
                    _uiState.value = SeriesDetailUiState.Error(response.errorMessage() ?: "Unknown error")
                }
            } catch (e: Exception) {
                _uiState.value = SeriesDetailUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun downloadVolume(volumeId: Int) {
        viewModelScope.launch {
            try {
                val downloadUrl = repository.getVolumeDownloadUrl(volumeId)
                // Handle download URL - can be opened in browser or downloaded
                // You can emit this URL to the UI state if needed
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}

sealed class SeriesDetailUiState {
    object Loading : SeriesDetailUiState()
    data class Success(val seriesDetail: SeriesDetail) : SeriesDetailUiState()
    data class Error(val message: String) : SeriesDetailUiState()
}
