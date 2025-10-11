package ru.feryafox.kavify.kavita

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import ru.feryafox.kavify.kavify.settings.KavifySettings
import ru.feryafox.kavify.kavita.api.Kavita4JManager
import ru.feryafox.yokailib.categories.Category
import ru.feryafox.yokailib.categories.CategoryItem
import ru.feryafox.yokailib.utils.AsyncValidatable
import ru.feryafox.yokailib.utils.Validatable
import ru.feryafox.yokailib.utils.ValidationResult
import ru.feryafox.yokailib.utils.ValidationRule
import ru.feryafox.yokailib.utils.requiredEnabledFieldsValidation
import javax.inject.Inject
import javax.inject.Singleton

const val CATEGORY_NAME = "kavita"

@Singleton
class KavitaCategory @Inject constructor(
    private val settings: KavifySettings,
    kavita4JManager: Kavita4JManager,
): Category, Validatable, AsyncValidatable {
    override val title: String
        get() = "Kavita"

    override val items: List<CategoryItem>
        get() = listOf(
            KavitaCategoryItem(
                "Поиск по категорию",
                id = "kavita-search"
            ) {
                Text("Здесь будет поиск по Kavita")
            },
            KavitaCategoryItem(
                "Мои книги",
                id = "kavita-my-books"
            ) {
                Text("Здесь будут мои книги из Kavita")
            },

        )

    override val id: String
        get() = CATEGORY_NAME

    override val validationRules: List<ValidationRule> = listOf(
        *settings.requiredEnabledFieldsValidation().toTypedArray(),
        kavita4JManager
    )

    /**
     * Асинхронная валидация категории.
     * Делегирует вызов к настройкам KavifySettings.
     */
    override suspend fun validateAsync(): ValidationResult {
        return settings.validateAsync()
    }
}

data class KavitaCategoryItem(
    override val title: String,
    override val id: String,
    private val body: @Composable () -> Unit
) : CategoryItem {

    @Composable
    override fun Content() = body()
}
