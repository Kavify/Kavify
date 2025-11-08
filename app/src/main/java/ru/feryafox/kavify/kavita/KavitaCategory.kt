package ru.feryafox.kavify.kavita

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.feryafox.kavify.kavify.settings.KavifySettings
import ru.feryafox.kavify.kavita.api.Kavita4JManager
import ru.feryafox.kavify.kavita.ui.screens.KavitaSearchScreen
import ru.feryafox.kavify.kavita.ui.screens.MyBooksScreen
import ru.feryafox.kavify.kavita.ui.screens.SeriesDetailScreen
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
    private val kavita4JManager: Kavita4JManager,
): Category, Validatable, AsyncValidatable {
    override val title: String
        get() = "Kavita"

    override val items: List<CategoryItem>
        get() = listOf(
            KavitaCategoryItem(
                "Поиск",
                id = "kavita-search"
            ) {
                val navController = rememberNavController()
                val baseUrl = settings.urlFieldSetting.field.field
                val apiKey = kavita4JManager.client.auth().credentials.apiKey

                NavHost(navController = navController, startDestination = "search") {
                    composable("search") {
                        KavitaSearchScreen(
                            onSeriesClick = { seriesId ->
                                navController.navigate("series/$seriesId")
                            },
                            baseUrl = baseUrl,
                            apiKey = apiKey
                        )
                    }
                    composable("series/{seriesId}") { backStackEntry ->
                        val seriesId = backStackEntry.arguments?.getString("seriesId")?.toIntOrNull() ?: 0
                        SeriesDetailScreen(
                            seriesId = seriesId,
                            baseUrl = baseUrl,
                            apiKey = apiKey,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            },
            KavitaCategoryItem(
                "Мои книги",
                id = "kavita-my-books"
            ) {
                val navController = rememberNavController()
                val baseUrl = settings.urlFieldSetting.field.field
                val apiKey = kavita4JManager.client.auth().credentials.apiKey

                NavHost(navController = navController, startDestination = "myBooks") {
                    composable("myBooks") {
                        MyBooksScreen(
                            onSeriesClick = { seriesId ->
                                navController.navigate("series/$seriesId")
                            }
                        )
                    }
                    composable("series/{seriesId}") { backStackEntry ->
                        val seriesId = backStackEntry.arguments?.getString("seriesId")?.toIntOrNull() ?: 0
                        SeriesDetailScreen(
                            seriesId = seriesId,
                            baseUrl = baseUrl,
                            apiKey = apiKey,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
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
