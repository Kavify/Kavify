package ru.feryafox.kavify.data.models

import ru.feryafox.kavify.getImageSeries
import ru.feryafox.kavita4j.models.responses.search.SearchResultGroup
import ru.feryafox.kavita4j.models.responses.search.SeriesItem

data class Series(
    val seriesId: Int,
    val name: String,
    val apiKey: String
) {
    companion object {
        fun from(seriesItem: SeriesItem, baseUrl: String, apiKey: String): Series =
            Series(
                seriesItem.seriesId,
                seriesItem.name,
                getImageSeries(baseUrl, seriesItem.seriesId, apiKey)
            )

        fun from(seriesItems: List<SeriesItem>, baseUrl: String, apiKey: String): List<Series> =
            seriesItems.map { from(it, baseUrl, apiKey) }

        fun List<SeriesItem>.from(baseUrl: String, apiKey: String): List<Series> =
            this.map { from(it, baseUrl, apiKey) }
    }
}
