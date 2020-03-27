package uk.dsx.reactiveconfig

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

object ReloadableFactory {
    fun <T> createReloadable(
        key: String,
        type: PropertyTypeBase.PropertyType<T>,
        map: MutableMap<String, Reloadable<*>>,
        flowOfChanges: Flow<RawProperty>,
        scope: CoroutineScope
    ): Reloadable<T> {
        return map[key] as Reloadable<T>? ?: Reloadable(type.initial,
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
                }, scope
        ).also {
            map[key] = it
        }
    }
}