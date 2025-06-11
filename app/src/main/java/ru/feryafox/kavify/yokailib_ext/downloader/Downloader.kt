package ru.feryafox.kavify.yokailib_ext.downloader

import android.content.Context
import ru.feryafox.kavify.yokailib_ext.models.DownloadLink

interface Downloader {
    suspend fun downloadBook(
        context: Context,
        downloadLink: DownloadLink,
        onProgressUpdate: (Int) -> Unit
    )
}