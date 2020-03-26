package uk.dsx.reactiveconfig

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KProperty

sealed class Node
data class ObjectNode(val value: MutableMap<String, Node?>) : Node()
data class NumericNode(val value: String) : Node()
data class StringNode(val value: String) : Node()
data class ArrayNode(val value: MutableList<Node?>) : Node()
data class BooleanNode(val value: Boolean) : Node()

sealed class ParseResult<T> {
    class Success<T>(val value: T) : ParseResult<T>()
    class Failure<T> : ParseResult<T>()
}

// todo: fix reloadable creation with delegation
fun <T> PropertyTypeBase.PropertyType<T>.nullable(): PropertyTypeBase.PropertyType<T?> {
    return base.PropertyType(initial, { node: Node? ->
        parse(node).let { result: ParseResult<T?> ->
            when (result) {
                is ParseResult.Success -> result
                is ParseResult.Failure -> ParseResult.Success(null)
            }
        }
    })
}

class PropertyTypeBase(
    val map: MutableMap<String, Reloadable<*>>,
    val flowOfChanges: Flow<RawProperty>,
    val scope: CoroutineScope
) {
    inner class PropertyType<T>(
        var initial: T,
        var parse: (Node?) -> ParseResult<T?>,
        val base: PropertyTypeBase = this@PropertyTypeBase
    ) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Reloadable<T> {
            return ReloadableFactory.createReloadable(property.name, this, map, flowOfChanges, scope)
        }
    }

    val stringType: PropertyType<String> = PropertyType("", { node: Node? ->
        when (node) {
            is StringNode -> ParseResult.Success(node.value)
            else -> ParseResult.Failure()
        }
    })

    val intType: PropertyType<Int> = PropertyType(0, { node: Node? ->
        when (node) {
            is NumericNode -> ParseResult.Success(node.value.toInt())
            else -> ParseResult.Failure()
        }
    })

    val longType: PropertyType<Long> = PropertyType(0L, { node: Node? ->
        when (node) {
            is NumericNode -> ParseResult.Success(node.value.toLong())
            else -> ParseResult.Failure()
        }
    })

    val floatType: PropertyType<Float> = PropertyType(0.0F, { node: Node? ->
        when (node) {
            is NumericNode -> ParseResult.Success(node.value.toFloat())
            else -> ParseResult.Failure()
        }
    })

    val doubleType: PropertyType<Double> = PropertyType(0.0, { node: Node? ->
        when (node) {
            is NumericNode -> ParseResult.Success(node.value.toDouble())
            else -> ParseResult.Failure()
        }
    })

    val booleanType: PropertyType<Boolean> = PropertyType(false, { node: Node? ->
        when (node) {
            is BooleanNode -> ParseResult.Success(node.value)
            else -> ParseResult.Failure()
        }
    })
}