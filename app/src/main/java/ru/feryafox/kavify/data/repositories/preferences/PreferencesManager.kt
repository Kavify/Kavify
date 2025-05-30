package ru.feryafox.kavify.data.repositories.preferences

import android.content.SharedPreferences
import androidx.core.content.edit

class PreferencesManager(
    private val prefs: SharedPreferences,
    private val id: String
) {

    private fun key(suffix: String): String = "${id}_$suffix"

    fun setString(suffix: String, value: String) {
        prefs.edit { putString(key(suffix), value) }
    }

    fun getString(suffix: String, default: String = ""): String {
        return prefs.getString(key(suffix), default) ?: default
    }

    fun setInt(suffix: String, value: Int) {
        prefs.edit { putInt(key(suffix), value) }
    }

    fun getInt(suffix: String, default: Int = 0): Int {
        return prefs.getInt(key(suffix), default)
    }

    fun setBoolean(suffix: String, value: Boolean) {
        prefs.edit { putBoolean(key(suffix), value) }
    }

    fun getBoolean(suffix: String, default: Boolean = false): Boolean {
        return prefs.getBoolean(key(suffix), default)
    }

    fun setFloat(suffix: String, value: Float) {
        prefs.edit { putFloat(key(suffix), value) }
    }

    fun getFloat(suffix: String, default: Float = 0f): Float {
        return prefs.getFloat(key(suffix), default)
    }

    fun setLong(suffix: String, value: Long) {
        prefs.edit { putLong(key(suffix), value) }
    }

    fun getLong(suffix: String, default: Long = 0L): Long {
        return prefs.getLong(key(suffix), default)
    }

    fun remove(suffix: String) {
        prefs.edit { remove(key(suffix)) }
    }

    fun contains(suffix: String): Boolean {
        return prefs.contains(key(suffix))
    }
}
