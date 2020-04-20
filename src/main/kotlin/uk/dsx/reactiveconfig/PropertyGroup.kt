package uk.dsx.reactiveconfig

import kotlin.reflect.KProperty

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

    private fun groupName() = javaClass.kotlin.simpleName?.substringBefore("$") ?:
        throw IllegalArgumentException("Unexpected name of PropertyGroup")

    private fun name(): String = outer() + groupName()

    operator fun <T> PropertyType<T>.getValue(group: PropertyGroup, property: KProperty<*>) =
        Pair(this, name() + "." + property.name)
}