package ru.feryafox.kavify.kavify.storages

import ru.feryafox.kavify.kavify.KAVIFY_ID
import ru.feryafox.kavify.kavify.storages.models.Kavita4JAuthCredentialsStorage
import ru.feryafox.yokailib.storages.base.BaseStorage
import ru.feryafox.yokailib.storages.defaultstorages.preferences.*
import ru.feryafox.yokailib.storages.defaultstorages.secure.JsonSecurePreferencesStorageField

import ru.feryafox.yokailib.storages.defaultstorages.secure.StringSecureStorageField

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KavifyStorage @Inject constructor() : BaseStorage(
    id = KAVIFY_ID,
    keys = listOf(
        DOWNLOADING_DIR_FIELD,
        API_KEY_FIELD,
        USERNAME_FIELD,
        PASSWORD_FIELD,
        IS_PASSWORD_OR_TOKEN_FIELD,
        URL_FIELD,
        KAVITA_4J_AUTH_CREDENTIALS_FIELD,
    )
) {
    companion object {
        val DOWNLOADING_DIR_FIELD = DirectoryStorageField(
            id = KAVIFY_ID,
            key = "downloading_dir",
            initValue = ""
        )

        val API_KEY_FIELD = StringStorageField(
            id = KAVIFY_ID,
            key = "api_key",
            initValue = ""
        )

        val USERNAME_FIELD = StringStorageField(
            id = KAVIFY_ID,
            key = "username",
            initValue = ""
        )

        val PASSWORD_FIELD = StringSecureStorageField(
            id = KAVIFY_ID,
            key = "password",
            initValue = ""
        )

        val IS_PASSWORD_OR_TOKEN_FIELD = BooleanStorageField(
            id = KAVIFY_ID,
            key = "is_password_or_token",
            initValue = true
        )

        val URL_FIELD = UrlStorageField(
            id = KAVIFY_ID,
            key = "url",
            initValue = ""
        )

        val KAVITA_4J_AUTH_CREDENTIALS_FIELD = JsonSecurePreferencesStorageField(
            id = KAVIFY_ID,
            key = "kavita_4j_auth_credentials",
            initValue = Kavita4JAuthCredentialsStorage(),
            serializer = Kavita4JAuthCredentialsStorage.serializer()
        )
    }
}
