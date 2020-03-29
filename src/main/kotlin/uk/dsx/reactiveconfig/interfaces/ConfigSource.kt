package uk.dsx.reactiveconfig.interfaces

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel
import uk.dsx.reactiveconfig.RawProperty

interface ConfigSource {
    suspend fun subscribe(channelOfChanges: SendChannel<RawProperty>, scope: CoroutineScope)

    fun pushChanges(key: String)
}