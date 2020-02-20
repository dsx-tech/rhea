package uk.dsx.reactiveconfig

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import uk.dsx.reactiveconfig.interfaces.ConfigSource
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.KProperty

abstract class ConfigManagerBase {
    companion object {
        val configScope = CoroutineScope(EmptyCoroutineContext)
        val channelOfChanges: Channel<RawProperty> = Channel(Channel.BUFFERED)
        val properties: HashMap<String, Reloadable<*>> = HashMap()
        val flowOfChanges: Flow<RawProperty> = channelOfChanges.consumeAsFlow()
    }

    fun addSource(source: ConfigSource) {
        configScope.launch {
            source.subscribe(channelOfChanges)
        }
    }

    abstract class PropertyType<T> {
        private lateinit var reloadable: Reloadable<T>
        // should it be taken from initial state of config?
        abstract var initial: T

        operator fun getValue(thisRef: Any?, property: KProperty<*>): Reloadable<T> {
            if (!::reloadable.isInitialized) {
                reloadable = Reloadable(initial,
                    flowOfChanges.filter { rawProperty: RawProperty ->
                        rawProperty.key == property.name
                    }.map { rawProperty: RawProperty ->
                        parse(rawProperty.value)?.let { it }
                            ?: error("Wrong type of property: ${property.name}")
                    }
                )
                properties[property.name] = reloadable
            }
            return reloadable
        }

        abstract fun parse(value: String): T?
    }

    class StringType : PropertyType<String>() {
        override var initial: String = ""
        override fun parse(value: String): String? {
            return value
        }
    }

    class IntType : PropertyType<Int>() {
        override var initial: Int = 0
        override fun parse(value: String): Int? {
            return value.toIntOrNull()
        }
    }

    class LongType : PropertyType<Long>() {
        override var initial: Long = 0
        override fun parse(value: String): Long? {
            return value.toLongOrNull()
        }
    }

    class FloatType : PropertyType<Float>() {
        override var initial: Float = 0.0F
        override fun parse(value: String): Float? {
            return value.toFloatOrNull()
        }
    }

    class DoubleType : PropertyType<Double>() {
        override var initial: Double = 0.0
        override fun parse(value: String): Double? {
            return value.toDoubleOrNull()
        }
    }

    class BooleanType : PropertyType<Boolean>() {
        override var initial: Boolean = false
        override fun parse(value: String): Boolean? {
            return value.toBoolean()
        }
    }
}