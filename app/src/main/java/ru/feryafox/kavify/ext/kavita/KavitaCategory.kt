package ru.feryafox.kavify.ext.kavita

import androidx.compose.runtime.Composable
import ru.feryafox.kavify.base.Category
import ru.feryafox.kavify.base.CategoryItem
import ru.feryafox.kavify.base.ui.screens.BaseBookSearchScreen
import ru.feryafox.kavify.base.ui.viewmodels.BaseSearchScreenViewModel
import ru.feryafox.kavify.ext.kavita.models.KavitaBookDetail
import ru.feryafox.kavify.ext.kavita.models.KavitaCurrentBookDetail
import javax.inject.Inject
import javax.inject.Singleton


val CATEGORY_NAME = "kavita"

@Singleton
class KavitaCategory @Inject constructor(): Category{
    override val title: String
        get() = "Kavita"

    override val items: List<CategoryItem>
        get() = listOf(
            KavitaCategoryItem(
                "Поиск",
                id = "kavita-search",

            )
        )

    override val id: String
        get() = CATEGORY_NAME
}

data class KavitaCategoryItem(
    override val title: String,
    override val id: String,
    override val content: @Composable () -> Unit
):  CategoryItem