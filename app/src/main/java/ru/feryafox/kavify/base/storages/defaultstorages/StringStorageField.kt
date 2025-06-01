package ru.feryafox.kavify.base.storages.defaultstorages

import ru.feryafox.kavify.data.repositories.preferences.PreferencesManager
import ru.feryafox.kavify.data.repositories.preferences.PreferencesManagerFactory
import ru.feryafox.kavify.base.storages.base.StorageField

class StringStorageField(
    override val id: String,
    override val key: String,
    override val initValue: String
): StorageField<String> {

    private var _field: String
    private val preferencesManager: PreferencesManager = PreferencesManagerFactory.create(id)

    init {
        if (preferencesManager.contains(key)) _field = preferencesManager.getString(key)
        else _field = initValue
    }

    override var field: String
        get() = _field
        set(value) {
            _field = value
        }

    overide
}