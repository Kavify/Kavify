package ru.feryafox.kavify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.feryafox.yokailib.root.ui.AppNavGraph
import ru.feryafox.yokailib.root.ui.Routes
import ru.feryafox.yokailib.root.ui.themes.YokiaLibTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            YokiaLibTheme {
                val navController = rememberNavController()
                AppNavGraph(Routes.MAIN.path, navController)
            }
        }
    }
}

