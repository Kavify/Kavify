package ru.feryafox.kavify.yokailib_ext.models

data class DownloadLink(
    val title: String,
    val downloadInfo: DownloadInfo
)

abstract class DownloadInfo
