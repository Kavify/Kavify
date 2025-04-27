package ru.feryafox.kavify.domain.servicies

import android.content.Context
import android.net.Uri
import ru.feryafox.kavify.data.repositories.KavitaRepository
import ru.feryafox.kavify.data.repositories.PreferencesManager
import androidx.documentfile.provider.DocumentFile
import javax.inject.Inject
import kotlin.math.roundToInt

class Kavita4JDownloader @Inject constructor(
    private val kavitaRepository: KavitaRepository,
    private val preferencesManager: PreferencesManager
) {
    suspend fun downloadVolume(
        context: Context,
        volumeId: Int,
        onProgressUpdate: (Int) -> Unit
    ) {
        try {
            val binaryResponse = kavitaRepository.downloadVolume(volumeId)

            val fileBytes = binaryResponse.data ?: throw Exception("Не данных для скачивания")
            val filename = binaryResponse.filename

            val folderUri = Uri.parse(preferencesManager.downloadPath)

            val documentFile = DocumentFile.fromTreeUri(context, folderUri)
                ?: throw Exception("Невалидный путь загрузки")

            val newFile = documentFile.createFile(
                "application/octet-stream",
                filename
            ) ?: throw Exception("Не удалось создать файл для загрузки")

            context.contentResolver.openOutputStream(newFile.uri)?.use { outputStream ->
                val bufferSize = 4096
                var bytesWritten = 0
                while (bytesWritten < fileBytes.size) {
                    val bytesToWrite = minOf(bufferSize, fileBytes.size - bytesWritten)
                    outputStream.write(fileBytes, bytesWritten, bytesToWrite)
                    bytesWritten += bytesToWrite

                    val progress = (bytesWritten.toDouble() / fileBytes.size.toDouble() * 100).roundToInt()
                    onProgressUpdate(progress)
                }
                outputStream.flush()
            } ?: throw Exception("Не удалось открыть поток записи")
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}