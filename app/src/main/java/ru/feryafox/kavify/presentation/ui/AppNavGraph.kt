package ru.feryafox.kavify.presentation.ui

import SettingsScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ru.feryafox.kavify.presentation.ui.screens.MainListScreen

@Composable
fun AppNavGraph(startDestination: String, navController: NavHostController) {
    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.MAIN.path) {
            MainListScreen(navController = navController)
        }

        composable(Routes.SETTING.path) {
            SettingsScreen(navController = navController)
        }
    }
}
