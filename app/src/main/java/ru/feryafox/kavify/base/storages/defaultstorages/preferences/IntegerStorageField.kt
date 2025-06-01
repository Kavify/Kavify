package ru.feryafox.kavify.base.storages.defaultstorages.preferences

class IntegerStorageField(
    override val id: String,
    override val key: String,
    override val initValue: Int
): DefaultPreferencesStorageField<Int>(id, key, initValue) {
    override fun serialize(value: Int): String = value.toString()

    override fun deserialize(value: String): Int = value.toInt()
}