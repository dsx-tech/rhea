package uk.dsx.reactiveconfig

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import uk.dsx.reactiveconfig.interfaces.ConfigSource
import kotlin.coroutines.EmptyCoroutineContext

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
}