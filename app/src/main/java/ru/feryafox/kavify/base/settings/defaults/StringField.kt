package ru.feryafox.kavify.base.settings.defaults

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import ru.feryafox.kavify.base.settings.base.OnUpdateBehavior
import ru.feryafox.kavify.base.settings.base.SettingField

class StringField(
    override val key: String,
    override val title: String = "",
    private var _field: String,
    override val isOnUpdateBehavior: OnUpdateBehavior,
    override val onUpdate: () -> Unit
) : SettingField<String> {
    private var _isUpdated = false

    override var field: String
        get() = _field
        set(value) {
            _field = value
        }

    override fun loadField(value: String) {
        field = value
    }

    override val isUpdated: Boolean
        get() = _isUpdated

    override val component: @Composable (String) -> Unit = { value ->
        var text by remember { mutableStateOf(value) }

        val keyboardController = LocalSoftwareKeyboardController.current

        TextField(
            value = text,
            onValueChange = { newText ->
                text = newText
                field = newText
            },
            label = { Text(title) },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                }
            )
        )
    }
}
