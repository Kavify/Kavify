#!/bin/bash
# Скрипт для просмотра логов загрузки книг

echo "=== Логи загрузки книг ==="
echo "Нажмите Ctrl+C для выхода"
echo ""

adb logcat -c  # Очистить старые логи
adb logcat | grep -E "DownloadViewModel|DownloadManager|DownloadService" --color=always

