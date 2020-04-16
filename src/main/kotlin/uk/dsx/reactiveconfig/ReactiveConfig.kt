package uk.dsx.reactiveconfig

import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import mu.KotlinLogging
import uk.dsx.reactiveconfig.interfaces.ConfigSource

class ReactiveConfig private constructor(val manager: ConfigManager) {
    val logger = KotlinLogging.logger {}

    class Builder {
        private val manager: ConfigManager = ConfigManager()

        fun addSource(name: String, source: ConfigSource): Builder {
            return apply {
                manager.mapOfSources[name] = source
                manager.addSource(source)
            }
        }

        fun build(): ReactiveConfig {
            return ReactiveConfig(manager)
        }
    }

    // todo: fun <T> getReloadable(key: String)

    inline fun <reified T> getReloadable(key: String, type: PropertyType<T>): Reloadable<T>? {
        if (manager.mapOfProperties.containsKey(key)) {
            with(manager.mapOfProperties[key]) {
                return if (this!!.get() is T) {
                    this as Reloadable<T>
                } else {
                    logger.error("You specified the wrong type of reloadable with key='$key' in method getReloadable: its value is not ${T::class.simpleName}")
                    null
                }
            }
        } else {
            synchronized(this) {
                if (!manager.mapOfProperties.containsKey(key)) {
                    var initialValue: T = type.initial

                    for (source in manager.mapOfSources.values) {
                        with(type.parse(source.getNode(key))) {
                            when (this) {
                                is ParseResult.Success -> {
                                    initialValue = this.value as T
                                }
                                is ParseResult.Failure -> logger.error("Wrong type of property: $key")
                            }
                        }

                        if (initialValue != type.initial) break
                    }

                    if (initialValue != type.initial) {
                        return Reloadable(
                            initialValue,
                            manager.flowOfChanges
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
                            manager.configScope
                        ).also {
                            manager.mapOfProperties[key] = it
                        }

                    } else {
                        logger.error("Couldn't find property with key=$key in any config sources")
                        return null
                    }
                } else {
                    with(manager.mapOfProperties[key]) {
                        return if (this!!.get() is T) {
                            this as Reloadable<T>
                        } else {
                            logger.error("You specified the wrong type of reloadable with key='$key' in method getReloadable: its value is not ${T::class.simpleName}")
                            null
                        }
                    }
                }
            }
        }
    }
    //operator fun get(key: String) = manager.mapOfProperties[key]?.get()

    inline operator  fun <reified T> get(pair: Pair<PropertyType<T>, String>) = getReloadable(pair.second, pair.first)?.get()

    // todo: fun <F> map(function: (T) -> (F)): Reloadable<F>
}
