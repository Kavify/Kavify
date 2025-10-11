# Система валидации с поддержкой разных типов ошибок

## Описание

Я реализовал систему валидации для `Kavita4JManager`, которая поддерживает отображение **разных сообщений об ошибках** в зависимости от типа проблемы аутентификации.

## Типы ошибок

В enum `AuthErrorType` определены следующие типы ошибок:

- `NO_ERROR` - нет ошибки
- `NO_ACCESS` - нет доступа к серверу
- `INVALID_ACCESS_TOKEN` - недействительный access token
- `INVALID_REFRESH_TOKEN` - недействительный refresh token
- `INVALID_API_KEY` - неверный API ключ
- `INVALID_CREDENTIALS` - неверное имя пользователя или пароль
- `NETWORK_ERROR` - ошибка сети
- `UNKNOWN_ERROR` - неизвестная ошибка

## Как это работает

### 1. Автоматическое определение ошибок

Класс `Kavita4JManager` автоматически обрабатывает исключения и устанавливает соответствующий тип ошибки:

```kotlin
try {
    client.auth().login(username, password)
} catch (e: IncorrectCredentialsException) {
    // Автоматически устанавливается AuthErrorType.INVALID_CREDENTIALS
} catch (e: UnknownHostException) {
    // Автоматически устанавливается AuthErrorType.NETWORK_ERROR
}
```

### 2. Ручная установка ошибки

Вы можете вручную установить тип ошибки:

```kotlin
// Получаем доступ к состоянию валидации
val validationState = kavita4JManager.validationState

// Устанавливаем конкретную ошибку
validationState.setError(AuthErrorType.INVALID_API_KEY)

// Проверяем, есть ли ошибка
if (validationState.hasError()) {
    // Получаем сообщение об ошибке
    val errorMessage = validationState.message
    Log.e("Auth", "Ошибка: $errorMessage")
}

// Очищаем ошибку после успешной аутентификации
validationState.clearError()
```

### 3. Проверка текущего типа ошибки

Вы можете получить текущий тип ошибки для специфичной обработки:

```kotlin
when (validationState.getCurrentErrorType()) {
    AuthErrorType.INVALID_API_KEY -> {
        // Показать UI для ввода нового API ключа
    }
    AuthErrorType.NETWORK_ERROR -> {
        // Показать UI с кнопкой "Повторить попытку"
    }
    AuthErrorType.INVALID_CREDENTIALS -> {
        // Показать UI для повторного ввода логина и пароля
    }
    else -> {
        // Обработка других ошибок
    }
}
```

## Интеграция с библиотекой yokailib

`Kavita4JManager` реализует интерфейс `ValidationRule` из библиотеки `yokailib`, что позволяет:

1. **Автоматическое отображение ошибок в UI настроек**
   - Ошибки автоматически показываются в категории `KavitaCategory`
   - Список `validationRules` содержит `kavita4JManager`

2. **Использование свойства `message`**
   ```kotlin
   // Получить текущее сообщение об ошибке
   val errorMessage = kavita4JManager.message
   ```

3. **Метод `validate()`**
   ```kotlin
   // Проверить, валидна ли текущая конфигурация
   val result = kavita4JManager.validate()
   if (result.isValid) {
       // Всё в порядке
   } else {
       // Есть ошибки: result.message
   }
   ```

## Примеры использования

### Пример 1: Обработка ошибки при логине

```kotlin
fun loginUser(username: String, password: String) {
    try {
        val success = kavita4JManager.reAuth()
        if (success) {
            showMessage("Успешная авторизация")
        } else {
            // Получаем конкретное сообщение об ошибке
            val errorMessage = kavita4JManager.message
            showError(errorMessage)
        }
    } catch (e: Exception) {
        // Ошибка уже установлена в authValidation
        showError(kavita4JManager.message)
    }
}
```

### Пример 2: Проверка конкретного типа ошибки

```kotlin
fun handleAuthError() {
    val validationState = kavita4JManager.validationState
    
    when (validationState.getCurrentErrorType()) {
        AuthErrorType.INVALID_ACCESS_TOKEN -> {
            // Access token истек, пробуем обновить с помощью refresh token
            refreshAccessToken()
        }
        AuthErrorType.INVALID_REFRESH_TOKEN -> {
            // Refresh token тоже недействителен, нужна полная повторная авторизация
            showLoginScreen()
        }
        AuthErrorType.NETWORK_ERROR -> {
            // Проблемы с сетью, показываем кнопку повтора
            showRetryButton()
        }
        else -> {
            // Показываем общее сообщение об ошибке
            showError(validationState.message)
        }
    }
}
```

### Пример 3: Добавление собственной обработки ошибок

Если вы хотите добавить новый тип ошибки:

1. Добавьте новое значение в enum `AuthErrorType`:
   ```kotlin
   enum class AuthErrorType {
       // ...existing values...
       SERVER_MAINTENANCE,  // Новый тип
   }
   ```

2. Добавьте сообщение в `AuthValidationRule`:
   ```kotlin
   override val message: String
       get() = when (currentErrorType) {
           // ...existing cases...
           AuthErrorType.SERVER_MAINTENANCE -> "Сервер на обслуживании. Попробуйте позже"
       }
   ```

3. Используйте новый тип:
   ```kotlin
   if (response.statusCode == 503) {
       validationState.setError(AuthErrorType.SERVER_MAINTENANCE)
   }
   ```

## Структура файлов

- `AuthValidationState.kt` - содержит `AuthErrorType` enum и класс `AuthValidationRule`
- `Kavita4JManager.kt` - использует `AuthValidationRule` для отслеживания ошибок
- `KavitaCategory.kt` - добавляет `kavita4JManager` в список `validationRules`

## Преимущества этого подхода

1. ✅ **Централизованная обработка ошибок** - все ошибки аутентификации обрабатываются в одном месте
2. ✅ **Читаемые сообщения** - каждая ошибка имеет понятное пользователю сообщение на русском языке
3. ✅ **Гибкость** - легко добавлять новые типы ошибок
4. ✅ **Интеграция с yokailib** - автоматическое отображение в UI настроек
5. ✅ **Type-safe** - использование enum предотвращает опечатки
6. ✅ **Тестируемость** - легко писать тесты для разных типов ошибок

