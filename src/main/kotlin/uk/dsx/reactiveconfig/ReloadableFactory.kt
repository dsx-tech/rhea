package uk.dsx.reactiveconfig

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import mu.KotlinLogging
import uk.dsx.reactiveconfig.interfaces.ConfigSource
import java.util.concurrent.atomic.AtomicBoolean

private val logger = KotlinLogging.logger {}

object ReloadableFactory {

    fun <T> createReloadable(
        key: String,
        isValueSet: AtomicBoolean,
        type: PropertyType<T>,
        mapOfProperties: MutableMap<String, Reloadable<*>>,
        mapOfSources: MutableMap<String, ConfigSource>,
        flowOfChanges: Flow<RawProperty>,
        scope: CoroutineScope
    ): Reloadable<T> {
        return mapOfProperties[key] as Reloadable<T>? ?: synchronized(this) {
            if (!mapOfProperties.containsKey(key)) {
                Reloadable(key,
                    type.initial,
                    flowOfChanges
                        .filter { rawProperty: RawProperty ->
                            rawProperty.key == key
                        }
                        .map { rawProperty: RawProperty ->
                            type.parse(rawProperty.value).let {
                                when (it) {
                                    is ParseResult.Success -> it.value
                                    is ParseResult.Failure -> logger.error("Wrong type of property: $key")
                                }
                            }
                        }
                        .map {
                            it as T
                        },
                    type,
                    mapOfProperties,
                    mapOfSources,
                    flowOfChanges,
                    scope,
                    isValueSet
                ).also {
                    mapOfProperties[key] = it
                }
            } else {
                mapOfProperties[key] as Reloadable<T>
            }
        }
    }
}