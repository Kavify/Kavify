package ru.feryafox.kavify.base.ui.components

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import ru.feryafox.kavify.base.downloader.Downloader
import ru.feryafox.kavify.base.models.BookDetail
import ru.feryafox.kavify.base.models.CurrentBookDetail

@Composable
fun BookDetailContent(
    bookDetail: CurrentBookDetail,
    downloaderCurrent: Downloader
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val downloader = remember { downloaderCurrent }

    var downloadProgress by remember { mutableStateOf(0) }
    var isDownloading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = bookDetail.title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        bookDetail.image?.let {
            AsyncImage(
                model = bookDetail.image,
                contentDescription = "Обложка книги",
                modifier = Modifier
                    .size(64.dp)
                    .padding(end = 16.dp),
                contentScale = ContentScale.Crop
            )
        }

        bookDetail.author?.let {
            Text(
                text = "Автор: ${bookDetail.author}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        bookDetail.description?.let { description ->
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

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

        bookDetail.downloadLinks.forEach { link ->
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

                                downloader.downloadBook(
                                    context,
                                    link,
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
