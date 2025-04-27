package ru.feryafox.kavify.domain.exceptions

open class KavifyException(
    errorMessage: String
): RuntimeException(errorMessage)
