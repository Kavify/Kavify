package ru.feryafox.kavify.base.models

data class DownloadLink(
    val title: String,
    val downloadInfo: DownloadInfo
)

abstract class DownloadInfo
