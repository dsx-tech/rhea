package uk.dsx.reactiveconfig

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import uk.dsx.reactiveconfig.interfaces.ConfigSource

class ConfigMock : ConfigSource {
    var channel: Channel<RawProperty>? = null

    override suspend fun subscribe(dataStream: Channel<RawProperty>) {
        channel = dataStream
    }

    fun pushChanges(key: String, value: String) {
        if (channel == null) {
            error("You didn't subscribe to this ConfigSource")
        } else {
            ConfigManager.configScope.launch {
                channel!!.send(RawProperty(key, value))
            }
        }
    }
}