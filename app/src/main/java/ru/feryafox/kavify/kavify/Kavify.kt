package ru.feryafox.kavify.kavify


import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import ru.feryafox.kavify.kavita.KavitaCategory
import ru.feryafox.kavify.kavify.settings.KavifySettings
import ru.feryafox.kavify.kavify.storages.KavifyStorage
import ru.feryafox.yokailib.categories.Category
import ru.feryafox.yokailib.settings.base.CategorySettings
import ru.feryafox.yokailib.storages.base.BaseStorage

const val KAVIFY_ID = "kavify"

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
