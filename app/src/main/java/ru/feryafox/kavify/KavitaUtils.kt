package ru.feryafox.kavify

fun getImageSeries(baseUrl: String, seriesId: Int, apiKey: String) =
    "${baseUrl}api/image/series-cover?seriesId=${seriesId}&apiKey=${apiKey}"
