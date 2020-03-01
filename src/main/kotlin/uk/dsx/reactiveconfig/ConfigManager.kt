package uk.dsx.reactiveconfig

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import uk.dsx.reactiveconfig.interfaces.ConfigSource
import kotlin.coroutines.EmptyCoroutineContext

object ConfigManager {
    val configScope = CoroutineScope(EmptyCoroutineContext)
    private val channelOfChanges: Channel<RawProperty> = Channel(Channel.BUFFERED)
    val properties: HashMap<String, Reloadable<*>> = HashMap()
    val flowOfChanges: Flow<RawProperty> = flow {
        channelOfChanges.consumeAsFlow().collect {
            emit(it)
        }
    }

    fun addSource(source: ConfigSource) {
        configScope.launch {
            source.subscribe(channelOfChanges)
        }
    }
}