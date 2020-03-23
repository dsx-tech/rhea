package uk.dsx.reactiveconfig

import java.util.concurrent.ConcurrentHashMap

open class PropertyGroup() {
    val classRef = this
    var keyList = ConcurrentHashMap<String, PropertyTypeBase.PropertyType<*>>()
    private val name = name()
    var manager: ConfigManager = ConfigManager()
    var base: PropertyTypeBase = PropertyTypeBase(manager.properties, manager.flowOfChanges, manager.configScope)

    private fun outer(): String? {
        var classPointer = this::class.java.enclosingClass?.kotlin?.objectInstance as? PropertyGroup
        var classPath = ""
        while (classPointer != null) {
            classPath = classPointer::class.simpleName + "." + classPath
            classPointer = classPointer::class.java.enclosingClass?.kotlin?.objectInstance as? PropertyGroup
        }
        return classPath
    }

    private fun groupName() = javaClass.kotlin.simpleName?.substringBefore("$")
        ?: throw IllegalArgumentException("cannot determine name of property group")
    private fun name(): String = outer() + groupName()

    infix fun <T : Any> String.of(type: PropertyTypeBase.PropertyType<T>) {
        val enclose = classRef::class.java.enclosingClass?.kotlin?.objectInstance as? PropertyGroup
        keyList["$name.$this"] = type
        if (enclose != null) {
            enclose.keyList["$name.$this"] = type
        }
    }
}