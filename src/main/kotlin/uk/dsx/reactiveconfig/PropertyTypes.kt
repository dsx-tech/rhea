package uk.dsx.reactiveconfig

import kotlin.reflect.KProperty

class PropertyType<T : Any>(val initial: T, val parse: (String) -> T?) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): Reloadable<T> {
        return ReloadableFactory.createReloadable(property.name, this)
    }
}

val StringType = PropertyType("", { it })

val IntType = PropertyType(0, String::toIntOrNull)

val LongType = PropertyType(0, String::toLongOrNull)

val FloatType = PropertyType(0.0F, String::toFloatOrNull)

val DoubleType = PropertyType(0.0, String::toDoubleOrNull)

val BooleanType = PropertyType(false, String::toBoolean)