package uk.dsx.reactiveconfig

import uk.dsx.reactiveconfig.interfaces.ConfigSource

class ReactiveConfig(block: ReactiveConfig.() -> Unit) {
    var manager: ConfigManager = ConfigManager()
    var base: PropertyTypeBase = PropertyTypeBase(manager.properties, manager.flowOfChanges, manager.configScope)

    init {
        apply(block)
    }

    infix fun <T : Any> String.of(type: PropertyTypeBase.PropertyType<T>) {
        ReloadableFactory.createReloadable(this, type, manager.properties, manager.flowOfChanges, manager.configScope)
    }

    fun <T : Any> reloadable(key: String, type: PropertyTypeBase.PropertyType<T>): Reloadable<T> {
        return ReloadableFactory.createReloadable(key, type, manager.properties, manager.flowOfChanges, manager.configScope)
    }

    fun addConfigSource(source: ConfigSource) {
        manager.addSource(source)
    }

    operator fun get(key: String) = manager.properties[key]?.get()

    fun register(group : PropertyGroup){
        for (line in group.keyList) {
            ReloadableFactory.createReloadable(line.key, line.value, manager.properties, manager.flowOfChanges, manager.configScope)
        }
    }
}