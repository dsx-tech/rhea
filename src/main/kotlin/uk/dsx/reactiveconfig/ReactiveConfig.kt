package uk.dsx.reactiveconfig

import mu.KotlinLogging
import uk.dsx.reactiveconfig.interfaces.ConfigSource
import java.util.concurrent.atomic.AtomicBoolean

class ReactiveConfig private constructor(
    private val manager: ConfigManager,
    val properties: MutableMap<String, Reloadable<*>>
) {
    val logger = KotlinLogging.logger {}

    class Builder {
        private val manager: ConfigManager = ConfigManager()
        private val propertyCreationFunctions: MutableList<() -> Unit> = mutableListOf()

        fun <T> addProperty(key: String, type: PropertyType<T>): Builder {
            return apply {
                propertyCreationFunctions.add {
                    ReloadableFactory.createReloadable(
                        key,
                        AtomicBoolean(false),
                        type,
                        manager.mapOfProperties,
                        manager.mapOfSources,
                        manager.flowOfChanges,
                        manager.configScope
                    )
                }
            }
        }

        fun addSource(name: String, source: ConfigSource): Builder {
            return apply {
                manager.mapOfSources[name] = source
                manager.addSource(source)
            }
        }

        fun build(): ReactiveConfig {
            for (creation in propertyCreationFunctions) {
                creation()
            }

            return ReactiveConfig(manager, manager.mapOfProperties)
        }
    }

    fun <T> reloadable(key: String, type: PropertyType<T>): Reloadable<T> {
        return ReloadableFactory.createReloadable(
            key,
            AtomicBoolean(false),
            type,
            properties,
            manager.mapOfSources,
            manager.flowOfChanges,
            manager.configScope
        )
    }

    inline fun <reified T> getReloadable(key: String): Reloadable<T>? {
        if (properties.containsKey(key)) {
            with(properties[key]) {
                return if (this!!.get() is T) {
                    this as Reloadable<T>
                } else {
                    logger.error("You specified the wrong type of reloadable with key='$key' in method getReloadable: its value is not ${T::class.simpleName}")
                    null
                }
            }
        } else {
            logger.error("Reloadable with key='$key' doesn't exist")
            return null
        }
    }

    inline fun <reified T> getReloadable(key: String, type: PropertyType<T>): Reloadable<T>? {
        return getReloadable(key)
    }
}