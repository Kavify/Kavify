package ru.feryafox.kavify.download

import android.content.Context
import android.net.Uri
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.StateFlow
import ru.feryafox.kavify.kavify.storages.KavifyStorage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "DownloadManager"
    }

    fun getDownloads(): StateFlow<Map<String, DownloadInfo>>? {
        return DownloadService.getDownloads()
    }

    fun getDownloadDirectory(): String {
        val dir = KavifyStorage.DOWNLOADING_DIR_FIELD.field
        Log.d(TAG, "getDownloadDirectory: $dir")
        return dir
    }

    fun setDownloadDirectory(uri: Uri) {
        Log.d(TAG, "setDownloadDirectory: $uri")
        KavifyStorage.DOWNLOADING_DIR_FIELD.field = uri.toString()
    }

    fun startDownload(
        itemId: Int,
        title: String,
        type: DownloadType,
        directoryUri: Uri
    ): String {
        Log.d(TAG, "startDownload called: itemId=$itemId, title=$title, type=$type, uri=$directoryUri")
        try {
            val downloadId = DownloadService.startDownload(context, itemId, title, type, directoryUri)
            Log.d(TAG, "Download started successfully with ID: $downloadId")
            return downloadId
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start download", e)
            throw e
        }
    }

    fun cancelDownload(downloadId: String) {
        DownloadService.cancelDownload(context, downloadId)
    }

    fun cancelAll() {
        DownloadService.cancelAll(context)
    }
}

