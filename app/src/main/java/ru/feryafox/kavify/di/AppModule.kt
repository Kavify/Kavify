package ru.feryafox.kavify.di

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.feryafox.kavify.data.repositories.KavitaRepository
import ru.feryafox.kavita4j.Kavita4J
import ru.feryafox.kavita4j.components.Kavita4JAuth.Kavita4JAuthCredentials
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    @Singleton
    fun provideSharedPrefs(
        @ApplicationContext context: Context
    ): SharedPreferences {
        return context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideKavitaClient(
        prefs: SharedPreferences
    ): Kavita4J {
        val server = prefs.getString("base_url", "") ?: ""

        val kavita4JCredential = Kavita4JAuthCredentials(
            prefs.getString("username", null),
            prefs.getString("password", null),
            prefs.getString("api_key", null),
            prefs.getString("access_token", null),
            prefs.getString("refresh_token", null)
        )

        val kavita4J = Kavita4J(server)
        kavita4J.auth().loadCredentials(kavita4JCredential)

        return kavita4J
    }

    @Provides
    @Singleton
    fun provideKavitaRepository(
        client: Kavita4J
    ): KavitaRepository = KavitaRepository(client)
}