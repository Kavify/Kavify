package ru.feryafox.kavify.base.models

import java.net.URL

abstract class BookDetail(
    val title: String,
    val author: String? = null,
    val image: URL? = null,
)

abstract class CurrentBookDetail(
    title: String,
    author: String?,
    image: URL?,
    val downloadLinks: List<DownloadLink>,
    val description: String? = null,
) : BookDetail(title = title, author = author, image = image)

