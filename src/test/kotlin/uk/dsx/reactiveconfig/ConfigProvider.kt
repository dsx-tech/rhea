package uk.dsx.reactiveconfig

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import uk.dsx.reactiveconfig.interfaces.ConfigSource

class ConfigProvider : ConfigSource {
    var channel : Channel<RawProperty>? = null
    var count = 0

    override suspend fun subscribe(dataStream: Channel<RawProperty>) {
        channel = dataStream
    }

    fun getChanges(keysOfProperties: List<String>) {
        if (channel == null) error("You didn't subscribe to this ConfigSource")

        ConfigManagerBase.configScope.launch {
            for (key in keysOfProperties) {
                channel!!.send(RawProperty(key, count.toString()))
                count++
            }
        }
    }
}