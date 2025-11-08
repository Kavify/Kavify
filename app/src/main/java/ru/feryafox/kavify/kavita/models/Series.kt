package ru.feryafox.kavify.kavita.models

data class Series(
    val seriesId: Int,
    val name: String,
    val coverUrl: String?,
    val author: String? = null
)

