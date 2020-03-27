package uk.dsx.reactiveconfig

import java.util.concurrent.ConcurrentHashMap

open class PropertyGroup {
    var keyList = ConcurrentHashMap<String, PropertyTypeBase.PropertyType<*>>()
    private val name = name()
    private var manager: ConfigManager = ConfigManager()
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

    private fun groupName() = javaClass.kotlin.simpleName?.substringBefore("$")?: "-"

    private fun name(): String = outer() + groupName()

    infix fun <T : Any> String.of(type: PropertyTypeBase.PropertyType<T>) {
        keyList["$name.$this"] = type
    }

    infix fun String.of(group: PropertyGroup) {
        for (line in group.keyList) {
            val parsed = line.key.split("-")
            val key = parsed[0] + this + parsed[1]
            if (!keyList.contains(key)){
                keyList[key] = line.value
            }
        }
    }
}