package ru.feryafox.kavify.download

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.documentfile.provider.DocumentFile
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.feryafox.kavify.kavita.repository.KavitaRepository
import java.io.BufferedInputStream
import java.io.OutputStream
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class DownloadService : Service() {

    @Inject
    lateinit var repository: KavitaRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val notificationManager by lazy {
        getSystemService(NotificationManager::class.java) as NotificationManager
    }

    private val _downloads = MutableStateFlow<Map<String, DownloadInfo>>(emptyMap())
    val downloads: StateFlow<Map<String, DownloadInfo>> = _downloads.asStateFlow()

    private val activeJobs = mutableMapOf<String, Job>()

    companion object {
        private const val TAG = "DownloadService"
        const val CHANNEL_ID = "download_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START_DOWNLOAD = "ACTION_START_DOWNLOAD"
        const val ACTION_CANCEL_DOWNLOAD = "ACTION_CANCEL_DOWNLOAD"
        const val ACTION_CANCEL_ALL = "ACTION_CANCEL_ALL"

        const val EXTRA_DOWNLOAD_ID = "EXTRA_DOWNLOAD_ID"
        const val EXTRA_ITEM_ID = "EXTRA_ITEM_ID"
        const val EXTRA_TITLE = "EXTRA_TITLE"
        const val EXTRA_TYPE = "EXTRA_TYPE"
        const val EXTRA_DIRECTORY_URI = "EXTRA_DIRECTORY_URI"

        private var instance: DownloadService? = null

        fun getDownloads(): StateFlow<Map<String, DownloadInfo>>? {
            return instance?.downloads
        }

        fun startDownload(
            context: Context,
            itemId: Int,
            title: String,
            type: DownloadType,
            directoryUri: Uri
        ): String {
            Log.d(TAG, "startDownload called: itemId=$itemId, title=$title, type=$type, uri=$directoryUri")
            val downloadId = UUID.randomUUID().toString()
            val intent = Intent(context, DownloadService::class.java).apply {
                action = ACTION_START_DOWNLOAD
                putExtra(EXTRA_DOWNLOAD_ID, downloadId)
                putExtra(EXTRA_ITEM_ID, itemId)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_TYPE, type.name)
                putExtra(EXTRA_DIRECTORY_URI, directoryUri.toString())
            }

            Log.d(TAG, "Starting service with download ID: $downloadId")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }

            return downloadId
        }

        fun cancelDownload(context: Context, downloadId: String) {
            val intent = Intent(context, DownloadService::class.java).apply {
                action = ACTION_CANCEL_DOWNLOAD
                putExtra(EXTRA_DOWNLOAD_ID, downloadId)
            }
            context.startService(intent)
        }

        fun cancelAll(context: Context) {
            val intent = Intent(context, DownloadService::class.java).apply {
                action = ACTION_CANCEL_ALL
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        instance = this
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: action=${intent?.action}")
        intent?.let {
            when (it.action) {
                ACTION_START_DOWNLOAD -> {
                    val downloadId = it.getStringExtra(EXTRA_DOWNLOAD_ID) ?: run {
                        Log.e(TAG, "No download ID provided")
                        return START_NOT_STICKY
                    }
                    val itemId = it.getIntExtra(EXTRA_ITEM_ID, -1)
                    val title = it.getStringExtra(EXTRA_TITLE) ?: "Unknown"
                    val typeString = it.getStringExtra(EXTRA_TYPE) ?: run {
                        Log.e(TAG, "No type provided")
                        return START_NOT_STICKY
                    }
                    val directoryUriString = it.getStringExtra(EXTRA_DIRECTORY_URI) ?: run {
                        Log.e(TAG, "No directory URI provided")
                        return START_NOT_STICKY
                    }
                    val type = DownloadType.valueOf(typeString)
                    val directoryUri = Uri.parse(directoryUriString)

                    Log.d(TAG, "Starting download: id=$downloadId, itemId=$itemId, title=$title")
                    startDownloadInternal(downloadId, itemId, title, type, directoryUri)
                }
                ACTION_CANCEL_DOWNLOAD -> {
                    val downloadId = it.getStringExtra(EXTRA_DOWNLOAD_ID)
                    downloadId?.let { id -> cancelDownloadInternal(id) }
                }
                ACTION_CANCEL_ALL -> {
                    cancelAllDownloads()
                }
            }
        }

        return START_STICKY
    }

    private fun startDownloadInternal(
        downloadId: String,
        itemId: Int,
        title: String,
        type: DownloadType,
        directoryUri: Uri
    ) {
        Log.d(TAG, "startDownloadInternal: id=$downloadId, itemId=$itemId, title=$title")

        try {
            val downloadInfo = DownloadInfo(
                id = downloadId,
                title = title,
                type = type,
                itemId = itemId,
                state = DownloadState.Queued
            )

            updateDownload(downloadInfo)
            Log.d(TAG, "Creating notification...")
            val notification = createNotification(downloadInfo)
            Log.d(TAG, "Starting foreground service...")
            startForeground(NOTIFICATION_ID, notification)
            Log.d(TAG, "Foreground service started successfully")

            val job = serviceScope.launch {
                try {
                    Log.d(TAG, "Starting download job for: $title")
                    updateDownload(downloadInfo.copy(state = DownloadState.Downloading(0, 0, 0)))

                    Log.d(TAG, "Fetching binary data from API...")
                    val binaryResponse = when (type) {
                        DownloadType.VOLUME -> repository.downloadVolume(itemId)
                        DownloadType.CHAPTER -> repository.downloadChapter(itemId)
                        DownloadType.SERIES -> repository.downloadSeries(itemId)
                    }
                    Log.d(TAG, "Binary data received")

                    // Get input stream from response data
                    Log.d(TAG, "Creating input stream from data type: ${binaryResponse.data?.javaClass?.name}")

                    // Try to get input stream from the data
                    val inputStream = when (val data = binaryResponse.data) {
                        is java.io.InputStream -> {
                            Log.d(TAG, "Data is InputStream")
                            BufferedInputStream(data)
                        }
                        is ByteArray -> {
                            Log.d(TAG, "Data is ByteArray, size: ${data.size} bytes")
                            BufferedInputStream(data.inputStream())
                        }
                        else -> {
                            Log.e(TAG, "Unknown data type: ${data?.javaClass?.name}")
                            throw Exception("Unsupported stream type: ${data?.javaClass?.name}")
                        }
                    }

                    val totalBytes = if (binaryResponse.data is ByteArray) {
                        (binaryResponse.data as ByteArray).size.toLong()
                    } else {
                        0L
                    }
                    Log.d(TAG, "Total bytes: $totalBytes")

                    val filename = ensureFileExtension(
                        binaryResponse.filename ?: extractFilename(null, title, type),
                        type
                    )
                    Log.d(TAG, "Filename: $filename")

                    Log.d(TAG, "Creating file in directory: $directoryUri")
                    val directory = DocumentFile.fromTreeUri(this@DownloadService, directoryUri)
                    val mimeType = getMimeTypeForDownloadType(type)
                    Log.d(TAG, "Using MIME type: $mimeType")
                    val file = directory?.createFile(mimeType, filename)

                    if (file == null) {
                        throw Exception("Не удалось создать файл")
                    }
                    Log.d(TAG, "File created: ${file.uri}")

                    val outputStream: OutputStream = contentResolver.openOutputStream(file.uri)
                        ?: throw Exception("Не удалось открыть поток для записи")
                    Log.d(TAG, "Output stream opened, starting data transfer...")

                    var downloadedBytes = 0L
                    val buffer = ByteArray(8192)

                    outputStream.use { output ->
                        var bytesRead = inputStream.read(buffer)
                        Log.d(TAG, "First read: $bytesRead bytes")
                        while (isActive && bytesRead != -1) {
                            output.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead

                            val progress = if (totalBytes > 0) {
                                ((downloadedBytes * 100) / totalBytes).toInt()
                            } else 0

                            updateDownload(
                                downloadInfo.copy(
                                    state = DownloadState.Downloading(progress, downloadedBytes, totalBytes)
                                )
                            )

                            if (downloadedBytes % (1024 * 1024) == 0L) {
                                updateNotification(downloadInfo.copy(
                                    state = DownloadState.Downloading(progress, downloadedBytes, totalBytes)
                                ))
                            }

                            bytesRead = inputStream.read(buffer)
                        }
                    }

                    inputStream.close()
                    Log.d(TAG, "Download completed: $downloadedBytes bytes downloaded")

                    if (isActive) {
                        updateDownload(downloadInfo.copy(state = DownloadState.Completed(file.uri.toString())))
                        showCompletionNotification(downloadInfo, file.uri)
                        Log.d(TAG, "Download marked as completed")
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Error during download", e)
                    if (e is CancellationException) {
                        updateDownload(downloadInfo.copy(state = DownloadState.Cancelled))
                    } else {
                        updateDownload(downloadInfo.copy(state = DownloadState.Failed(e.message ?: "Unknown error")))
                        showErrorNotification(downloadInfo, e.message ?: "Unknown error")
                    }
                } finally {
                    activeJobs.remove(downloadId)
                    checkAndStopService()
                    Log.d(TAG, "Download job finished")
                }
            }

            activeJobs[downloadId] = job
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start download internal", e)
            val errorInfo = DownloadInfo(
                id = downloadId,
                title = title,
                type = type,
                itemId = itemId,
                state = DownloadState.Failed(e.message ?: "Failed to start")
            )
            updateDownload(errorInfo)
            showErrorNotification(errorInfo, e.message ?: "Failed to start")
        }
    }

    private fun extractFilename(contentDisposition: String?, title: String, type: DownloadType): String {
        contentDisposition?.let {
            val filenameRegex = "filename=\"?([^\"]+)\"?".toRegex()
            val match = filenameRegex.find(it)
            if (match != null) {
                return match.groupValues[1]
            }
        }

        val sanitizedTitle = title.replace(Regex("[^a-zA-Z0-9.-]"), "_")
        return when (type) {
            DownloadType.VOLUME -> "${sanitizedTitle}_volume.cbz"
            DownloadType.CHAPTER -> "${sanitizedTitle}_chapter.cbz"
            DownloadType.SERIES -> "${sanitizedTitle}_series.zip"
        }
    }

    /**
     * Ensures the filename has the correct extension based on download type.
     * If the filename already has the correct extension, it's returned as-is.
     * Otherwise, the correct extension is appended.
     */
    private fun ensureFileExtension(filename: String, type: DownloadType): String {
        val expectedExtension = when (type) {
            DownloadType.VOLUME, DownloadType.CHAPTER -> ".cbz"
            DownloadType.SERIES -> ".zip"
        }

        return if (filename.endsWith(expectedExtension, ignoreCase = true)) {
            filename
        } else {
            // Remove any existing extension and add the correct one
            val nameWithoutExtension = filename.substringBeforeLast('.')
            if (nameWithoutExtension.isNotEmpty() && nameWithoutExtension != filename) {
                // Had an extension, replace it
                "$nameWithoutExtension$expectedExtension"
            } else {
                // No extension, just append
                "$filename$expectedExtension"
            }
        }
    }

    /**
     * Gets the appropriate MIME type for the download type.
     * CBZ files are ZIP files, but we use specific MIME types for better Android compatibility.
     */
    private fun getMimeTypeForDownloadType(type: DownloadType): String {
        return when (type) {
            DownloadType.VOLUME, DownloadType.CHAPTER -> "application/vnd.comicbook+zip" // CBZ MIME type
            DownloadType.SERIES -> "application/zip"
        }
    }

    private fun cancelDownloadInternal(downloadId: String) {
        activeJobs[downloadId]?.cancel()
        activeJobs.remove(downloadId)

        val downloads = _downloads.value.toMutableMap()
        downloads[downloadId]?.let {
            downloads[downloadId] = it.copy(state = DownloadState.Cancelled)
        }
        _downloads.value = downloads

        checkAndStopService()
    }

    private fun cancelAllDownloads() {
        activeJobs.values.forEach { it.cancel() }
        activeJobs.clear()

        val downloads = _downloads.value.toMutableMap()
        downloads.keys.forEach { downloadId ->
            downloads[downloadId]?.let {
                if (it.state is DownloadState.Downloading || it.state is DownloadState.Queued) {
                    downloads[downloadId] = it.copy(state = DownloadState.Cancelled)
                }
            }
        }
        _downloads.value = downloads

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun updateDownload(downloadInfo: DownloadInfo) {
        val downloads = _downloads.value.toMutableMap()
        downloads[downloadInfo.id] = downloadInfo
        _downloads.value = downloads
    }

    private fun checkAndStopService() {
        if (activeJobs.isEmpty()) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Загрузки",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Уведомления о загрузке книг"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(downloadInfo: DownloadInfo): Notification {
        val cancelIntent = Intent(this, DownloadService::class.java).apply {
            action = ACTION_CANCEL_DOWNLOAD
            putExtra(EXTRA_DOWNLOAD_ID, downloadInfo.id)
        }
        val cancelPendingIntent = PendingIntent.getService(
            this,
            downloadInfo.id.hashCode(),
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Загрузка: ${downloadInfo.title}")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_delete, "Отменить", cancelPendingIntent)

        when (val state = downloadInfo.state) {
            is DownloadState.Downloading -> {
                builder.setProgress(100, state.progress, state.totalBytes == 0L)
                builder.setContentText("${state.progress}% - ${formatBytes(state.downloadedBytes)}/${formatBytes(state.totalBytes)}")
            }
            is DownloadState.Queued -> {
                builder.setProgress(0, 0, true)
                builder.setContentText("В очереди...")
            }
            else -> {
                builder.setProgress(0, 0, true)
            }
        }

        return builder.build()
    }

    private fun updateNotification(downloadInfo: DownloadInfo) {
        notificationManager.notify(NOTIFICATION_ID, createNotification(downloadInfo))
    }

    private fun showCompletionNotification(downloadInfo: DownloadInfo, fileUri: Uri) {
        val openIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(fileUri, "application/zip")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val openPendingIntent = PendingIntent.getActivity(
            this,
            downloadInfo.id.hashCode(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Загрузка завершена")
            .setContentText(downloadInfo.title)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)
            .build()

        notificationManager.notify(downloadInfo.id.hashCode(), notification)
    }

    private fun showErrorNotification(downloadInfo: DownloadInfo, error: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Ошибка загрузки")
            .setContentText("${downloadInfo.title}: $error")
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(downloadInfo.id.hashCode(), notification)
    }

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        serviceScope.cancel()
    }
}

