package uk.dsxt.rhea

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

/**
 *  @return [PropertyType] that allows value to be null.
 */
fun <T> PropertyType<T>.nullable(): PropertyType<T?> {
    return PropertyType(initial, { node: Node? ->
        parse(node).let { result: ParseResult<T?> ->
            when (result) {
                is ParseResult.Success -> result
                is ParseResult.Failure -> ParseResult.Success(null)
            }
        }
    })
}

/**
 * Represents type of property that has some default value [initial] and [parse] function.
 */
class PropertyType<T>(val initial: T, val parse: (Node?) -> ParseResult<T?>)

/**
 * The type of string property.
 */
@JvmField
val stringType: PropertyType<String> = PropertyType("", { node: Node? ->
    when (node) {
        is StringNode -> ParseResult.Success(node.value)
        else -> ParseResult.Failure()
    }
})

/**
 * The type of int property.
 */
@JvmField
val intType: PropertyType<Int> = PropertyType(0, { node: Node? ->
    when (node) {
        is NumericNode -> ParseResult.Success(node.value.toInt())
        is StringNode -> try {
            return@PropertyType ParseResult.Success(node.value.toInt())
        } catch (e: Exception) {
            return@PropertyType ParseResult.Failure()
        }
        else -> ParseResult.Failure()
    }
})

/**
 * The type of long property.
 */
@JvmField
val longType: PropertyType<Long> = PropertyType(0L, { node: Node? ->
    when (node) {
        is NumericNode -> ParseResult.Success(node.value.toLong())
        is StringNode -> try {
            return@PropertyType ParseResult.Success(node.value.toLong())
        } catch (e: Exception) {
            return@PropertyType ParseResult.Failure()
        }
        else -> ParseResult.Failure()
    }
})

/**
 * The type of float property.
 */
@JvmField
val floatType: PropertyType<Float> = PropertyType(0.0F, { node: Node? ->
    when (node) {
        is NumericNode -> ParseResult.Success(node.value.toFloat())
        is StringNode -> try {
            return@PropertyType ParseResult.Success(node.value.toFloat())
        } catch (e: Exception) {
            return@PropertyType ParseResult.Failure()
        }
        else -> ParseResult.Failure()
    }
})

/**
 * The type of double property.
 */
@JvmField
val doubleType: PropertyType<Double> = PropertyType(0.0, { node: Node? ->
    when (node) {
        is NumericNode -> ParseResult.Success(node.value.toDouble())
        is StringNode -> try {
            return@PropertyType ParseResult.Success(node.value.toDouble())
        } catch (e: Exception) {
            return@PropertyType ParseResult.Failure()
        }
        else -> ParseResult.Failure()
    }
})

/**
 * The type of boolean property.
 */
@JvmField
val booleanType: PropertyType<Boolean> = PropertyType(false, { node: Node? ->
    when (node) {
        is BooleanNode -> ParseResult.Success(node.value)
        is StringNode -> try {
            return@PropertyType ParseResult.Success(node.value.toBoolean())
        } catch (e: Exception) {
            return@PropertyType ParseResult.Failure()
        }
        else -> ParseResult.Failure()
    }
})