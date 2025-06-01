package ru.feryafox.kavify.base.settings.base

import androidx.compose.runtime.Composable
import ru.feryafox.kavify.base.storages.base.StorageField

interface SettingField<T> {
    val title: String
    val field: StorageField<*>
    val component: @Composable (T) -> Unit
    val onUpdate: (T) -> Unit
    val isOnUpdateBehavior: OnUpdateBehavior
    val isUpdated: Boolean
}

enum class OnUpdateBehavior {
    // onUpdate вызывается в любом случае, при выходе с настроек
    ON_SAVED,

    // onUpdate вызывается только, если isUpdated = true
    ON_CHANGED
}