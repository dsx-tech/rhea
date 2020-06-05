package uk.dsxt.rhea

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.EmptyCoroutineContext

val reactiveConfigLogger = KotlinLogging.logger {}

class ConfigManager {
    val configScope = CoroutineScope(EmptyCoroutineContext)
    @ExperimentalCoroutinesApi
    private val channelOfChanges: BroadcastChannel<RawProperty> = BroadcastChannel(Channel.BUFFERED)
    val mapOfProperties: MutableMap<String, Reloadable<*>> = ConcurrentHashMap()
    val mapOfSources: MutableMap<String, ConfigSource> = ConcurrentHashMap()
    @FlowPreview
    @ExperimentalCoroutinesApi
    val flowOfChanges: Flow<RawProperty> = channelOfChanges.asFlow()

    @ExperimentalCoroutinesApi
    fun addSource(source: ConfigSource) {
        configScope.launch {
            source.subscribe(channelOfChanges, configScope)
        }
        Thread.sleep(100)
    }
}