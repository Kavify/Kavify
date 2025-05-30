package ru.feryafox.kavify.data.repositories.storages.base

abstract class BaseStorage(
    val id: String,
    val keys: Map<String, StorageField<*>>
) {

}
