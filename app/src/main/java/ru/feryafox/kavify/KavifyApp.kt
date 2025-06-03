package ru.feryafox.kavify

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import ru.feryafox.yokailib.preferences.PreferencesManagerFactory

@HiltAndroidApp
class KavifyApp : Application() {
    override fun onCreate() {
        PreferencesManagerFactory.init(this)

        super.onCreate()
    }
}
