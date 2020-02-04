package uk.dsx.reactiveconfig

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import uk.dsx.reactiveconfig.interfaces.ConfigSource
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.reflect.KProperty

abstract class ConfigManagerBase {
    companion object {
        val configScope = CoroutineScope(EmptyCoroutineContext)
        val channelOfChanges: Channel<RawProperty<Any>> = Channel()
        val properties: HashMap<String, Reloadable<Any>> = HashMap()
        // TODO consumeAsFlow() must be replaced because receiveChannel.consumeasflow can be collected just once
        val flowOfChanges: Flow<RawProperty<Any>> = channelOfChanges.consumeAsFlow()
    }

    suspend fun addSource(source: ConfigSource) {
        source.subscribe(channelOfChanges)
    }

    abstract class PropertyType<T> {
        abstract operator fun getValue(thisRef: Any?, property: KProperty<*>): Reloadable<T>
    }

    // TODO is it possible to parametrize StringType with String?
    class StringType : PropertyType<Any>() {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Reloadable<Any> {
            val reloadable = Reloadable("",
                flowOfChanges.filter {
                    it.key == property.name
                }
            )
            properties[property.name] = reloadable
            return reloadable
        }
    }
}