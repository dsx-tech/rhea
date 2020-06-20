package uk.dsxt.rhea

import kotlin.reflect.KProperty

/**
 * Allows to define hierarchies of properties.
 */
open class PropertyGroup {
    private fun outer(): String? {
        var classPointer = this::class.java.enclosingClass?.kotlin?.objectInstance as? PropertyGroup
        var classPath = ""
        while (classPointer != null) {
            classPath = classPointer::class.simpleName + "." + classPath
            classPointer = classPointer::class.java.enclosingClass?.kotlin?.objectInstance as? PropertyGroup
        }
        return classPath
    }

    private val group = javaClass.kotlin.simpleName

    private fun groupName() = group?.substringBefore("$") ?: reactiveConfigLogger.error("Unexpected name $group")

    private fun name(): String = outer() + groupName()

    operator fun <T> PropertyType<T>.getValue(group: PropertyGroup, property: KProperty<*>) =
        Pair(this, name() + "." + property.name)
}