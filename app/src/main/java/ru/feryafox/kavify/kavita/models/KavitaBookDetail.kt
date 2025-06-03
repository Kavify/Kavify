package ru.feryafox.kavify.kavita.models

import ru.feryafox.yokailib.models.BookDetail
import ru.feryafox.yokailib.models.CurrentBookDetail
import ru.feryafox.yokailib.models.DownloadLink
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


