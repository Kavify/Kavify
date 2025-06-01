package ru.feryafox.kavify.base.storages.base

abstract class BaseStorage(
    val id: String,
    val keys: Map<String, StorageField<*>>
) {

}
