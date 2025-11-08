package ru.feryafox.kavify.download

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class DownloadViewModel @Inject constructor(
    private val downloadManager: DownloadManager
) : ViewModel() {

    private val _downloadDirectory = MutableStateFlow<Uri?>(null)
    val downloadDirectory: StateFlow<Uri?> = _downloadDirectory.asStateFlow()

    private val _showDirectoryPicker = MutableStateFlow(false)
    val showDirectoryPicker: StateFlow<Boolean> = _showDirectoryPicker.asStateFlow()

    private val _downloadStarted = MutableStateFlow<String?>(null)
    val downloadStarted: StateFlow<String?> = _downloadStarted.asStateFlow()

    val downloads: StateFlow<Map<String, DownloadInfo>>? = downloadManager.getDownloads()

    val activeDownloads: StateFlow<List<DownloadInfo>> = downloads?.map { downloadsMap ->
        downloadsMap.values
            .filter { it.state is DownloadState.Downloading || it.state is DownloadState.Queued }
            .sortedByDescending { it.timestamp }
    }?.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        ?: MutableStateFlow(emptyList())

    val completedDownloads: StateFlow<List<DownloadInfo>> = downloads?.map { downloadsMap ->
        downloadsMap.values
            .filter { it.state is DownloadState.Completed }
            .sortedByDescending { it.timestamp }
    }?.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        ?: MutableStateFlow(emptyList())

    init {
        loadDownloadDirectory()
    }

    private fun loadDownloadDirectory() {
        val dirString = downloadManager.getDownloadDirectory()
        Log.d("DownloadViewModel", "Loaded directory: $dirString")
        if (dirString.isNotEmpty()) {
            _downloadDirectory.value = Uri.parse(dirString)
        }
    }

    fun setDownloadDirectory(uri: Uri) {
        Log.d("DownloadViewModel", "Setting directory: $uri")
        downloadManager.setDownloadDirectory(uri)
        _downloadDirectory.value = uri
        _showDirectoryPicker.value = false
    }

    fun requestDirectoryPicker() {
        _showDirectoryPicker.value = true
    }

    fun dismissDirectoryPicker() {
        _showDirectoryPicker.value = false
    }

    fun startDownload(itemId: Int, title: String, type: DownloadType) {
        Log.d("DownloadViewModel", "startDownload called: id=$itemId, title=$title, type=$type")
        val directory = _downloadDirectory.value
        if (directory == null) {
            Log.d("DownloadViewModel", "No directory selected, showing picker")
            _showDirectoryPicker.value = true
            return
        }

        Log.d("DownloadViewModel", "Starting download to directory: $directory")
        try {
            val downloadId = downloadManager.startDownload(itemId, title, type, directory)
            _downloadStarted.value = title
            Log.d("DownloadViewModel", "Download started with ID: $downloadId")
        } catch (e: Exception) {
            Log.e("DownloadViewModel", "Failed to start download", e)
        }
    }

    fun clearDownloadStartedMessage() {
        _downloadStarted.value = null
    }

    fun cancelDownload(downloadId: String) {
        downloadManager.cancelDownload(downloadId)
    }

    fun cancelAll() {
        downloadManager.cancelAll()
    }
}

