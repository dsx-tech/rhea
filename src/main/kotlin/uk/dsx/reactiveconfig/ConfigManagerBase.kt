package uk.dsx.reactiveconfig

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import uk.dsx.reactiveconfig.interfaces.ConfigSource
import kotlin.coroutines.EmptyCoroutineContext

abstract class ConfigManagerBase {
    companion object {
        val configScope = CoroutineScope(EmptyCoroutineContext)
    }

    val flowOfChanges: Flow<RawProperty> = flow {}
    val channelOfChanges: Channel<RawProperty> = Channel()
    val properties: Map<RawProperty, Reloadable<RawProperty>> = HashMap()

    suspend fun addSource(source: ConfigSource) {
        source.subscribe(channelOfChanges)
    }

    private class PropertyType<T> {

    }
}