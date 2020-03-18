package uk.dsx.reactiveconfig

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger {}

object ReloadableFactory {
    fun <T : Any> createReloadable(
        key: String,
        type: PropertyTypeBase.PropertyType<T>,
        map: ConcurrentHashMap<String, Reloadable<*>>,
        flowOfChanges: Flow<RawProperty>,
        scope: CoroutineScope
    ): Reloadable<T> {
        return map[key] as Reloadable<T>? ?: Reloadable(type.initial,
            flowOfChanges.filter { rawProperty: RawProperty ->
                rawProperty.key == key
            }.mapNotNull { rawProperty: RawProperty ->
                type.parse(rawProperty.value).also { result: T? ->
                    result ?: logger.error("Wrong type of property: $key")
                }
            }, scope
        ).also { map[key] = it }
    }
}