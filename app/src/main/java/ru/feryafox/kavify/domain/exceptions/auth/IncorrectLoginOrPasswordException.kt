package ru.feryafox.kavify.domain.exceptions.auth

const val errorText = "Неверное имя пользователя или пароль"

class IncorrectLoginOrPasswordException: AuthException(errorMessage = errorText){
}