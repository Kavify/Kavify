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
                val seriesDetailResponse = repository.getSeriesDetail(seriesId)

                if (seriesDetailResponse.isSuccess() && seriesDetailResponse.responseModel() != null) {
                    val seriesDetail = seriesDetailResponse.responseModel()!!
                    val seriesCoverUrl = repository.getSeriesCoverUrl(seriesId)
                    val volumeCoverUrls = seriesDetail.volumes.map { repository.getVolumeCoverUrl(it.id) }
                    val chapterCoverUrls = seriesDetail.chapters.map { repository.getChapterCoverUrl(it.id) }
                    val specialCoverUrls = seriesDetail.specials.map { repository.getVolumeCoverUrl(it.volumeId) }

                    // Extract series name from first available item
                    val seriesName = when {
                        seriesDetail.volumes.isNotEmpty() -> seriesDetail.volumes.first().name
                        seriesDetail.specials.isNotEmpty() -> seriesDetail.specials.first().titleName
                        seriesDetail.chapters.isNotEmpty() -> seriesDetail.chapters.first().titleName
                        else -> "Unknown Series"
                    }

                    _uiState.value = SeriesDetailUiState.Success(
                        seriesId = seriesId,
                        seriesDetail = seriesDetail,
                        seriesCoverUrl = seriesCoverUrl,
                        volumeCoverUrls = volumeCoverUrls,
                        chapterCoverUrls = chapterCoverUrls,
                        specialCoverUrls = specialCoverUrls,
                        seriesName = seriesName
                    )
                } else {
                    _uiState.value = SeriesDetailUiState.Error(seriesDetailResponse.errorMessage() ?: "Unknown error")
                }
            } catch (e: Exception) {
                _uiState.value = SeriesDetailUiState.Error(e.message ?: "Unknown error")
            }
        }
    
    }

    suspend fun getVolumeCoverUrl(volumeId: Int): String {
        return repository.getVolumeCoverUrl(volumeId)
    }

    suspend fun getSeriesCoverUrl(seriesId: Int): String {
        return repository.getSeriesCoverUrl(seriesId)
    }
}

sealed class SeriesDetailUiState {
    object Loading : SeriesDetailUiState()
    data class Success(
        val seriesId: Int,
        val seriesDetail: SeriesDetail,
        val seriesCoverUrl: String,
        val volumeCoverUrls: List<String>,
        val chapterCoverUrls: List<String>,
        val specialCoverUrls: List<String>,
        val seriesName: String
    ) : SeriesDetailUiState()
    data class Error(val message: String) : SeriesDetailUiState()
}
