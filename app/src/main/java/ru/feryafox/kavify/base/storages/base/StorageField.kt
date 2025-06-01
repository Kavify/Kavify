package ru.feryafox.kavify.base.storages.base

interface StorageField<T> {
    val id: String
    val key: String
    var field: T
    val initValue: T
    var fieldString: T
}
