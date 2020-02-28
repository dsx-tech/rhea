package uk.dsx.reactiveconfig

import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class ReloadableFactory {

    companion object {
        fun <T : Any> createReloadable(key: String, type: PropertyType<T>): Reloadable<T> {

            return ConfigManager.properties[key] as Reloadable<T>? ?: Reloadable(type.initial,
                ConfigManager.flowOfChanges.filter { rawProperty: RawProperty ->
                    rawProperty.key == key
                }.mapNotNull { rawProperty: RawProperty ->
                    type.parse(rawProperty.value).also { result: T? ->
                        result ?: logger.error("Wrong type of property: $key")
                    }
                }).also { ConfigManager.properties[key] = it }
        }
    }
}