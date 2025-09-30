package ru.feryafox.kavify.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.feryafox.kavify.kavify.storages.KavifyStorage
import ru.feryafox.kavify.kavita.api.Kavita4JManager
import ru.feryafox.kavify.kavita.repository.KavitaRepository
import ru.feryafox.kavita4j.Kavita4J
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object KavitaModule {
    @Provides
    @Singleton
    fun provideKavitaApiManager(
        kavifyStorage: KavifyStorage,
        kavita4J: Kavita4J
    ): Kavita4JManager {
        return Kavita4JManager(kavifyStorage, kavita4J)
    }

    @Provides
    @Singleton
    fun provideKavitaRepository(
        apiManager: Kavita4JManager
    ): KavitaRepository {
        return KavitaRepository(apiManager.client)
    }
}
