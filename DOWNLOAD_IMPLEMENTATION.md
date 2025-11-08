# Download Implementation Guide

## Overview
This document explains how file downloads are implemented in the Kavify app using the Kavita4J library.

## Java API Reference (from Kavita4J)

The `Kavita4JDownload` class provides the following methods for downloading content:

### Download Binary Data

```java
// Download a volume
public BinaryResponse volume(int volumeId) {
    return client.getBinaryAuth(
        RequestOptions.builder()
            .addQueryParam("volumeId", String.valueOf(volumeId))
            .build(),
        "api",
        "Download",
        "volume"
    );
}

// Download a chapter
public BinaryResponse chapter(int chapterId) {
    return client.getBinaryAuth(
        RequestOptions.builder()
            .addQueryParam("chapterId", String.valueOf(chapterId))
            .build(),
        "api",
        "Download",
        "chapter"
    );
}

// Download a series
public BinaryResponse series(int seriesId) {
    return client.getBinaryAuth(
        RequestOptions.builder()
            .addQueryParam("seriesId", String.valueOf(seriesId))
            .build(),
        "api",
        "Download",
        "series"
    );
}
```

### Get Download Links (URL only)

```java
public UrlResponse volumeLink(int volumeId) {
    return UrlResponse.from(
        baseUrlBuilder()
            .addPathSegment("volume")
            .addQueryParameter("volumeId", String.valueOf(volumeId))
            .build()
    );
}

public UrlResponse chapterLink(int chapterId) {
    return UrlResponse.from(
        baseUrlBuilder()
            .addPathSegment("chapter")
            .addQueryParameter("chapterId", String.valueOf(chapterId))
            .build()
    );
}

public UrlResponse seriesLink(int seriesId) {
    return UrlResponse.from(
        baseUrlBuilder()
            .addPathSegment("series")
            .addQueryParameter("seriesId", String.valueOf(seriesId))
            .build()
    );
}
```

### Get Download Size

```java
public HttpClientResponse<Size> volumeSize(int volumeId) {
    return client.getAuth(
        Size.class,
        RequestOptions.builder()
            .addQueryParam("volumeId", String.valueOf(volumeId))
            .build(),
        "api",
        "Download",
        "volume-size"
    );
}

public HttpClientResponse<Size> chapterSize(int chapterId) {
    return client.getAuth(
        Size.class,
        RequestOptions.builder()
            .addQueryParam("chapterId", String.valueOf(chapterId))
            .build(),
        "api",
        "Download",
        "chapter-size"
    );
}

public HttpClientResponse<Size> seriesSize(int seriesId) {
    return client.getAuth(
        Size.class,
        RequestOptions.builder()
            .addQueryParam("seriesId", String.valueOf(seriesId))
            .build(),
        "api",
        "Download",
        "series-size"
    );
}
```

## Kotlin Implementation in Kavify

### KavitaRepository.kt

The repository provides suspend functions that wrap the Java API calls:

```kotlin
// Download binary data
suspend fun downloadVolume(volumeId: Int): BinaryResponse = 
    withContext(Dispatchers.IO) { client.download().volume(volumeId) }

suspend fun downloadChapter(chapterId: Int): BinaryResponse = 
    withContext(Dispatchers.IO) { client.download().chapter(chapterId) }

suspend fun downloadSeries(seriesId: Int): BinaryResponse = 
    withContext(Dispatchers.IO) { client.download().series(seriesId) }

// Get download URLs
suspend fun getVolumeDownloadUrl(volumeId: Int): UrlResponse = 
    withContext(Dispatchers.IO) { client.download().volumeLink(volumeId) }

suspend fun getChapterDownloadUrl(chapterId: Int): UrlResponse = 
    withContext(Dispatchers.IO) { client.download().chapterLink(chapterId) }

suspend fun getSeriesDownloadUrl(seriesId: Int): UrlResponse = 
    withContext(Dispatchers.IO) { client.download().seriesLink(seriesId) }
```

### DownloadService.kt

The `DownloadService` uses these repository methods to download files:

1. **Start Download**: The service receives a download request with item ID and type
2. **Fetch Binary Data**: Calls the appropriate repository method based on download type
   ```kotlin
   val binaryResponse = when (type) {
       DownloadType.VOLUME -> repository.downloadVolume(itemId)
       DownloadType.CHAPTER -> repository.downloadChapter(itemId)
       DownloadType.SERIES -> repository.downloadSeries(itemId)
   }
   ```
3. **Process Response**: The `BinaryResponse` contains:
   - `data`: The binary content (as InputStream or ByteArray)
   - `filename`: The suggested filename from the server
4. **Ensure Correct File Extension**: The service automatically ensures files have the correct extension:
   ```kotlin
   val filename = ensureFileExtension(
       binaryResponse.filename ?: extractFilename(null, title, type), 
       type
   )
   ```
   - Volumes and Chapters get `.cbz` extension
   - Series get `.zip` extension
   - Wrong extensions are replaced with correct ones
5. **Set Correct MIME Type**: Uses appropriate MIME type for file creation:
   ```kotlin
   val mimeType = getMimeTypeForDownloadType(type)
   // VOLUME/CHAPTER: "application/vnd.comicbook+zip"
   // SERIES: "application/zip"
   ```
6. **Save to File**: The service saves the data to the user-selected directory using Android's SAF (Storage Access Framework)

## BinaryResponse Structure

The `BinaryResponse` object returned from download methods contains:
- **data**: The actual file content (can be `InputStream` or `ByteArray`)
- **filename**: The filename suggested by the server (extracted from Content-Disposition header)

## Usage Example

To download a volume:

```kotlin
// In a coroutine scope
val downloadId = DownloadService.startDownload(
    context = context,
    itemId = volumeId,
    title = "My Volume",
    type = DownloadType.VOLUME,
    directoryUri = userSelectedDirectoryUri
)
```

The service will:
1. Call `repository.downloadVolume(volumeId)`
2. Receive a `BinaryResponse` with the file data
3. Extract the input stream from the response
4. Save it to a file in the selected directory
5. Show progress notifications during the download
6. Show completion notification when done

## File Formats

- **Volume**: `.cbz` (Comic Book Archive)
  - MIME type: `application/vnd.comicbook+zip`
  - Contains images of volume pages
- **Chapter**: `.cbz` (Comic Book Archive)
  - MIME type: `application/vnd.comicbook+zip`
  - Contains images of chapter pages
- **Series**: `.zip` (ZIP archive containing all volumes/chapters)
  - MIME type: `application/zip`
  - Contains multiple CBZ files

### File Extension Handling

The service automatically ensures correct file extensions:

1. **Server provides filename**: If the server sends a filename (e.g., "MyVolume.zip"), the service checks and corrects the extension if needed (→ "MyVolume.cbz" for volumes)
2. **No filename from server**: Generates a default name with correct extension (e.g., "Title_volume.cbz")
3. **Extension correction**: Automatically replaces wrong extensions with correct ones
4. **MIME type matching**: Uses proper MIME types for better Android compatibility

See `FILE_EXTENSIONS_GUIDE.md` for detailed examples and implementation details.

## API Endpoints

The downloads are made to these Kavita API endpoints:
- Volume: `api/Download/volume?volumeId={id}`
- Chapter: `api/Download/chapter?chapterId={id}`
- Series: `api/Download/series?seriesId={id}`

