package ru.feryafox.kavify.ext.kavita.models

import ru.feryafox.kavify.base.models.BookDetail
import ru.feryafox.kavify.base.models.CurrentBookDetail
import ru.feryafox.kavify.base.models.DownloadLink
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


