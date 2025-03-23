package ru.feryafox.kavify.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.BlendMode.Companion.Screen
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ru.feryafox.kavify.data.models.Book
import ru.feryafox.kavify.ui.screens.BookListScreen
import ru.feryafox.kavify.ui.screens.LoginScreen

@Composable
fun AppNavGraph(startDestination: String, navController: NavHostController) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.LOGIN.path) {
            LoginScreen(onLoginSuccess = {
                navController.navigate(Routes.SEARCH.path) {
                    popUpTo(Routes.LOGIN.path) { inclusive = true }
                }
            })
        }
        composable(Routes.SEARCH.path) {

            val books = listOf(
                Book(1, "Дюна", "Фрэнк Герберт"),
                Book(2, "1984", "Джордж Оруэлл"),
                Book(3, "Бойцовский клуб", "Чак Паланик")
            )

            BookListScreen(books = books, onBookClick = {
                println("Foo")
            })
        }
    }
}

enum class Routes(val path: String) {
    LOGIN("route"),
    SEARCH("search")
}
