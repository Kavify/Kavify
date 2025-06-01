package ru.feryafox.kavify.base.storages.defaultstorages.preferences

import ru.feryafox.kavify.base.storages.base.StorageField
import ru.feryafox.kavify.data.repositories.preferences.PreferencesManager
import ru.feryafox.kavify.data.repositories.preferences.PreferencesManagerFactory

abstract class DefaultPreferencesStorageField<T>(
    override val id: String,
    override val key: String,
    override val initValue: T
) : StorageField<T> {

    private val prefs by lazy { PreferencesManagerFactory.create(id) }

    private var _value: T? = null

    override var field: T
        get() {
            if (_value == null) _value = load()
            return _value!!
        }
        set(value) {
            _value = value
            prefs.setString(key, serialize(value))
        }

    private fun load(): T =
        if (prefs.contains(key)) deserialize(prefs.getString(key))
        else {
            prefs.setString(key, serialize(initValue))
            initValue
        }
}
