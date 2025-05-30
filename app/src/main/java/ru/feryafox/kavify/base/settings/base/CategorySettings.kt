package ru.feryafox.kavify.base.settings.base

import ru.feryafox.kavify.data.repositories.preferences.PreferencesManagerFactory

abstract class CategorySettings(
    val id: String,
    val title: String?,
    val fields: List<SettingField<*>>,
    val onSaved: () -> Unit = {}
) {
    fun save() {
        val preferencesManager = PreferencesManagerFactory.create(id)
        fields.forEach {
            if (it.isUpdated || it.isOnUpdateBehavior == OnUpdateBehavior.ON_SAVED) it.onUpdate()
            preferencesManager.setString(it.key, it.getFieldString())
            onSaved()
        }
    }

    fun load() {
        val preferencesManager = PreferencesManagerFactory.create(id)

        fields.forEach {
            if (preferencesManager.contains(it.key)) it.loadField(preferencesManager.getString(it.key))
        }
    }

    fun get(key: String): Any? = fields.firstOrNull { it.key == key }?.field
}
