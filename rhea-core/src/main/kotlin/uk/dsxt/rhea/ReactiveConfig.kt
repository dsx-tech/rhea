package uk.dsxt.rhea

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.EmptyCoroutineContext

/**
 * [ReactiveConfig] manages configuration.
 *
 * **Note: use [Builder] to build instances of this class.
 */
class ReactiveConfig private constructor(
    private val mapOfSources: MutableMap<String, ConfigSource>,
    private val configScope: CoroutineScope,
    private val channelOfChanges: BroadcastChannel<RawProperty>
) {
    private val flowOfChanges: Flow<RawProperty> = channelOfChanges.asFlow()
    private val mapOfProperties: MutableMap<String, Reloadable<*>> = ConcurrentHashMap()

    companion object {
        val reactiveConfigLogger = KotlinLogging.logger {}
    }

    /**
     * Builder for [ReactiveConfig].
     */
    class Builder {
        private val mapOfSources: MutableMap<String, ConfigSource> = ConcurrentHashMap()
        private val configScope = CoroutineScope(EmptyCoroutineContext)
        private val channelOfChanges: BroadcastChannel<RawProperty> = BroadcastChannel(Channel.BUFFERED)

        /**
         * Adds configuration [source] in [ReactiveConfig]s built.
         *
         * @param source the configuration source to add
         * @param name human-readable name of configuration source
         * @return this instance of builder with provided source added
         */
        fun addSource(name: String, source: ConfigSource): Builder {
            return apply {
                mapOfSources[name] = source
                configScope.launch {
                    source.subscribe(channelOfChanges, configScope)
                }
                Thread.sleep(100)
            }
        }

        /**
         * Builds a [ReactiveConfig] using this builder
         *
         * @return new instance of [ReactiveConfig]
         */
        fun build(): ReactiveConfig {
            return ReactiveConfig(mapOfSources, configScope, channelOfChanges)
        }
    }

    /**
     * @return [Reloadable] that holds the freshest value of property with given [key] and [type].
     */
    operator fun <T> get(key: String, type: PropertyType<T>): Reloadable<T>? {
        if (mapOfProperties.containsKey(key)) {
            with(mapOfProperties[key]) {
                return this as Reloadable<T>
            }
        } else {
            synchronized(this) {
                if (!mapOfProperties.containsKey(key)) {
                    var isSet = false
                    var initialValue: T = type.initial

                    for (source in mapOfSources.values) {
                        with(type.parse(source.getNode(key))) {
                            when (this) {
                                is ParseResult.Success -> {
                                    initialValue = this.value as T
                                    isSet = true
                                }
                                is ParseResult.Failure -> reactiveConfigLogger.error("Invalid value of property with key=$key")
                            }
                        }

                        if (isSet) break
                    }

                    if (isSet) {
                        return Reloadable(
                            initialValue,
                            flowOfChanges
                                .filter { rawProperty: RawProperty ->
                                    rawProperty.key == key
                                }
                                .map { rawProperty: RawProperty ->
                                    type.parse(rawProperty.value).let {
                                        when (it) {
                                            is ParseResult.Success -> it.value
                                            is ParseResult.Failure -> reactiveConfigLogger.error("Invalid value of property with key=$key")
                                        }
                                    }
                                }
                                .filter {
                                    it !is Unit
                                }
                                .map {
                                    it as T
                                },
                            configScope
                        ).also {
                            mapOfProperties[key] = it
                        }
                    } else {
                        reactiveConfigLogger.error("Couldn't find property with key=$key in any config sources")
                        return null
                    }
                } else {
                    with(mapOfProperties[key]) {
                        return this as Reloadable<T>
                    }
                }
            }
        }
    }

    operator fun <T> get(pair: Pair<PropertyType<T>, String>) = get(pair.second, pair.first)
}