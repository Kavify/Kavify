package ru.feryafox.kavify.base.storages.defaultstorages.preferences

class StringStorageField(
    override val id: String,
    override val key: String,
    override val initValue: String
): DefaultPreferencesStorageField<String>(id, key, initValue) {
    override fun serialize(value: String): String = value

    override fun deserialize(value: String): String = value
}