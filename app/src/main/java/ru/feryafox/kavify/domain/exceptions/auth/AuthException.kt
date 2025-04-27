package ru.feryafox.kavify.domain.exceptions.auth

import ru.feryafox.kavify.domain.exceptions.KavifyException

open class AuthException(
    errorMessage: String
): KavifyException(errorMessage)