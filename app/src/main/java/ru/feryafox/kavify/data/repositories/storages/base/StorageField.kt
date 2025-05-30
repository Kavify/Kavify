package ru.feryafox.kavify.data.repositories.storages.base

interface StorageField<T> {
    val id: String
    val key: String
    var field: T
    fun fromString(value: String)
}
