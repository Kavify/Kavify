package ru.feryafox.kavify.base.settings.base

abstract class CategorySettings(
    val id: String,
    val title: String?,
    val fields: List<SettingField<*>>,
    val onSaved: () -> Unit = {}
) {
    @Suppress("UNCHECKED_CAST")
    fun save() {
        fields.forEach {
            if (it.isUpdated || it.isOnUpdateBehavior == OnUpdateBehavior.ON_SAVED) (it.onUpdate as (Any?) -> Unit).invoke(it.field.field)
            onSaved()
        }
    }

    fun get(key: String): Any? = fields.firstOrNull { it.field.key == key }?.field?.field
}
