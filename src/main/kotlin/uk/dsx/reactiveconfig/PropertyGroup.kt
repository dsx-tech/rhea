package uk.dsx.reactiveconfig

import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KProperty

open class PropertyGroup {
    var keyList = ConcurrentHashMap<String, PropertyType<*>>()
    private val name = name()

    private fun outer(): String? {
        var classPointer = this::class.java.enclosingClass?.kotlin?.objectInstance as? PropertyGroup
        var classPath = ""
        while (classPointer != null) {
            classPath = classPointer::class.simpleName + "." + classPath
            classPointer = classPointer::class.java.enclosingClass?.kotlin?.objectInstance as? PropertyGroup
        }
        return classPath
    }

    private fun groupName() = javaClass.kotlin.simpleName?.substringBefore("$")?: "-"

    private fun name(): String = outer() + groupName()

    operator fun <T> PropertyType<T>.getValue(group: PropertyGroup, property: KProperty<*>) : Pair<PropertyType<T>, String> {
        return Pair(this, name() + "." + property.name)
    }
}