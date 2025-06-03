package ru.feryafox.kavify.kavify.settings

import android.util.Log
import ru.feryafox.kavify.kavify.KAVIFY_ID
import ru.feryafox.kavify.kavify.storages.KavifyStorage.Companion.FOO_FIELD
import ru.feryafox.yokailib.settings.base.CategorySettings
import ru.feryafox.yokailib.settings.base.OnUpdateBehavior
import ru.feryafox.yokailib.settings.defaults.StringField
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class KavifySettings @Inject constructor() : CategorySettings(
    KAVIFY_ID,
    "Kavify",
    listOf(
        StringField(
            title = "Foo",
            field = FOO_FIELD,
            isOnUpdateBehavior = OnUpdateBehavior.ON_CHANGED
        ) {
            Log.i("KavifySettings", "Foo field updated: $it")
        },
        StringField(
            title = "Bar",
            field = FOO_FIELD,
            isOnUpdateBehavior = OnUpdateBehavior.ON_SAVED
        ) {
            Log.i("KavifySettings", "Bar field updated: $it")
        }
    )
)
