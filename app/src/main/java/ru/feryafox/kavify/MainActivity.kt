package ru.feryafox.kavify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.feryafox.kavify.data.repositories.PreferencesManager
import ru.feryafox.kavify.presentation.ui.AppNavGraph
import ru.feryafox.kavify.presentation.ui.Routes
import ru.feryafox.kavify.presentation.ui.theme.KavifyTheme
import ru.feryafox.kavify.startup.StartupInitializer
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var preferencesManager: PreferencesManager

    @Inject
    lateinit var startupInitializer: StartupInitializer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startupInitializer.initialize()

        setContent {
            KavifyTheme {
                val navController = rememberNavController()
                AppNavGraph(getStartDestination(), navController)
            }
        }
    }

    private fun getStartDestination() = if (preferencesManager.isAuthed) Routes.SEARCH.path else Routes.LOGIN.path
}
