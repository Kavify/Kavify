# Диагностика проблемы загрузки

## Проблема
Показывается Toast "Загрузка начата", но дальше ничего не происходит.

## Что было добавлено
Детальное логирование на всех этапах:
1. DownloadViewModel - логирует вызов startDownload
2. DownloadManager - логирует передачу команды сервису
3. DownloadService - логирует каждый шаг загрузки

## Как проверить что происходит

### Вариант 1: Через терминал
```bash
cd /home/feryafox/AndroidStudioProjects/Kavify
chmod +x check_download_logs.sh
./check_download_logs.sh
```

### Вариант 2: Вручную через adb
```bash
adb logcat | grep -E "DownloadViewModel|DownloadManager|DownloadService"
```

### Вариант 3: В Android Studio
1. Откройте Logcat (View -> Tool Windows -> Logcat)
2. В фильтре введите: `DownloadViewModel|DownloadManager|DownloadService`
3. Нажмите кнопку загрузки в приложении
4. Смотрите логи

## Что искать в логах

### Успешный сценарий должен показать:
```
DownloadViewModel: startDownload called: id=X, title=Y, type=Z
DownloadManager: startDownload called: itemId=X, title=Y
DownloadService: startDownload called: itemId=X, title=Y
DownloadService: Service onCreate
DownloadService: onStartCommand: action=ACTION_START_DOWNLOAD
DownloadService: Starting download: id=..., itemId=...
DownloadService: startDownloadInternal: id=...
DownloadService: Creating notification...
DownloadService: Starting foreground service...
DownloadService: Foreground service started successfully
DownloadService: Starting download job for: [название]
DownloadService: Fetching binary data from API...
DownloadService: Binary data received
DownloadService: Creating input stream...
DownloadService: Filename: [название файла]
DownloadService: Creating file in directory: [uri]
DownloadService: File created: [uri]
DownloadService: Output stream opened, starting data transfer...
DownloadService: First read: X bytes
DownloadService: Download completed: X bytes downloaded
DownloadService: Download marked as completed
```

### Возможные ошибки и решения:

#### 1. "Failed to start download" в DownloadManager
**Причина**: Сервис не может запуститься
**Решение**: Проверьте AndroidManifest.xml - сервис должен быть объявлен

#### 2. "SecurityException: FOREGROUND_SERVICE_DATA_SYNC"
**Причина**: Нет разрешения
**Решение**: Проверьте что в AndroidManifest.xml есть:
```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />
```

#### 3. "Failed to start download internal" + "Cannot create notification channel"
**Причина**: Проблема с уведомлениями
**Решение**: Проверьте разрешение POST_NOTIFICATIONS для Android 13+

#### 4. "Invalid stream type"
**Причина**: API вернуло данные в неправильном формате
**Решение**: Проблема с библиотекой kavita4j или API сервера

#### 5. "Не удалось создать файл"
**Причина**: Нет прав на запись в выбранную директорию
**Решение**: 
- Убедитесь что папка была выбрана через системный диалог
- Проверьте что приложение получило persistable permission

## Проверка разрешений

Проверьте что у приложения есть разрешения:
```bash
adb shell dumpsys package ru.feryafox.kavify | grep permission
```

Должны быть:
- android.permission.FOREGROUND_SERVICE: granted=true
- android.permission.FOREGROUND_SERVICE_DATA_SYNC: granted=true
- android.permission.POST_NOTIFICATIONS: granted=true (для Android 13+)

## Следующие шаги

1. Пересоберите и переустановите приложение
2. Запустите логи в отдельном терминале
3. Нажмите кнопку "Скачать" в приложении
4. Посмотрите где останавливается процесс
5. Сообщите на каком этапе произошла ошибка

