package uk.dsx.reactiveconfig

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KProperty

sealed class Node
data class ObjectNode(val value: MutableMap<String, Node?>) : Node()
data class NumericNode(val value: String) : Node()
data class StringNode(val value: String) : Node()
data class ArrayNode(val value: MutableList<Node?>) : Node()
data class BooleanNode(val value: Boolean) : Node()

class PropertyTypeBase(val map: ConcurrentHashMap<String, Reloadable<*>>, val flowOfChanges: Flow<RawProperty>, val scope: CoroutineScope) {

    inner class PropertyType<T : Any>(val initial: T, val parse: (Node?) -> T?) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Reloadable<T> {
            return ReloadableFactory.createReloadable(property.name, this, map, flowOfChanges, scope)
        }
    }

    val stringType = PropertyType("", { node: Node? ->
        when (node) {
            is StringNode -> node.value
            else -> return@PropertyType null
        }
    })

    val intType = PropertyType(0, { node: Node? ->
        when (node) {
            is NumericNode -> node.value.toInt()
            else -> return@PropertyType null
        }
    })

    val longType = PropertyType(0L, { node: Node? ->
        when (node) {
            is NumericNode -> node.value.toLong()
            else -> return@PropertyType null
        }
    })

    val floatType = PropertyType(0.0F, { node: Node? ->
        when (node) {
            is NumericNode -> node.value.toFloat()
            else -> return@PropertyType null
        }
    })

    val doubleType = PropertyType(0.0, { node: Node? ->
        when (node) {
            is NumericNode -> node.value.toDouble()
            else -> return@PropertyType null
        }
    })

    val booleanType = PropertyType(false, { node: Node? ->
        when (node) {
            is BooleanNode -> node.value
            else -> return@PropertyType null
        }
    })
}