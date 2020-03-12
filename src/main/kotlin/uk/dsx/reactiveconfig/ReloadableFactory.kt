package uk.dsx.reactiveconfig

import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

object ReloadableFactory {
    fun <T : Any> createReloadable(key: String, type: PropertyTypeBase.PropertyType<T>, manager: ConfigManager): Reloadable<T> {
        return manager.properties[key] as Reloadable<T>? ?: Reloadable(type.initial,
            manager.flowOfChanges.filter { rawProperty: RawProperty ->
                rawProperty.key == key
            }.mapNotNull { rawProperty: RawProperty ->
                type.parse(rawProperty.value).also { result: T? ->
                    result ?: logger.error("Wrong type of property: $key")
                }
            }, manager.configScope
        ).also { manager.properties[key] = it }
    }
}