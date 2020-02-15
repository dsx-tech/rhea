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
                    flow {
                        flowOfChanges.filter {
                            it.key == property.name
                        }.collect {
                            val result = parse(it.value)
                            if (result != null) {
                                this.emit(result as T)
                            } else {
                                error("Wrong type of property: ${property.name}")
                            }
                        }
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
}