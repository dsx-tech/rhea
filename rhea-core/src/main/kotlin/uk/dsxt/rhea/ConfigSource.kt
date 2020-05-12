package uk.dsxt.rhea

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel
import uk.dsxt.rhea.Node
import uk.dsxt.rhea.RawProperty

interface ConfigSource {
    suspend fun subscribe(channelOfChanges: SendChannel<RawProperty>, scope: CoroutineScope)

    fun getNode(key: String): Node?
}