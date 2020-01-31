package uk.dsx.reactiveconfig

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import uk.dsx.reactiveconfig.interfaces.ConfigSource
import uk.dsx.reactiveconfig.interfaces.RawProperty

abstract class ConfigManagerBase {
    val configScope: CoroutineScope = ConfigScope
    val channel: Channel<RawProperty> = Channel()
    val properties: Map<String, Reloadable<*>> = HashMap()

    suspend fun addSource(source: ConfigSource) {
        source.subscribe(channel)
    }
}