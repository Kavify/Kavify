package ru.feryafox.kavify.data.repositories.preferences

import android.content.Context

object PreferencesManagerFactory {

    private lateinit var appContext: Context
    private val cache = mutableMapOf<String, PreferencesManager>()

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun create(id: String): PreferencesManager {
        if (!PreferencesManagerFactory::appContext.isInitialized) {
            throw IllegalStateException("PreferencesManagerFactory is not initialized. Call init(context) first.")
        }

        return cache.getOrPut(id) {
            val prefs = appContext.getSharedPreferences("common_prefs", Context.MODE_PRIVATE)
            PreferencesManager(prefs, id)
        }
    }
}
