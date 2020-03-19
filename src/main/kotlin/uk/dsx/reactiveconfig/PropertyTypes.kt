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

sealed class ParseResult<T>
class Success<T>(val value: T) : ParseResult<T>()
class Failure<T> : ParseResult<T>()

class PropertyTypeBase(
    val map: ConcurrentHashMap<String, Reloadable<*>>,
    val flowOfChanges: Flow<RawProperty>,
    val scope: CoroutineScope
) {

    inner class PropertyType<T>(var initial: T, var parse: (Node?) -> ParseResult<T?>) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Reloadable<T> {
            return ReloadableFactory.createReloadable(property.name, this, map, flowOfChanges, scope)
        }
    }

    fun <T> PropertyType<T>.nullable(): PropertyType<T?> {
        return PropertyType(this.initial, { node: Node? ->
            this.parse(node).let { result: ParseResult<T?> ->
                when (result) {
                    is Success -> result
                    is Failure -> Success(null)
                }
            }
        })
    }

    val stringType: PropertyType<String> = PropertyType("", { node: Node? ->
        when (node) {
            is StringNode -> Success(node.value)
            else -> Failure()
        }
    })

    val intType: PropertyType<Int> = PropertyType(0, { node: Node? ->
        when (node) {
            is NumericNode -> Success(node.value.toInt())
            else -> Failure()
        }
    })

    val longType: PropertyType<Long> = PropertyType(0L, { node: Node? ->
        when (node) {
            is NumericNode -> Success(node.value.toLong())
            else -> Failure()
        }
    })

    val floatType: PropertyType<Float> = PropertyType(0.0F, { node: Node? ->
        when (node) {
            is NumericNode -> Success(node.value.toFloat())
            else -> Failure()
        }
    })

    val doubleType: PropertyType<Double> = PropertyType(0.0, { node: Node? ->
        when (node) {
            is NumericNode -> Success(node.value.toDouble())
            else -> Failure()
        }
    })

    val booleanType: PropertyType<Boolean> = PropertyType(false, { node: Node? ->
        when (node) {
            is BooleanNode -> Success(node.value)
            else -> Failure()
        }
    })
}