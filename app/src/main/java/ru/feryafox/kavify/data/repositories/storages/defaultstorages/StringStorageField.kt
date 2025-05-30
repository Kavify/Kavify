package ru.feryafox.kavify.data.repositories.storages.defaultstorages

import ru.feryafox.kavify.data.repositories.preferences.PreferencesManager
import ru.feryafox.kavify.data.repositories.preferences.PreferencesManagerFactory
import ru.feryafox.kavify.data.repositories.storages.base.StorageField

class StringStorageField(
    override val id: String,
    override val key: String
): StorageField<String> {

    private var _field: String
    private val preferencesManager: PreferencesManager = PreferencesManagerFactory.create(id)

    init {
         
    }

    override var field: String
        get() = _field
        set(value) {
            _field = value
        }

}