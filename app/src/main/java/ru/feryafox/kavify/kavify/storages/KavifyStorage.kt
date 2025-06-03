package ru.feryafox.kavify.kavify.storages

import ru.feryafox.kavify.kavify.KAVIFY_ID
import ru.feryafox.yokailib.storages.base.BaseStorage
import ru.feryafox.yokailib.storages.defaultstorages.preferences.StringStorageField
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class KavifyStorage @Inject constructor() : BaseStorage(
    id = KAVIFY_ID,
    keys = listOf(FOO_FIELD)
) {
    companion object {
        val FOO_FIELD = StringStorageField(
            id  = KAVIFY_ID,
            key = "foo",
            initValue = "Bar"
        )
    }
}
