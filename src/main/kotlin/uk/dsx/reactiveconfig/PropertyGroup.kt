package uk.dsx.reactiveconfig

import java.util.*
import java.util.concurrent.ConcurrentHashMap

open class PropertyGroup(block: PropertyGroup.() -> Unit) {
    var keyList = ConcurrentHashMap<String, PropertyType<*>>()
    var toAdd : String = ""

    init {
        toAdd += javaClass.kotlin.simpleName?.substringBefore("$")?:
                throw IllegalArgumentException("cannot determine name of property group")
        apply(block)
    }

    infix fun <T> String.of(type: PropertyType<T>) {
        keyList["$toAdd.$this"] = type
    }

    infix fun String.of(block : () -> Unit){
        val temp = toAdd
        toAdd += "." + this
        block.invoke()
        toAdd = temp
    }
}