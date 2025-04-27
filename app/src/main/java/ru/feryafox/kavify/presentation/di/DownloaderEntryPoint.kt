package ru.feryafox.kavify.presentation.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.feryafox.kavify.domain.servicies.Kavita4JDownloader

@EntryPoint
@InstallIn(SingletonComponent::class)
interface DownloaderEntryPoint {
    fun kavitaDownloader(): Kavita4JDownloader
}
