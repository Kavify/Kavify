package ru.feryafox.kavify.presentation.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ru.feryafox.kavify.presentation.ui.screens.BookListScreen
import ru.feryafox.kavify.presentation.ui.screens.LoginScreen

@Composable
fun AppNavGraph(startDestination: String, navController: NavHostController) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.LOGIN.path) {
            LoginScreen(navController = navController)
        }

        composable(Routes.SEARCH.path) {
            BookListScreen(navController = navController)
        }

//        composable("book/{id}") { backStackEntry ->
//            val bookId = backStackEntry.arguments?.getString("id")?.toIntOrNull()
//            BookDetailScreen(bookId)
//        }
    }
}
