package ru.feryafox.kavify.kavita

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import ru.feryafox.yokailib.categories.Category
import ru.feryafox.yokailib.categories.CategoryItem
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
                "Поиск по категорию",
                id = "kavita-search"
            ) {
                Text("Здесь будет поиск по Kavita")
            }
        )

    override val id: String
        get() = CATEGORY_NAME
}

data class KavitaCategoryItem(
    override val title: String,
    override val id: String,
    private val body: @Composable () -> Unit
) : CategoryItem {

    @Composable
    override fun Content() = body()
}
