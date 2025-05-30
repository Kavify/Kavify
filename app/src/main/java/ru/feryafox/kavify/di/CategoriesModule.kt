package ru.feryafox.kavify.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import ru.feryafox.kavify.base.Category
import ru.feryafox.kavify.ext.kavita.KavitaCategory

@Module
@InstallIn(SingletonComponent::class)
class CategoriesModule {
    @Provides
    @IntoSet
    fun provideKavita(impl: KavitaCategory): Category = impl
}