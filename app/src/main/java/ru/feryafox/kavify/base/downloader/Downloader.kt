package ru.feryafox.kavify.base.downloader

import android.content.Context
import ru.feryafox.kavify.base.models.DownloadLink

interface Downloader {
    suspend fun downloadBook(
        context: Context,
        downloadLink: DownloadLink,
        onProgressUpdate: (Int) -> Unit
    )
}