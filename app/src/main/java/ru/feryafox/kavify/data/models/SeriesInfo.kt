package ru.feryafox.kavify.data.models


data class SeriesInfo(
    val title: String,
    val description: String,
    val image: String,
    val downloadLinks: List<DownloadLink>
)