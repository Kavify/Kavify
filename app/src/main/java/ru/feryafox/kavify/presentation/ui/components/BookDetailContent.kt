package ru.feryafox.kavify.presentation.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch
import ru.feryafox.kavify.data.models.SeriesInfo
import ru.feryafox.kavify.domain.servicies.Kavita4JDownloader
import ru.feryafox.kavify.presentation.di.DownloaderEntryPoint

@Composable
fun BookDetailContent(
    seriesInfo: SeriesInfo
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val downloader = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            DownloaderEntryPoint::class.java
        ).kavitaDownloader()
    }

    var downloadProgress by remember { mutableStateOf(0) }
    var isDownloading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = seriesInfo.title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        AsyncImage(
            model = seriesInfo.image,
            contentDescription = "Обложка книги",
            modifier = Modifier
                .size(64.dp)
                .padding(end = 16.dp),
            contentScale = ContentScale.Crop
        )
        Text(
            text = seriesInfo.description,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "Ссылки на скачивание:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (isDownloading) {
            LinearProgressIndicator(
                progress = { downloadProgress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            )
            Text("Скачивание: $downloadProgress%")
        }

        seriesInfo.downloadLinks.forEach { link ->
            Text(
                text = link.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        coroutineScope.launch {
                            try {
                                isDownloading = true
                                downloadProgress = 0

                                downloader.downloadVolume(
                                    context = context,
                                    volumeId = link.id,
                                    onProgressUpdate = { progress ->
                                        downloadProgress = progress
                                    }
                                )

                                Toast.makeText(context, "Файл успешно скачан!", Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Ошибка при скачивании: ${e.message}", Toast.LENGTH_LONG).show()
                            } finally {
                                isDownloading = false
                            }
                        }
                    },
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
