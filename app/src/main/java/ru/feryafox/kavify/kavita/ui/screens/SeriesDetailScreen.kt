package ru.feryafox.kavify.kavita.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import ru.feryafox.kavify.kavita.getImageSeries
import ru.feryafox.kavify.kavita.viewmodels.SeriesDetailUiState
import ru.feryafox.kavify.kavita.viewmodels.SeriesDetailViewModel
import ru.feryafox.kavita4j.models.responses.series.SeriesDetail
import ru.feryafox.kavita4j.models.responses.series.VolumesItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeriesDetailScreen(
    seriesId: Int,
    viewModel: SeriesDetailViewModel = hiltViewModel(),
    baseUrl: String,
    apiKey: String,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(seriesId) {
        viewModel.loadSeriesDetail(seriesId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Детали серии") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is SeriesDetailUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is SeriesDetailUiState.Success -> {
                    SeriesDetailContent(
                        seriesDetail = state.seriesDetail,
                        baseUrl = baseUrl,
                        apiKey = apiKey,
                        onDownloadVolume = { volumeId ->
                            viewModel.downloadVolume(volumeId)
                        }
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
    seriesDetail: SeriesDetail,
    baseUrl: String,
    apiKey: String,
    onDownloadVolume: (Int) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Series header with cover and info
        item {
            SeriesHeader(seriesDetail, baseUrl, apiKey)
        }

        // Volumes section
        if (seriesDetail.volumes.isNotEmpty()) {
            item {
                Text(
                    text = "Тома (${seriesDetail.volumes.size})",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            items(seriesDetail.volumes) { volume ->
                VolumeItem(
                    volume = volume,
                    baseUrl = baseUrl,
                    apiKey = apiKey,
                    onDownload = { onDownloadVolume(volume.id) }
                )
            }
        }

        // Chapters section if no volumes
        if (seriesDetail.volumes.isEmpty() && seriesDetail.chapters.isNotEmpty()) {
            item {
                Text(
                    text = "Главы (${seriesDetail.chapters.size})",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}

@Composable
private fun SeriesHeader(
    seriesDetail: SeriesDetail,
    baseUrl: String,
    apiKey: String
) {
    // Get first volume for series ID
    val seriesId = seriesDetail.volumes.firstOrNull()?.seriesId ?: 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row {
                // Cover image
                AsyncImage(
                    model = getImageSeries(baseUrl, seriesId, apiKey),
                    contentDescription = "Обложка",
                    modifier = Modifier
                        .width(120.dp)
                        .height(180.dp),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Basic info
                Column {
                    Text(
                        text = seriesDetail.volumes.firstOrNull()?.name ?: "Серия",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Statistics
                    InfoRow("Томов:", "${seriesDetail.volumes.size}")
                    InfoRow("Глав:", "${seriesDetail.chapters.size}")
                    InfoRow("Всего страниц:", "${seriesDetail.totalCount}")
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
    baseUrl: String,
    apiKey: String,
    onDownload: () -> Unit
) {
    val context = LocalContext.current

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
                model = volume.coverImage?.let {
                    "$baseUrl/api/image/volume-cover?volumeId=${volume.id}&apiKey=$apiKey"
                } ?: getImageSeries(baseUrl, volume.seriesId, apiKey),
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
                onClick = {
                    val downloadUrl = "$baseUrl/api/download/volume?volumeId=${volume.id}&apiKey=$apiKey"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
                    context.startActivity(intent)
                }
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
