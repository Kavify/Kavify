package ru.feryafox.kavify.kavita.ui.models

import ru.feryafox.kavita4j.models.responses.series.SeriesDto

data class Book(
    val series: SeriesDto,
    val coverUrl: String?
)