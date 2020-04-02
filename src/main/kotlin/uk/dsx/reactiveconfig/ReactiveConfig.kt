package uk.dsx.reactiveconfig

import mu.KotlinLogging
import uk.dsx.reactiveconfig.interfaces.ConfigSource

class ReactiveConfig(block: ReactiveConfig.() -> Unit) {
    private var manager: ConfigManager = ConfigManager()
    val properties = manager.mapOfProperties

    val logger = KotlinLogging.logger {}

    init {
        apply(block)
    }

    infix fun <T> String.of(type: PropertyType<T>) {
        ReloadableFactory.createReloadable(
            this,
            type,
            properties,
            manager.mapOfSources,
            manager.flowOfChanges,
            manager.configScope
        )
    }

    fun <T> reloadable(key: String, type: PropertyType<T>): Reloadable<T> {
        return ReloadableFactory.createReloadable(
            key,
            type,
            properties,
            manager.mapOfSources,
            manager.flowOfChanges,
            manager.configScope
        )
    }

    fun addConfigSource(name: String, source: ConfigSource) {
        manager.mapOfSources[name] = source
        manager.addSource(source)
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