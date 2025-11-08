package ru.feryafox.kavify.kavita.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import ru.feryafox.kavify.download.DownloadType
import ru.feryafox.kavify.download.DownloadViewModel
import ru.feryafox.kavify.kavita.viewmodels.SeriesDetailUiState
import ru.feryafox.kavify.kavita.viewmodels.SeriesDetailViewModel
import ru.feryafox.kavita4j.models.responses.series.SeriesDetail
import ru.feryafox.kavita4j.models.responses.series.VolumesItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeriesDetailScreen(
    seriesId: Int,
    viewModel: SeriesDetailViewModel = hiltViewModel(),
    downloadViewModel: DownloadViewModel = hiltViewModel(),
    baseUrl: String,
    apiKey: String,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val downloadStarted by downloadViewModel.downloadStarted.collectAsState()
    val showDirectoryPicker by downloadViewModel.showDirectoryPicker.collectAsState()
    val context = LocalContext.current
    // Show toast when download starts
    LaunchedEffect(downloadStarted) {
        downloadStarted?.let { title ->
            Toast.makeText(context, "Загрузка начата: $title", Toast.LENGTH_SHORT).show()
            downloadViewModel.clearDownloadStartedMessage()
        }
    }


    val directoryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            downloadViewModel.setDownloadDirectory(it)
            Toast.makeText(context, "Папка выбрана", Toast.LENGTH_SHORT).show()
        } ?: run {
            downloadViewModel.dismissDirectoryPicker()
        }
    }

    LaunchedEffect(showDirectoryPicker) {
        if (showDirectoryPicker) {
            directoryPickerLauncher.launch(null)
        }
    }

    LaunchedEffect(seriesId) {
        viewModel.loadSeriesDetail(seriesId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (val state = uiState) {
                            is SeriesDetailUiState.Success -> state.seriesName
                            else -> "Детали серии"
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (val state = uiState) {
                is SeriesDetailUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is SeriesDetailUiState.Success -> {
                    SeriesDetailContent(
                        seriesId = state.seriesId,
                        seriesDetail = state.seriesDetail,
                        seriesCoverUrl = state.seriesCoverUrl,
                        volumeCoverUrls = state.volumeCoverUrls,
                        chapterCoverUrls = state.chapterCoverUrls,
                        specialCoverUrls = state.specialCoverUrls,
                        seriesName = state.seriesName,
                        baseUrl = baseUrl,
                        apiKey = apiKey,
                        downloadViewModel = downloadViewModel
                    )
                }
                is SeriesDetailUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Ошибка: ${state.message}",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadSeriesDetail(seriesId) }) {
                            Text("Повторить")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SeriesDetailContent(
    seriesId: Int,
    seriesDetail: SeriesDetail,
    seriesCoverUrl: String,
    volumeCoverUrls: List<String>,
    chapterCoverUrls: List<String>,
    specialCoverUrls: List<String>,
    seriesName: String,
    baseUrl: String,
    apiKey: String,
    downloadViewModel: DownloadViewModel
) {
    val context = LocalContext.current
    var isDescriptionExpanded by remember { mutableStateOf(false) }

    // Get metadata from the first available item (special, volume, or chapter)
    // Specials and Chapters have summary and writers, but not Volumes
    val summary = when {
        seriesDetail.specials.isNotEmpty() -> seriesDetail.specials.first().summary
        seriesDetail.chapters.isNotEmpty() -> seriesDetail.chapters.first().summary
        seriesDetail.volumes.isNotEmpty() && seriesDetail.volumes.first().chapters.isNotEmpty() -> seriesDetail.volumes.first().chapters.first().summary
        else -> null
    }

    val writers = when {
        seriesDetail.specials.isNotEmpty() -> seriesDetail.specials.first().writers
        seriesDetail.chapters.isNotEmpty() -> seriesDetail.chapters.first().writers
        else -> null
    }


    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SeriesHeader(seriesId, seriesName, seriesDetail, seriesCoverUrl)
        }

        // Show summary if available
        if (summary != null && summary.isNotBlank()) {
            item {
                Text(
                    text = "Описание",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column {
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 5,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (summary.length > 200) {
                        TextButton(
                            onClick = { isDescriptionExpanded = !isDescriptionExpanded }
                        ) {
                            Text(
                                text = if (isDescriptionExpanded) "Свернуть" else "Развернуть",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        // Show writers if available
        if (writers != null && writers.isNotEmpty()) {
            item {
                Text(
                    text = "Авторы",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = writers.joinToString { it.name },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Show specials if available
        if (seriesDetail.specials.isNotEmpty()) {
            items(seriesDetail.specials.size) { index ->
                val special = seriesDetail.specials[index]
                val specialCoverUrl = specialCoverUrls.getOrNull(index) ?: ""
                SpecialItem(
                    special = special,
                    specialCoverUrl = specialCoverUrl,
                    onDownload = {
                        downloadViewModel.startDownload(
                            itemId = special.volumeId,
                            title = special.titleName,
                            type = DownloadType.VOLUME
                        )
                    }
                )
            }
        }

        if (seriesDetail.volumes.isNotEmpty()) {
            item {
                Text(
                    text = "Тома (${seriesDetail.volumes.size})",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            items(seriesDetail.volumes.size) { index ->
                val volume = seriesDetail.volumes[index]
                val volumeCoverUrl = volumeCoverUrls.getOrNull(index) ?: ""
                VolumeItem(
                    volume = volume,
                    volumeCoverUrl = volumeCoverUrl,
                    baseUrl = baseUrl,
                    apiKey = apiKey,
                    onDownload = {
                        downloadViewModel.startDownload(
                            itemId = volume.id,
                            title = volume.name,
                            type = DownloadType.VOLUME
                        )
                    }
                )
            }
        } else if (seriesDetail.chapters.isNotEmpty()) {
            item {
                Text(
                    text = "Главы (${seriesDetail.chapters.size})",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            items(seriesDetail.chapters.size) { index ->
                val chapter = seriesDetail.chapters[index]
                val chapterCoverUrl = chapterCoverUrls.getOrNull(index) ?: ""
                ChapterItem(
                    chapter = chapter,
                    chapterCoverUrl = chapterCoverUrl,
                    onDownload = {
                        downloadViewModel.startDownload(
                            itemId = chapter.id,
                            title = chapter.titleName.ifBlank { "Глава ${chapter.range}" },
                            type = DownloadType.CHAPTER
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun SeriesHeader(
    seriesId: Int,
    seriesName: String,
    seriesDetail: SeriesDetail,
    seriesCoverUrl: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp)
        ) {
            AsyncImage(
                model = seriesCoverUrl,
                contentDescription = "Обложка $seriesName",
                modifier = Modifier
                    .width(120.dp)
                    .height(180.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = seriesName,
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (seriesDetail.volumes.isNotEmpty()) {
                    InfoRow("Томов:", "${seriesDetail.volumes.size}")
                }
                if (seriesDetail.chapters.isNotEmpty()) {
                    InfoRow("Глав:", "${seriesDetail.chapters.size}")
                }
                if (seriesDetail.specials.isNotEmpty()) {
                    InfoRow("Специальных:", "${seriesDetail.specials.size}")
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun VolumeItem(
    volume: VolumesItem,
    volumeCoverUrl: String,
    baseUrl: String,
    apiKey: String,
    onDownload: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Volume cover
            AsyncImage(
                model = volumeCoverUrl,
                contentDescription = "Обложка тома",
                modifier = Modifier
                    .width(50.dp)
                    .height(75.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Volume info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = volume.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Том ${volume.minNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Страниц: ${volume.pages}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (volume.pagesRead > 0) {
                    Text(
                        text = "Прочитано: ${volume.pagesRead}/${volume.pages}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Download button
            IconButton(
                onClick = onDownload
            ) {
                Icon(
                    Icons.Default.Download,
                    contentDescription = "Скачать",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ChapterItem(
    chapter: ru.feryafox.kavita4j.models.responses.series.ChaptersItem,
    chapterCoverUrl: String,
    onDownload: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Chapter cover
            AsyncImage(
                model = if (chapterCoverUrl.isNotBlank()) chapterCoverUrl else null,
                contentDescription = "Обложка главы",
                modifier = Modifier
                    .width(50.dp)
                    .height(75.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chapter.titleName.ifBlank { "Глава ${chapter.range}" },
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "Страниц: ${chapter.pages}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDownload) {
                Icon(
                    Icons.Default.Download,
                    contentDescription = "Скачать",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun SpecialItem(
    special: ru.feryafox.kavita4j.models.responses.series.SpecialsItem,
    specialCoverUrl: String,
    onDownload: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Special cover
            AsyncImage(
                model = if (specialCoverUrl.isNotBlank()) specialCoverUrl else null,
                contentDescription = "Обложка специального выпуска",
                modifier = Modifier
                    .width(50.dp)
                    .height(75.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Special info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = special.titleName,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Страниц: ${special.pages}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (special.pagesRead > 0) {
                    Text(
                        text = "Прочитано: ${special.pagesRead}/${special.pages}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Download button
            IconButton(
                onClick = onDownload
            ) {
                Icon(
                    Icons.Default.Download,
                    contentDescription = "Скачать",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

