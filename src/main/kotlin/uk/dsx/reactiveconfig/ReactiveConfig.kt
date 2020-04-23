package uk.dsx.reactiveconfig

import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import mu.KotlinLogging
import uk.dsx.reactiveconfig.interfaces.ConfigSource
import kotlin.reflect.KProperty

class ReactiveConfig private constructor(val manager: ConfigManager) {
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

    inline operator fun <reified T> get(key: String, type: PropertyType<T>): Reloadable<T>? {
        if (manager.mapOfProperties.containsKey(key)) {
            with(manager.mapOfProperties[key]) {
                return if (this!!.get() is T) {
                    this as Reloadable<T>
                } else {
                    reactiveConfigLogger.error("Specified the wrong type of reloadable with key=$key: its value is not ${T::class.simpleName}")
                    null
                }
            }
        } else {
            synchronized(this) {
                if (!manager.mapOfProperties.containsKey(key)) {
                    var isSet = false
                    var initialValue: T = type.initial

                    for (source in manager.mapOfSources.values) {
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
                            manager.flowOfChanges
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
                            manager.configScope
                        ).also {
                            manager.mapOfProperties[key] = it
                        }
                    } else {
                        reactiveConfigLogger.error("Couldn't find property with key=$key in any config sources")
                        return null
                    }
                } else {
                    with(manager.mapOfProperties[key]) {
                        return if (this!!.get() is T) {
                            this as Reloadable<T>
                        } else {
                            reactiveConfigLogger.error("Specified the wrong type of reloadable with key=$key: its value is not ${T::class.simpleName}")
                            null
                        }
                    }
                }
            }
        }
    }

    inline operator fun <reified T> get(pair: Pair<PropertyType<T>, String>) = get(pair.second, pair.first)
}