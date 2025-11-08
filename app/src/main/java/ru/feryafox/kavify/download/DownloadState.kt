package ru.feryafox.kavify.download

sealed class DownloadState {
    data object Idle : DownloadState()
    data object Queued : DownloadState()
    data class Downloading(val progress: Int, val downloadedBytes: Long, val totalBytes: Long) : DownloadState()
    data class Completed(val filePath: String) : DownloadState()
    data class Failed(val error: String) : DownloadState()
    data object Cancelled : DownloadState()
}

data class DownloadInfo(
    val id: String,
    val title: String,
    val type: DownloadType,
    val itemId: Int,
    val state: DownloadState = DownloadState.Idle,
    val timestamp: Long = System.currentTimeMillis()
)

enum class DownloadType {
    VOLUME,
    CHAPTER,
    SERIES
}

