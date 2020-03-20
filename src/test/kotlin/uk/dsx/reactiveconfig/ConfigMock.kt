package uk.dsx.reactiveconfig

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import uk.dsx.reactiveconfig.interfaces.ConfigSource

class ConfigMock : ConfigSource {
    private lateinit var channel: SendChannel<RawProperty>
    private lateinit var scope: CoroutineScope

    override suspend fun subscribe(channelOfChanges: SendChannel<RawProperty>, scope: CoroutineScope) {
        channel = channelOfChanges
        this.scope = scope
    }

    fun pushChanges(key: String, value: Any?) {
        scope.launch {
            channel.send(
                RawProperty(
                    key,
                    when (value) {
                        is Int -> NumericNode(value.toString())
                        is Long -> NumericNode(value.toString())
                        is Float -> NumericNode(value.toString())
                        is Double -> NumericNode(value.toString())
                        is Boolean -> BooleanNode(value)
                        is String -> StringNode(value)
                        else -> null
                    }
                )
            )
        }
    }
}