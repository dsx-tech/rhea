package uk.dsx.reactiveconfig

import kotlin.reflect.KProperty

sealed class Node
data class ObjectNode(val value: Map<String, Node>) : Node()
data class IntNode(val value: Long) : Node()
data class FloatNode(val value: Double) : Node()
data class StringNode(val value: String) : Node()
data class ArrayNode(val value: List<Node>) : Node()
data class BooleanNode(val value: Boolean) : Node()
data class NullNode(val value: Any?) : Node()

class PropertyTypeBase(val configManager: ConfigManager) {

    inner class PropertyType<T : Any>(val initial: T, val parse: (Node) -> T?) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Reloadable<T> {
            return ReloadableFactory.createReloadable(property.name, this, configManager)
        }
    }

    val stringType = PropertyType("", { node: Node ->
        when (node) {
            is StringNode -> node.value
            else -> return@PropertyType null
        }
    })

    val intType = PropertyType(0, { node: Node ->
        when (node) {
            is IntNode -> node.value.toInt()
            else -> return@PropertyType null
        }
    })

    val longType = PropertyType(0L, { node: Node ->
        when (node) {
            is IntNode -> node.value
            else -> return@PropertyType null
        }
    })

    val floatType = PropertyType(0.0F, { node: Node ->
        when (node) {
            is FloatNode -> node.value.toFloat()
            else -> return@PropertyType null
        }
    })

    val doubleType = PropertyType(0.0, { node: Node ->
        when (node) {
            is FloatNode -> node.value
            else -> return@PropertyType null
        }
    })

    val booleanType = PropertyType(false, { node: Node ->
        when (node) {
            is BooleanNode -> node.value
            else -> return@PropertyType null
        }
    })
}