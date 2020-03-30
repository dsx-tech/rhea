package uk.dsx.reactiveconfig

import uk.dsx.reactiveconfig.interfaces.ConfigSource

class ReactiveConfig(block: ReactiveConfig.() -> Unit) {
    var manager: ConfigManager = ConfigManager()

    init {
        apply(block)
    }

    infix fun <T> String.of(type: PropertyType<T>) {
        ReloadableFactory.createReloadable(
            this,
            type,
            manager.mapOfProperties,
            manager.mapOfSources,
            manager.flowOfChanges,
            manager.configScope
        )
    }

    fun <T> reloadable(key: String, type: PropertyType<T>): Reloadable<T> {
        return ReloadableFactory.createReloadable(
            key,
            type,
            manager.mapOfProperties,
            manager.mapOfSources,
            manager.flowOfChanges,
            manager.configScope
        )
    }

    fun addConfigSource(name: String, source: ConfigSource) {
        manager.mapOfSources[name] = source
        manager.addSource(source)
    }
}