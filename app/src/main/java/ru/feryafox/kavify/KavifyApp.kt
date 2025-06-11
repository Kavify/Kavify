package ru.feryafox.kavify

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import ru.feryafox.yokailib.preferences.PreferencesManagerInitializer

@HiltAndroidApp
class KavifyApp : Application() {
    override fun onCreate() {
        PreferencesManagerInitializer.init(this)

        super.onCreate()
    }
}
