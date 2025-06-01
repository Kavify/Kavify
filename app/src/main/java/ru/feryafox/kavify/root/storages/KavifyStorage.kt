package ru.feryafox.kavify.root.storages

import ru.feryafox.kavify.base.storages.base.BaseStorage
import ru.feryafox.kavify.base.storages.defaultstorages.preferences.StringStorageField
import ru.feryafox.kavify.root.KAVIFY_ID
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
