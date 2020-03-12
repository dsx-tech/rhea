package uk.dsx.reactiveconfig

import uk.dsx.reactiveconfig.interfaces.ConfigSource
import kotlin.reflect.KProperty

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

class PropertyTypeBase(val configManager: ConfigManager) {

    inner class PropertyType<T : Any>(val initial: T, val parse: (String) -> T?) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Reloadable<T> {
            return ReloadableFactory.createReloadable(property.name, this, configManager)
        }
    }

    val stringType = PropertyType("", { it })

    val intType = PropertyType(0, String::toIntOrNull)

    val longType = PropertyType(0, String::toLongOrNull)

    val floatType = PropertyType(0.0F, String::toFloatOrNull)

    val doubleType = PropertyType(0.0, String::toDoubleOrNull)

    val booleanType = PropertyType(false, String::toBoolean)
}