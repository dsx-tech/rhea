package uk.dsx.reactiveconfig

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import uk.dsx.reactiveconfig.interfaces.ConfigSource

class ConfigMock : ConfigSource {
    var channel: Channel<RawProperty>? = null
    var scope: CoroutineScope? = null

    override suspend fun subscribe(dataStream: Channel<RawProperty>, scope: CoroutineScope) {
        channel = dataStream
        this.scope = scope
    }

    fun pushChanges(key: String, value: String) {
        if (channel == null) {
            error("You didn't subscribe to this ConfigSource")
        } else {
            scope?.launch {
                channel!!.send(RawProperty(key, value))
            }
        }
    }
}