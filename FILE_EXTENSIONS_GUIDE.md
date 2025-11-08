# Управление расширениями файлов при скачивании

## Обзор

В `DownloadService` реализована система автоматического определения и установки правильных расширений файлов при скачивании контента из Kavita.

## Типы файлов и расширения

| Тип загрузки | Расширение | MIME тип | Описание |
|--------------|------------|----------|----------|
| `VOLUME` | `.cbz` | `application/vnd.comicbook+zip` | Comic Book ZIP архив |
| `CHAPTER` | `.cbz` | `application/vnd.comicbook+zip` | Comic Book ZIP архив |
| `SERIES` | `.zip` | `application/zip` | ZIP архив со всеми томами |

## Реализованные функции

### 1. `ensureFileExtension()`

Гарантирует, что файл имеет правильное расширение:

```kotlin
private fun ensureFileExtension(filename: String, type: DownloadType): String {
    val expectedExtension = when (type) {
        DownloadType.VOLUME, DownloadType.CHAPTER -> ".cbz"
        DownloadType.SERIES -> ".zip"
    }
    
    return if (filename.endsWith(expectedExtension, ignoreCase = true)) {
        filename // Уже есть правильное расширение
    } else {
        // Заменяет неправильное расширение или добавляет его
        val nameWithoutExtension = filename.substringBeforeLast('.')
        if (nameWithoutExtension.isNotEmpty() && nameWithoutExtension != filename) {
            "$nameWithoutExtension$expectedExtension" // Было расширение - заменить
        } else {
            "$filename$expectedExtension" // Не было - добавить
        }
    }
}
```

#### Примеры работы:

```kotlin
// Том с правильным расширением
ensureFileExtension("MyVolume.cbz", DownloadType.VOLUME) 
// → "MyVolume.cbz" (без изменений)

// Том с неправильным расширением
ensureFileExtension("MyVolume.zip", DownloadType.VOLUME) 
// → "MyVolume.cbz" (заменено .zip на .cbz)

// Том без расширения
ensureFileExtension("MyVolume", DownloadType.VOLUME) 
// → "MyVolume.cbz" (добавлено .cbz)

// Серия с правильным расширением
ensureFileExtension("MySeries.zip", DownloadType.SERIES) 
// → "MySeries.zip" (без изменений)

// Серия с неправильным расширением
ensureFileExtension("MySeries.cbz", DownloadType.SERIES) 
// → "MySeries.zip" (заменено .cbz на .zip)
```

### 2. `getMimeTypeForDownloadType()`

Возвращает правильный MIME-тип для типа загрузки:

```kotlin
private fun getMimeTypeForDownloadType(type: DownloadType): String {
    return when (type) {
        DownloadType.VOLUME, DownloadType.CHAPTER -> "application/vnd.comicbook+zip"
        DownloadType.SERIES -> "application/zip"
    }
}
```

#### Зачем нужен правильный MIME-тип?

- **Лучшая совместимость**: Android использует MIME-тип для определения приложений, которые могут открыть файл
- **Правильные иконки**: Файловые менеджеры показывают правильные иконки
- **Автоматическое открытие**: CBZ файлы автоматически открываются в приложениях для чтения комиксов

### 3. `extractFilename()`

Создает имя файла по умолчанию, если сервер не предоставил имя:

```kotlin
private fun extractFilename(contentDisposition: String?, title: String, type: DownloadType): String {
    // Пытаемся извлечь имя из Content-Disposition заголовка
    contentDisposition?.let {
        val filenameRegex = "filename=\"?([^\"]+)\"?".toRegex()
        val match = filenameRegex.find(it)
        if (match != null) {
            return match.groupValues[1]
        }
    }

    // Создаем имя на основе названия и типа
    val sanitizedTitle = title.replace(Regex("[^a-zA-Z0-9.-]"), "_")
    return when (type) {
        DownloadType.VOLUME -> "${sanitizedTitle}_volume.cbz"
        DownloadType.CHAPTER -> "${sanitizedTitle}_chapter.cbz"
        DownloadType.SERIES -> "${sanitizedTitle}_series.zip"
    }
}
```

## Процесс скачивания с правильным расширением

```kotlin
// 1. Получаем имя файла от сервера или создаем по умолчанию
val rawFilename = binaryResponse.filename ?: extractFilename(null, title, type)

// 2. Гарантируем правильное расширение
val filename = ensureFileExtension(rawFilename, type)
// Пример: "MyVolume.zip" → "MyVolume.cbz" для VOLUME

// 3. Получаем правильный MIME-тип
val mimeType = getMimeTypeForDownloadType(type)
// Для VOLUME: "application/vnd.comicbook+zip"

// 4. Создаем файл с правильным именем и MIME-типом
val file = directory?.createFile(mimeType, filename)
```

## Примеры использования

### Скачивание тома

```kotlin
DownloadService.startDownload(
    context = context,
    itemId = 123,
    title = "Моя Манга Том 1",
    type = DownloadType.VOLUME,
    directoryUri = selectedDirectoryUri
)
```

**Результат:**
- Имя файла: `Моя_Манга_Том_1_volume.cbz` (если имя не пришло с сервера)
- Расширение: `.cbz`
- MIME-тип: `application/vnd.comicbook+zip`

### Скачивание серии

```kotlin
DownloadService.startDownload(
    context = context,
    itemId = 456,
    title = "Моя Манга",
    type = DownloadType.SERIES,
    directoryUri = selectedDirectoryUri
)
```

**Результат:**
- Имя файла: `Моя_Манга_series.zip` (если имя не пришло с сервера)
- Расширение: `.zip`
- MIME-тип: `application/zip`

## Преимущества реализации

✅ **Всегда правильное расширение**: Даже если сервер вернул неправильное имя файла  
✅ **Правильные MIME-типы**: Файлы корректно распознаются Android  
✅ **Безопасность имен**: Специальные символы заменяются на подчеркивания  
✅ **Поддержка имен от сервера**: Если сервер дает имя файла, оно используется (с проверкой расширения)  
✅ **Fallback логика**: Если имени нет, генерируется осмысленное имя по умолчанию  

## Логирование

Сервис логирует информацию о файлах:

```kotlin
Log.d(TAG, "Filename: $filename")
Log.d(TAG, "Using MIME type: $mimeType")
Log.d(TAG, "File created: ${file.uri}")
```

Это помогает отлаживать проблемы с сохранением файлов.

## Формат CBZ

CBZ (Comic Book ZIP) - это стандартный формат для цифровых комиксов:
- Это обычный ZIP архив с расширением `.cbz`
- Содержит изображения страниц (обычно JPEG или PNG)
- Поддерживается большинством читалок комиксов на Android
- Может быть открыт любым ZIP архиватором

## Формат ZIP для серий

Серии скачиваются как обычные ZIP архивы:
- Содержат несколько CBZ файлов (тома/главы)
- Имеют стандартное расширение `.zip`
- Могут быть распакованы любым архиватором

