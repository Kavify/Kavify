package ru.feryafox.kavify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.feryafox.kavify.data.repositories.preferences.PreferencesManagerFactory
import ru.feryafox.kavify.presentation.ui.AppNavGraph
import ru.feryafox.kavify.presentation.ui.Routes
import ru.feryafox.kavify.presentation.ui.theme.KavifyTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            KavifyTheme {
                val navController = rememberNavController()
                AppNavGraph(Routes.MAIN.path, navController)
            }
        }
    }
}
