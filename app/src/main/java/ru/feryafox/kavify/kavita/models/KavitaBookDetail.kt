package ru.feryafox.kavify.kavita.models

import ru.feryafox.kavify.yokailib_ext.models.BookDetail
import ru.feryafox.kavify.yokailib_ext.models.CurrentBookDetail
import ru.feryafox.kavify.yokailib_ext.models.DownloadLink
import java.net.URL

class KavitaBookDetail(
    title: String,
    author: String? = null,
    image: URL? = null
): BookDetail(title, author, image)

class KavitaCurrentBookDetail(
    title: String,
    author: String? = null,
    image: URL? = null,
    downloadLinks: List<DownloadLink>,
    description: String? = null,
): CurrentBookDetail(title, author, image, downloadLinks, description)


