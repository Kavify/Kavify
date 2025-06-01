package ru.feryafox.kavify.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import ru.feryafox.kavify.base.Category
import ru.feryafox.kavify.base.settings.base.CategorySettings
import ru.feryafox.kavify.base.storages.base.BaseStorage
import ru.feryafox.kavify.ext.kavita.KavitaCategory
import ru.feryafox.kavify.root.settings.KavifySettings
import ru.feryafox.kavify.root.storages.KavifyStorage

@Module
@InstallIn(SingletonComponent::class)
class CategoriesModule {
    @Provides
    @IntoSet
    fun provideKavita(impl: KavitaCategory): Category = impl

    @Provides
    @IntoSet
    fun provideKavifySettings(impl: KavifySettings): CategorySettings = impl

    @Provides
    @IntoSet
    fun provideKavifyStorage(impl: KavifyStorage): BaseStorage = impl
}