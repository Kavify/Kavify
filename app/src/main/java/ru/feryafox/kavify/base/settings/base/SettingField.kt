package ru.feryafox.kavify.base.settings.base

import androidx.compose.runtime.Composable

interface SettingField<T> {
    val key: String
    val title: String
    var field: T
    fun getFieldString(): String = field.toString()
    fun loadField(value: String)
    val component: @Composable (T) -> Unit
    val onUpdate: () -> Unit
    val isOnUpdateBehavior: OnUpdateBehavior
    val isUpdated: Boolean
}

enum class OnUpdateBehavior {
    // onUpdate вызывается в любом случае, при выходе с настроек
    ON_SAVED,

    // onUpdate вызывается только, если isUpdated = true
    ON_CHANGED
}