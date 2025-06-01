package ru.feryafox.kavify.base.storages.base

interface StorageField<T> {
    val id: String
    val key: String
    var field: T
    val initValue: T

    fun serialize(value: T): String
    fun deserialize(value: String): T
}

fun List<StorageField<*>>.getByKey(key: String) = this.find { it.key == key }!!
