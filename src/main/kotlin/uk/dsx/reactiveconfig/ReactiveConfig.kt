package uk.dsx.reactiveconfig

import uk.dsx.reactiveconfig.interfaces.ConfigSource

class ReactiveConfig(block: ReactiveConfig.() -> Unit) {
    var manager: ConfigManager = ConfigManager()
    var base: PropertyTypeBase = PropertyTypeBase(manager)

    init {
        apply(block)
    }

    infix fun <T : Any> String.of(type: PropertyTypeBase.PropertyType<T>) {
        ReloadableFactory.createReloadable(this, type, manager)
    }

    fun <T : Any> reloadable(key: String, type: PropertyTypeBase.PropertyType<T>): Reloadable<T> {
        return ReloadableFactory.createReloadable(key, type, manager)
    }

    fun addConfigSource(source: ConfigSource) {
        manager.addSource(source)
    }
}