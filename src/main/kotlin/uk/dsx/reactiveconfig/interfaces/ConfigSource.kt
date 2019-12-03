package uk.dsx.reactiveconfig.interfaces

import kotlinx.coroutines.channels.Channel

interface ConfigSource {
    suspend fun subscribe(dataStream: Channel<RawProperty>)
}